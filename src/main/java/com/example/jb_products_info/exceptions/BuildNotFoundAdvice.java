package com.example.jb_products_info.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BuildNotFoundAdvice {
    private static final Logger logger = LoggerFactory.getLogger(BuildNotFoundAdvice.class);
    @ExceptionHandler(BuildNotFoundException.class)
    public ResponseEntity<String> buildNotFound(BuildNotFoundException ex) {
        logger.error("Build not found", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Build not found");
    }
}
