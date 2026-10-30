package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "hits")
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app", nullable = false)
    private String app;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "timestamp_hit", nullable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Hit hit = (Hit) o;
        return Objects.equals(id, hit.id) && Objects.equals(app, hit.app) && Objects.equals(uri, hit.uri) && Objects.equals(ip, hit.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, app, uri, ip);
    }
}