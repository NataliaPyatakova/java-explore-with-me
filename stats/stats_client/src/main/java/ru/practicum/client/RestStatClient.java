package ru.practicum.client;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RestStatClient implements StatClient {

    private final RestClient restClient;
    private final String statsUrl;

    public RestStatClient(@Value("http://localhost:9090") String statsUrl) {
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
            System.out.println("Сервис статистики не запущен! Не могу сохранить статистику.");
        }
    }

    @Override
    public Set<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        URI uri = UriComponentsBuilder.fromUriString(statsUrl + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .build()
                .encode()
                .toUri();
        Set<ViewStats> result = new HashSet<>();
        try {
            result = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (ResourceAccessException e) {
            System.out.println("Сервис статистики не запущен! Не могу получить статистику.");
        }
        return result;
    }
}