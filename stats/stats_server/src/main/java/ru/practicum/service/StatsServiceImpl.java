package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public Set<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("getStats start {} end {} uris {} unique {}", start, end, uris, unique);
        List<Hit> hits;
        if (uris != null && !uris.isEmpty()) {
            hits = hitRepository.findAllByTimestampBetweenAndUriIn(start, end, uris);
        } else {
            hits = hitRepository.findAllByTimestampBetween(start, end);
        }
        Set<ViewStats> setViewStats = new HashSet<>();
        if (!hits.isEmpty()) {
            setViewStats = hits.stream()
                    .map(hit -> new ViewStats().setUri(hit.getUri()).setApp(hit.getApp()).setHits(0))
                    .collect(Collectors.toSet());
            if (unique) {
                //учитываем только уникальные ip
                Set<Hit> uniqueHits = hits.stream()
                        .map(hit -> new Hit().setApp(hit.getApp()).setUri(hit.getUri()).setIp(hit.getIp()))
                        .collect(Collectors.toSet());
                setViewStats.forEach(viewStats ->
                        uniqueHits.forEach(hit -> {
                            if (hit.getApp().equals(viewStats.getApp()) && hit.getUri().equals(viewStats.getUri())) {
                                viewStats.setHits(viewStats.getHits() + 1);
                            }
                        })
                );
            } else {
                setViewStats.forEach(viewStats ->
                        hits.forEach(hit -> {
                            if (hit.getApp().equals(viewStats.getApp()) && hit.getUri().equals(viewStats.getUri())) {
                                viewStats.setHits(viewStats.getHits() + 1);
                            }
                        })
                );
            }
        }
        return setViewStats
                .stream()
                .sorted(Comparator.comparing(ViewStats::getHits).reversed())  //по убыванию
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}