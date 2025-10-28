package com.example.demo.base.infra;

import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private record ErrorResponse(String erro) {}

    // Retorna HTTP 400 (Bad Request).
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponse> handlerRegraDeNegocio(NegocioException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Retorna erro HTTP 404 (Not Found).
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
