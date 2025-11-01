package com.example.demo.base.infra;

import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Itera sobre todos os erros de campo encontrados na exceção
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // Obtém o nome do campo que falhou na validação
            String fieldName = ((FieldError) error).getField();
            // Obtém a mensagem de erro que você definiu na annotation da DTO
            String errorMessage = error.getDefaultMessage();
            // Adiciona ao mapa
            errors.put(fieldName, errorMessage);
        });
        
        return errors;
    }
}
