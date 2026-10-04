package br.com.nfe.manager.dto;

import br.com.nfe.manager.model.Empresa;

/** Representação pública de uma empresa, sem expor diretamente a entidade JPA. */
public class EmpresaDTO {

    private Long id;
    private String cnpj;
    private String nome;

    public EmpresaDTO() {
    }

    public EmpresaDTO(Long id, String cnpj, String nome) {
        this.id = id;
        this.cnpj = cnpj;
        this.nome = nome;
    }

    public static EmpresaDTO fromEntity(Empresa empresa) {
        // A conversão mantém o contrato da API separado do modelo de persistência.
        if (empresa == null) return null;
        return new EmpresaDTO(empresa.getId(), empresa.getCnpj(), empresa.getNome());
    }

    public Long getId() {
        return id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getNome() {
        return nome;
    }
}
