package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RestStatClient implements StatClient {

    private final RestClient restClient;
    private final String statsUrl;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RestStatClient(@Value("${stats.server.url}") String statsUrl) {
        this.restClient = RestClient.create();
        this.statsUrl = statsUrl;
    }

    @Override
    public void hit(EndpointHit endpointHit) {
        URI uri = UriComponentsBuilder.fromUriString(statsUrl + "/hit").build().toUri();
        try {
            restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHit)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            log.info("Сервис статистики не запущен! Не могу сохранить статистику.");
        }
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        URI uri = UriComponentsBuilder.fromUriString(statsUrl + "/stats")
                .queryParam("start", start.format(dtf))
                .queryParam("end", end.format(dtf))
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .build()
                .encode()
                .toUri();
        List<ViewStats> result = new ArrayList<>();
        try {
            result = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (ResourceAccessException e) {
            log.info("Сервис статистики не запущен! Не могу получить статистику.");
        }
        return result;
    }
}