package br.com.nfe.manager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nfe", indexes = {
    @Index(name = "idx_nfe_chave_acesso", columnList = "chave_acesso", unique = true)
})
public class NFe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave_acesso", nullable = false, unique = true, length = 44)
    private String chaveAcesso;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "data", nullable = false)
    private OffsetDateTime data;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @org.hibernate.annotations.BatchSize(size = 25)
    @OneToMany(mappedBy = "nfe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemNFe> itens = new ArrayList<>();

    @Lob
    @Column(name = "xml_original", columnDefinition = "CLOB")
    private String xmlOriginal;

    public NFe() {
    }

    public NFe(String chaveAcesso, String numero, OffsetDateTime data, Empresa empresa, BigDecimal valorTotal, List<ItemNFe> itens) {
        this.chaveAcesso = chaveAcesso;
        this.numero = numero;
        this.data = data;
        this.empresa = empresa;
        this.valorTotal = valorTotal;
        if (itens != null) {
            for (ItemNFe item : itens) {
                addItem(item);
            }
        }
    }

    public void addItem(ItemNFe item) {
        itens.add(item);
        item.setNfe(this);
    }

    public Long getId() {
        return id;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public void setChaveAcesso(String chaveAcesso) {
        this.chaveAcesso = chaveAcesso;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public OffsetDateTime getData() {
        return data;
    }

    public void setData(OffsetDateTime data) {
        this.data = data;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemNFe> getItens() {
        return itens;
    }

    public void setItens(List<ItemNFe> itens) {
        this.itens = itens;
        if (itens != null) {
            for (ItemNFe item : itens) {
                item.setNfe(this);
            }
        }
    }

    public String getXmlOriginal() {
        return xmlOriginal;
    }

    public void setXmlOriginal(String xmlOriginal) {
        this.xmlOriginal = xmlOriginal;
    }
}
