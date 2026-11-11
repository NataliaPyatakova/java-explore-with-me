package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class NewEventDto {

    @NotBlank
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;
    @NotBlank
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;
    @NotBlank
    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
    private String description;
    @NotNull
    private Long category;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    private LocationDto location;
    private Boolean paid = false;
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;

    //для моков - иначе не срабатывает подстановка в моке
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NewEventDto that = (NewEventDto) o;
        return Objects.equals(title, that.title)
                && Objects.equals(annotation, that.annotation)
                && Objects.equals(description, that.description)
                && Objects.equals(category, that.category)
                && Objects.equals(location, that.location)
                && Objects.equals(paid, that.paid)
                && Objects.equals(participantLimit, that.participantLimit)
                && Objects.equals(requestModeration, that.requestModeration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, annotation, description, category, location, paid, participantLimit, requestModeration);
    }
}
