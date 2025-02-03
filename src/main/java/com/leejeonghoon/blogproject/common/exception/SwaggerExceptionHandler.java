package com.leejeonghoon.blogproject.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SwaggerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleSwaggerException(Exception e, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/v3/api-docs") || request.getRequestURI().startsWith("/swagger-ui")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Swagger 요청 처리 중 오류가 발생했습니다.\n" + e.getMessage());
        }
        return null;
    }
}

