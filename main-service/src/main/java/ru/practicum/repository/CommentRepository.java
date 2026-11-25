package ru.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorIdOrderByDateCreationAsc(Long authorId);

    @EntityGraph(attributePaths = {"author"})
    List<Comment> findAllByEventIdOrderByDateCreationAsc(Long eventId);

}
