package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(hit.ip))" +
            "from Hit as hit " +
            "where hit.timestamp between :start and :end " +
            "group by hit.app, hit.uri " +
            "order by count(hit.ip) desc")
    List<ViewStats> findAllByTimestampBetweenNotUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(distinct hit.ip))" +
            "from Hit as hit " +
            "where hit.timestamp between :start and :end " +
            "group by hit.app, hit.uri " +
            "order by count(distinct hit.ip) desc")
    List<ViewStats> findAllByTimestampBetweenUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(hit.ip))" +
            "from Hit as hit " +
            "where hit.timestamp between :start and :end " +
            "and hit.uri in :uris " +
            "group by hit.app, hit.uri " +
            "order by count(hit.ip) desc")
    List<ViewStats> findAllByTimestampBetweenAndUriInNotUniqueIp(@Param("start") LocalDateTime start,
                                                                 @Param("end") LocalDateTime end,
                                                                 @Param("uris") List<String> uris);

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(distinct hit.ip))" +
            "from Hit as hit " +
            "where hit.timestamp between :start and :end " +
            "and hit.uri in :uris " +
            "group by hit.app, hit.uri " +
            "order by count(distinct hit.ip) desc")
    List<ViewStats> findAllByTimestampBetweenAndUriInUniqueIp(@Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end,
                                                              @Param("uris") List<String> uris);
}
