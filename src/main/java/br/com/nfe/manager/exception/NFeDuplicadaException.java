package br.com.nfe.manager.exception;

public class NFeDuplicadaException extends RuntimeException {

    private final String chaveAcesso;

    public NFeDuplicadaException(String chaveAcesso) {
        super("Nota fiscal com a chave de acesso '" + chaveAcesso + "' já está cadastrada no sistema.");
        this.chaveAcesso = chaveAcesso;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }
}
