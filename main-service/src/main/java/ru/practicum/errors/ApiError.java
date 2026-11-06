package ru.practicum.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ApiError {

    private final List<String> errors;
    private final String message;
    private final String reason;
    private final String status;
    private final String timestamp;
}
