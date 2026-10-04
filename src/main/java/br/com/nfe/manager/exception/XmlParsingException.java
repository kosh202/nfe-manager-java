package br.com.nfe.manager.exception;

/** Representa XML ausente, inválido ou incompatível com o layout esperado. */
public class XmlParsingException extends RuntimeException {

    public XmlParsingException(String message) {
        super(message);
    }

    public XmlParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
