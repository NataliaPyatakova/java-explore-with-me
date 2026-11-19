package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.event.EventCountRequests;
import ru.practicum.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    @Query("select new ru.practicum.dto.event.EventCountRequests(req.event.id, count(req.id)) " +
            "from Request as req " +
            "where req.event.id = :eventId and req.status = 'CONFIRMED' " +
            "group by req.event.id")
    EventCountRequests countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("select new ru.practicum.dto.event.EventCountRequests(req.event.id, count(req.id)) " +
            "from Request as req " +
            "where req.event.id in (:ids) and req.status = 'CONFIRMED' " +
            "group by req.event.id")
    List<EventCountRequests> countConfirmedRequestsByListEventId(@Param("ids") List<Long> ids);
}
