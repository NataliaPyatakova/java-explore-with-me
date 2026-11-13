package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation mapToCompilationForCreate(NewCompilationDto newCompilationDto, List<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setEvents(events);
        return compilation;
    }

    public static CompilationDto mapToCompilationDtoFromCompilation(Compilation compilation, List<EventShortDto> events) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setPinned(compilation.isPinned());
        compilationDto.setEvents(events);
        return compilationDto;
    }

    public static Compilation mapToCompilationForUpdate(Compilation foundedComp, UpdateCompilationRequest dto, List<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setId(foundedComp.getId());
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        } else {
            compilation.setTitle(foundedComp.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        } else {
            compilation.setPinned(foundedComp.isPinned());
        }
        if (events != null && !events.isEmpty()) {
            compilation.setEvents(events);
        } else {
            compilation.setEvents(foundedComp.getEvents());
        }
        return compilation;
    }
}