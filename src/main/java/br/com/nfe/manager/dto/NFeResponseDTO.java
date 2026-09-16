package br.com.nfe.manager.dto;

import br.com.nfe.manager.model.NFe;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NFeResponseDTO {

    private Long id;
    private String chaveAcesso;
    private String numero;
    private OffsetDateTime data;
    private EmpresaDTO empresa;
    private BigDecimal valorTotal;
    private List<ItemNFeDTO> itens;

    public NFeResponseDTO() {
    }

    public NFeResponseDTO(Long id, String chaveAcesso, String numero, OffsetDateTime data, EmpresaDTO empresa, BigDecimal valorTotal, List<ItemNFeDTO> itens) {
        this.id = id;
        this.chaveAcesso = chaveAcesso;
        this.numero = numero;
        this.data = data;
        this.empresa = empresa;
        this.valorTotal = valorTotal;
        this.itens = itens;
    }

    public static NFeResponseDTO fromEntity(NFe nfe) {
        if (nfe == null) return null;
        List<ItemNFeDTO> itemDTOs = nfe.getItens() != null ?
                nfe.getItens().stream().map(ItemNFeDTO::fromEntity).collect(Collectors.toList()) : List.of();

        return new NFeResponseDTO(
                nfe.getId(),
                nfe.getChaveAcesso(),
                nfe.getNumero(),
                nfe.getData(),
                EmpresaDTO.fromEntity(nfe.getEmpresa()),
                nfe.getValorTotal(),
                itemDTOs
        );
    }

    public Long getId() {
        return id;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public String getNumero() {
        return numero;
    }

    public OffsetDateTime getData() {
        return data;
    }

    public EmpresaDTO getEmpresa() {
        return empresa;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemNFeDTO> getItens() {
        return itens;
    }
}
