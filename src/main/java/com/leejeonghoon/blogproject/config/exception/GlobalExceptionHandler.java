package com.leejeonghoon.blogproject.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handlerIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("잘못된 요청입니다." + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handlerException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부에 오류가 발생했습니다.");
    }
}
