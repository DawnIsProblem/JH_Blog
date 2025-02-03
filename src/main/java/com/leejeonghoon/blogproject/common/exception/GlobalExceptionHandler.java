package com.leejeonghoon.blogproject.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handlerIllegalArgumentException(IllegalArgumentException e) {
        logger.error("잘못된 요청 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body("잘못된 요청입니다. " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handlerException(Exception e, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/v3/api-docs") || request.getRequestURI().startsWith("/swagger-ui")) {
            logger.error("Swagger 요청 중 예외 발생: {}", e.getMessage(), e);
            return null;
        }

        logger.error("서버 내부 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부에 오류가 발생했습니다. " + e.getMessage());
    }
}

