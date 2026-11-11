package ru.practicum.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.practicum.model.Compilation;

@Component
public class CompilationSpecification {

    public static Specification<Compilation> isPinned(Boolean pinned){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("pinned"), pinned);
    }
}