package ru.yandex.practicum.filmorate.exception.handler;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ConstraintViolationException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(400, String.format("A validation error occurred while handling request:%n %s",
                e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(404, e.getMessage());
    }

    @ExceptionHandler({Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedException(Exception e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(500, "An unexpected error occurred.");
    }

    public record ErrorResponse(int statusCode, String message) {
    }
}

