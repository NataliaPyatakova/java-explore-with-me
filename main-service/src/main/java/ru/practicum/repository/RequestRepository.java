package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByEventId(Long eventId);
}
