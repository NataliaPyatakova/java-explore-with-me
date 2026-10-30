package ru.practicum.errors;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.BadRequestException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
    public ApiError handleException(final Exception e) {
        log.info("handleException {}", e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError(
                new ArrayList<>(Collections.singleton(stackTrace)),
                e.getMessage(),
                "",
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                LocalDateTime.now().format(dtf));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handleIncorrectParameter(final RuntimeException e) {
        log.info("handleIncorrectParameter {}", e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError(
                new ArrayList<>(Collections.singleton(stackTrace)),
                e.getMessage(),
                "",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().format(dtf));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handleBadRequest(final BadRequestException e) {
        log.info("handleBadRequest {}", e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError(
                new ArrayList<>(Collections.singleton(stackTrace)),
                e.getMessage(),
                "",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().format(dtf));
    }
}
