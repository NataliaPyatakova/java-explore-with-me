package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepository;

    @Override
    @Transactional
    public void save(EndpointHit endpointHit) {
        log.info("save endpointHit {}", endpointHit);
        hitRepository.save(HitMapper.mapToHit(endpointHit));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("getStats start {} end {} uris {} unique {}", start, end, uris, unique);
        if (start.isAfter(end)) {
            throw new BadRequestException("Дата начала позже даты конца");
        }
        List<ViewStats> viewStatsList;
        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                viewStatsList = hitRepository.findAllByTimestampBetweenAndUriInUniqueIp(start, end, uris);
            } else {
                viewStatsList = hitRepository.findAllByTimestampBetweenAndUriInNotUniqueIp(start, end, uris);
            }
        } else {
            if (unique) {
                viewStatsList = hitRepository.findAllByTimestampBetweenUniqueIp(start, end);
            } else {
                viewStatsList = hitRepository.findAllByTimestampBetweenNotUniqueIp(start, end);
            }
        }
        return viewStatsList;
    }
}