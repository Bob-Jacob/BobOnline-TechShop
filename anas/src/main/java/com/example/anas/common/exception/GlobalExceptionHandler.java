package com.example.anas.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        log.error("Illegal argument: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("title", "Ошибка");
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException e, Model model) {
        log.error("Runtime error: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Произошла ошибка. Попробуйте позже.");
        model.addAttribute("title", "Ошибка");
        return "error";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException e, Model model) {
        log.error("Page not found: {}", e.getRequestURL());
        model.addAttribute("errorMessage", "Страница не найдена");
        model.addAttribute("title", "404 - Страница не найдена");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Произошла непредвиденная ошибка");
        model.addAttribute("title", "Ошибка");
        return "error";
    }
}
