package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {

    public static Location mapToLocationFromLocationDto(LocationDto locationDto) {
        Location location = new Location();
        location.setLon(locationDto.getLon());
        location.setLat(locationDto.getLat());
        return location;
    }

    public static LocationDto mapToLocationDtoFromLocation(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setLon(location.getLon());
        locationDto.setLat(location.getLat());
        return locationDto;
    }
}
