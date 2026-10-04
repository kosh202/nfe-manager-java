package br.com.nfe.manager.exception;

/** Indica que a chave de acesso já foi importada anteriormente. */
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
