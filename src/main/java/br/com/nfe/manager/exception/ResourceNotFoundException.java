package br.com.nfe.manager.exception;

/** Exceção de domínio convertida pelo handler global em HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
