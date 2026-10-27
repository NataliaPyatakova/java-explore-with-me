package ru.practicum.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceTest {

    @Autowired
    private StatsService statsService;
    private final EntityManager em;

    private final LocalDateTime time = LocalDateTime.now();
    private final LocalDateTime startTime = time.minusDays(1);
    private final LocalDateTime endTime = time.plusDays(1);
    private final EndpointHit endpointHit = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri")
            .setIp("testIp")
            .setTimestamp(time);
    private final EndpointHit endpointHitOtherIp = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri")
            .setIp("testIp1")
            .setTimestamp(time);
    private final EndpointHit endpointHitOtherIp2 = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri")
            .setIp("testIp2")
            .setTimestamp(time);
    private final EndpointHit endpointHitSameIpAnotherApp = new EndpointHit()
            .setApp("testApp1")
            .setUri("testUri")
            .setIp("testIp")
            .setTimestamp(time);
    private final EndpointHit endpointHitSameIpAnotherUri = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri1")
            .setIp("testIp")
            .setTimestamp(time);
    private final EndpointHit endpointHitSameIpAnotherAppAnotherUri = new EndpointHit()
            .setApp("testApp1")
            .setUri("testUri1")
            .setIp("testIp")
            .setTimestamp(time);
    private final EndpointHit endpointHitSameIpAnotherAppAnotherUriDiffDate = new EndpointHit()
            .setApp("testApp1")
            .setUri("testUri1")
            .setIp("testIp")
            .setTimestamp(time.plusDays(2));
    private final EndpointHit endpointHitSameAllDiffDate = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri")
            .setIp("testIp")
            .setTimestamp(time.plusDays(2));
    List<String> uris = new ArrayList<>();

    @Test
    @DisplayName("Сохранение hit - все хорошо")
    public void testSave() {
        statsService.save(endpointHit);
        TypedQuery<Hit> query = em.createQuery("SELECT h FROM Hit h where h.app = :app and h.uri = :uri and h.ip = :ip",
                Hit.class);
        query.setParameter("app", endpointHit.getApp());
        query.setParameter("uri", endpointHit.getUri());
        query.setParameter("ip", endpointHit.getIp());
        Hit result = query.getSingleResult();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
        Assertions.assertEquals(endpointHit.getIp(), result.getIp());
    }

    @Test
    @DisplayName("Получение stats - без хитов, без uri, без уникальных ip")
    public void testGetStatsNoHitsNoUriNoUniqueIp() {
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        Assertions.assertEquals(0, resultSet.size());
    }

    @Test
    @DisplayName("Получение stats - без хитов, с uri, без уникальных ip")
    public void testGetStatsNoHitsWithUriNoUniqueIp() {
        uris.add(endpointHit.getUri());
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, uris, false);
        Assertions.assertEquals(0, resultSet.size());
    }

    @Test
    @DisplayName("Получение stats - без хитов, с uri, c уникальными ip")
    public void testGetStatsNoHitsWithUriWithUniqueIp() {
        uris.add(endpointHit.getUri());
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, uris, true);
        Assertions.assertEquals(0, resultSet.size());
    }

    @Test
    @DisplayName("Получение stats - по 1му хиту, без uri, без уникальных ip")
    public void testGetStats1HitNoUriNoUniqueIp() {
        statsService.save(endpointHit);
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(1, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с одинаковыми app и uri, без фильтрации по uri, без уникальных ip")
    public void testGetStats2HitNoUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitOtherIp); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(2, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с одинаковыми app и uri, c фильтрацией по uri, без уникальных ip")
    public void testGetStats2HitWithUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitOtherIp); //пройдет
        uris.add(endpointHit.getUri());
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, uris, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(2, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с разными app и одинаковыми uri, без фильтрацией по uri, без уникальных ip")
    public void testGetStats2DiffHitNoUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitSameIpAnotherApp); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(2, resultSet.size());
        Assertions.assertEquals(1, result.getHits()); //не важно кто - у них должно быть по 1му хиту
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с одинаковыми app и разными uri, без фильтрацией по uri, без уникальных ip")
    public void testGetStats2DiffHit1NoUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitSameIpAnotherUri); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(2, resultSet.size());
        Assertions.assertEquals(1, result.getHits()); //не важно кто - у них должно быть по 1му хиту
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с разными app и разными uri, без фильтрацией по uri, без уникальных ip")
    public void testGetStats2DiffHit2NoUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitSameIpAnotherAppAnotherUri); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(2, resultSet.size());
        Assertions.assertEquals(1, result.getHits()); //не важно кто - у них должно быть по 1му хиту
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с разными app и разными uri, с фильтрацией по uri, без уникальных ip")
    public void testGetStats2DiffHit2WithUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitSameIpAnotherAppAnotherUri); //не пройдет
        uris.add(endpointHit.getUri());
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, uris, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(1, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с разными app и разными uri, без фильтрацией по uri, без уникальных ip, " +
            "с фильтрацией по датам")
    public void testGetStats2DiffHit2NoUriNoUniqueIpDateFilter() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitSameIpAnotherAppAnotherUriDiffDate); //не пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(1, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с одинаковыми app и uri, без фильтрацией по uri, без уникальных ip, " +
            "с фильтрацией по датам")
    public void testGetStats2SameHitNoUriNoUniqueIpDateFilter() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitSameAllDiffDate); //не пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(1, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 3м хитам с одинаковыми всем, без фильтрацией по uri, без уникальных ip")
    public void testGetStats3SameHitNoUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHit); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(3, result.getHits()); //схлопнулись в 1 запись
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 3м хитам с разными app и разными uri, без фильтрацией по uri, без уникальных ip, " +
            "с сортировкой")
    public void testGetStats3DiffHit2NoUriNoUniqueIpSorted() {
        //В теле ответа должна соблюдаться сортировка по убыванию количества просмотров
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitOtherIp); //пройдет
        statsService.save(endpointHitSameIpAnotherAppAnotherUri); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(2, resultSet.size());
        Assertions.assertEquals(2, result.getHits()); //у первого 2 хита
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 1му хиту, без uri, с уникальными ip")
    public void testGetStats1HitNoUriWithUniqueIp() {
        statsService.save(endpointHit); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, true);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(1, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());

    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с одинаковыми app и uri, разными ip, без uri, с уникальными ip")
    public void testGetStats2HitNoUriWithUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitOtherIp); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, true);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size()); //1 запись
        Assertions.assertEquals(2, result.getHits()); //на 2 хита
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 3м хитам с одинаковыми всем, без uri, с уникальными ip")
    public void testGetStats3HitSameAllNoUriWithUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHit); //не пройдет
        statsService.save(endpointHit); //не пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, true);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size()); //1 запись
        Assertions.assertEquals(1, result.getHits()); //на 1 хит
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 3м хитам с одинаковым app и одинаковым uri и разными ip, без фильтрацией по uri, " +
            "с уникальными ip, с сортировкой")
    public void testGetStats3DiffHit2NoUriWithUniqueIpSorted() {
        //В теле ответа должна соблюдаться сортировка по убыванию количества просмотров
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitOtherIp); //пройдет
        statsService.save(endpointHitOtherIp2); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, true);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());  //схлопнулись
        Assertions.assertEquals(3, result.getHits()); //в 3 хита
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 3м хитам с одинаковым app и одинаковым uri и одинаковыми ip у 2," +
            " без фильтрацией по uri, с уникальными ip, с сортировкой")
    public void testGetStats3SameHit2NoUriWithUniqueIpSorted() {
        //В теле ответа должна соблюдаться сортировка по убыванию количества просмотров
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHit); //не пройдет
        statsService.save(endpointHitOtherIp2); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, null, true);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());  //схлопнулись
        Assertions.assertEquals(2, result.getHits()); //в 2 хита
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }

    @Test
    @DisplayName("Получение stats - по 2м хитам с одинаковыми app и uri, с пустой uri, без уникальных ip")
    public void testGetStats2HitEmptyUriNoUniqueIp() {
        statsService.save(endpointHit); //пройдет
        statsService.save(endpointHitOtherIp); //пройдет
        Set<ViewStats> resultSet = statsService.getStats(startTime, endTime, uris, false);
        ViewStats result = resultSet.iterator().next();
        Assertions.assertEquals(1, resultSet.size());
        Assertions.assertEquals(2, result.getHits());
        Assertions.assertEquals(endpointHit.getApp(), result.getApp());
        Assertions.assertEquals(endpointHit.getUri(), result.getUri());
    }
}