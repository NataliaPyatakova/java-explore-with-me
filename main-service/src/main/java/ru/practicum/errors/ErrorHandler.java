package ru.practicum.errors;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

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

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
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
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handleConflictDB(final DataIntegrityViolationException e) {
        log.info("handleConflictDB {}", e.getMessage());
        return new ApiError(
                null,
                e.getMessage(),
                "",
                HttpStatus.CONFLICT.toString(),
                LocalDateTime.now().format(dtf));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handleBadRequest(final BadRequestException e) {
        log.info("handleBadRequest {}", e.getMessage());
        return new ApiError(null,
                e.getMessage(),
                e.getReason(),
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().format(dtf));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND) //404
    public ApiError handleNotFound(final NotFoundException e) {
        log.info("handleNotFound {}", e.getMessage());
        return new ApiError(null,
                e.getMessage(),
                e.getReason(),
                HttpStatus.NOT_FOUND.toString(),
                LocalDateTime.now().format(dtf));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handleConflict(final ValidationException e) {
        log.info("handleConflict {}", e.getMessage());
        return new ApiError(null,
                e.getMessage(),
                e.getReason(),
                HttpStatus.CONFLICT.toString(),
                LocalDateTime.now().format(dtf));
    }
}