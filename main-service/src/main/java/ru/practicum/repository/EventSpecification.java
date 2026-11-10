package ru.practicum.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.practicum.enumeration.EventState;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventSpecification {

    public static Specification<Event> hasInitiatorId(Long id){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("initiator").get("id"), id);
    }

    public static Specification<Event> hasInitiatorIdInList(List<Long> ids) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.join("initiator").get("id")).value(ids);
    }

    public static Specification<Event> hasCategoryIdInList(List<Long> ids){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.join("category").get("id")).value(ids);
    }

    public static Specification<Event> hasStateInList(List<EventState> states){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("state")).value(states);
    }

    public static Specification<Event> isAfter(LocalDateTime rangeStart){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
    }

    public static Specification<Event> isBefore(LocalDateTime rangeEnd){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
    }
}
