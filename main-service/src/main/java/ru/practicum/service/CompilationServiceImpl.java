package ru.practicum.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.ParamCompilationFindAll;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.ParamEventDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.QCompilation;
import ru.practicum.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    @Override
    @Transactional
    public CompilationDto save(NewCompilationDto newDto) {
        log.info("save compilation {}", newDto);
        Result events = getEvents(newDto.getEvents());
        Compilation compilation = CompilationMapper.mapToCompilationForCreate(newDto, events.eventList());
        return CompilationMapper.mapToCompilationDtoFromCompilation(compilationRepository.save(compilation),
                events.eventShortDtoList());
    }


    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest updateDto) {
        log.info("update compilation compId {} updateDto {}", compId, updateDto);
        Compilation foundedComp = findCompilationById(compId);
        Result events = getEvents(updateDto.getEvents());
        Compilation compilation = CompilationMapper.mapToCompilationForUpdate(foundedComp, updateDto, events.eventList());
        return CompilationMapper.mapToCompilationDtoFromCompilation(compilationRepository.save(compilation),
                events.eventShortDtoList());
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.info("delete compilation compId {}", compId);
        findCompilationById(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto findById(Long compId) {
        log.info("findById compilation compId {}", compId);
        Compilation foundedComp = findCompilationById(compId);
        Result events = getEvents(foundedComp.getEvents().stream().map(Event::getId).toList());
        return CompilationMapper.mapToCompilationDtoFromCompilation(foundedComp, events.eventShortDtoList());
    }

    @Override
    public List<CompilationDto> findAll(ParamCompilationFindAll params) {
        log.info("findAll compilation params {}", params);
        List<Compilation> compilationList;
        //в требованиях не нашла - сортируем по Id чтобы не кучей
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize(), sortById);
        Page<Compilation> page;
        if (params.getPinned() == null) {
            page = compilationRepository.findAll(pageable);
        } else {
            BooleanExpression byPinned = QCompilation.compilation.pinned.eq(params.getPinned());
            page = compilationRepository.findAll(byPinned, pageable);
        }
        compilationList = page.getContent();
        //собираем все Id event
        List<Long> ids = new ArrayList<>();
        compilationList.forEach(compilation ->
                compilation.getEvents().forEach(event -> ids.add(event.getId())));
        //запускаем по ним доначитку данных про просмотры и статистику
        Map<Long, ParamEventDto> mapParams = eventService.calcStatistic(ids);
        //мапим
        Map<Long, List<EventShortDto>> mapEvents = new HashMap<>();
        compilationList.forEach(compilation -> {
            mapEvents.computeIfAbsent(compilation.getId(),
                    k -> compilation.getEvents().stream().map(event ->
                            EventMapper.mapFromEventToEventShortDto(event, mapParams.get(event.getId()))).toList());
        });
        return compilationList.stream().map(comp ->
                CompilationMapper.mapToCompilationDtoFromCompilation(comp, mapEvents.get(comp.getId()))).toList();
    }

    private Compilation findCompilationById(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found",
                        "The required object was not found."));
    }

    private Result getEvents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new Result(new ArrayList<>(), new ArrayList<>());
        }
        List<EventShortDto> eventShortDtoList = eventService.findAllByIdIn(ids);
        List<Event> eventList = eventShortDtoList.stream().map(EventMapper::mapFromEventShortDtoToEvent).toList();
        return new Result(eventList, eventShortDtoList);
    }

    private record Result(List<Event> eventList, List<EventShortDto> eventShortDtoList) {
    }
}