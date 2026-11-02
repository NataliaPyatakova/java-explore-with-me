package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.EndpointHit;
import ru.practicum.model.Hit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HitMapper {

    public static Hit mapToHit(EndpointHit endpointHit) {
        Hit hit = new Hit();
        hit.setApp(endpointHit.getApp());
        hit.setUri(endpointHit.getUri());
        hit.setIp(endpointHit.getIp());
        hit.setTimestamp(endpointHit.getTimestamp());
        return hit;
    }
}
