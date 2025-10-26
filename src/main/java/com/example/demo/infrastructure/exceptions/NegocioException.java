package com.example.demo.infrastructure.exceptions;

public class NegocioException extends RuntimeException{

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
