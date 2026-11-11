package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.enumeration.AdminEventStateAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpdateEventAdminRequest extends UpdateEventRequest {

    private AdminEventStateAction stateAction;

    //для корректного вывода в логи
    @Override
    public String toString() {
        return "UpdateEventAdminRequest{" + super.toString() + ", stateAction=" + stateAction + "}";
    }

    //для моков - иначе не срабатывает подстановка в моке
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UpdateEventAdminRequest that = (UpdateEventAdminRequest) o;
        return super.getTitle().equals(that.getTitle())
                && super.getDescription().equals(that.getDescription())
                && super.getAnnotation().equals(that.getAnnotation())
                && super.getCategory().equals(that.getCategory())
                && super.getLocation().equals(that.getLocation())
                && super.getPaid().equals(that.getPaid())
                && super.getParticipantLimit().equals(that.getParticipantLimit())
                && super.getRequestModeration().equals(that.getRequestModeration())
                && stateAction == that.stateAction;
    }
}
