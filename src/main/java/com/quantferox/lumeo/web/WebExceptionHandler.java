package com.quantferox.lumeo.web;

import com.quantferox.lumeo.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Handles exceptions thrown from Thymeleaf MVC controllers and renders
 * user-friendly HTML error pages - distinct from the REST {@code GlobalExceptionHandler}
 * which is scoped to {@code /api/**} and returns JSON.
 */
@Slf4j
@ControllerAdvice(basePackages = "com.quantferox.lumeo.web")
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorCode",    "404");
        model.addAttribute("errorTitle",   "Page Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandler(Exception ex, Model model) {
        model.addAttribute("errorCode",    "404");
        model.addAttribute("errorTitle",   "Page Not Found");
        model.addAttribute("errorMessage", "The page you're looking for doesn't exist.");
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(AccessDeniedException ex, Model model) {
        model.addAttribute("errorCode",    "403");
        model.addAttribute("errorTitle",   "Access Denied");
        model.addAttribute("errorMessage", "You don't have permission to access this page.");
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Unhandled web exception", ex);
        model.addAttribute("errorCode",    "500");
        model.addAttribute("errorTitle",   "Something went wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        return "error/500";
    }
}
