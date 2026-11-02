package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.EndpointHit;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;

@SpringBootTest
public class HitMapperTest {

    private final LocalDateTime time = LocalDateTime.now();
    private final EndpointHit endpointHit = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri")
            .setIp("testIp")
            .setTimestamp(time);

    @Test
    @DisplayName("Тест маппера")
    public void testMapToHit() {
        Hit result = HitMapper.mapToHit(endpointHit);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getIp(), result.getIp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
        Assertions.assertEquals(endpointHit.getTimestamp(), result.getTimestamp());
    }
}