package com.example.demo.base.exception;

public class NegocioException extends RuntimeException{

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
