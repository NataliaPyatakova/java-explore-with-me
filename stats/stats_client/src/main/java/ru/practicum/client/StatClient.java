package ru.practicum.client;

import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface StatClient {

    void hit(EndpointHit endpointHit);

    Set<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
