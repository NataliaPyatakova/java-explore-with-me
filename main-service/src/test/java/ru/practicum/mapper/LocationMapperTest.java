package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.model.Location;

@SpringBootTest
public class LocationMapperTest {

    private final Location location = new Location().setLon(123.456).setLat(345.678);
    private final LocationDto locationDto = new LocationDto().setLon(123.456).setLat(345.678);

    @Test
    @DisplayName("Тест маппера из Location в LocationDto")
    void testMapToLocationDtoFromLocation() {
        LocationDto result = LocationMapper.mapToLocationDtoFromLocation(location);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(location.getLat(), result.getLat());
        Assertions.assertEquals(location.getLon(), result.getLon());
    }

    @Test
    @DisplayName("Тест маппера из LocationDto в Location")
    void testMapToLocationFromLocationDto() {
        Location result = LocationMapper.mapToLocationFromLocationDto(locationDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(locationDto.getLat(), result.getLat());
        Assertions.assertEquals(locationDto.getLon(), result.getLon());
    }
}