package br.com.nfe.manager.dto;

/** Resultado resumido retornado após uma importação bem-sucedida. */
public class ImportResultDTO {

    private String mensagem;
    private String chaveAcesso;
    private Long id;

    public ImportResultDTO() {
    }

    public ImportResultDTO(String mensagem, String chaveAcesso, Long id) {
        this.mensagem = mensagem;
        this.chaveAcesso = chaveAcesso;
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public Long getId() {
        return id;
    }
}
