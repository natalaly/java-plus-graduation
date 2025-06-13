package ru.practicum.exception.handler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "ru.practicum")
public class MainServiceErrorHandler extends ErrorHandler {

}
