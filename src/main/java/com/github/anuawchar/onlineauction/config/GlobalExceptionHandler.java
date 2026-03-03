package com.github.anuawchar.onlineauction.config;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleValidationException(BindException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("statusCode", HttpStatus.BAD_REQUEST.value());
        mav.addObject("errorMessage", "Validation failed: " + ex.getMessage());
        mav.addObject("timestamp", new java.util.Date());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        mav.addObject("timestamp", new java.util.Date());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("errorMessage", "An unexpected error occurred");
        mav.addObject("timestamp", new java.util.Date());
        mav.setViewName("error");
        return mav;
    }
}
