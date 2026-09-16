package br.com.nfe.manager.service;

import br.com.nfe.manager.dto.NFeFilterDTO;
import br.com.nfe.manager.exception.NFeDuplicadaException;
import br.com.nfe.manager.exception.ResourceNotFoundException;
import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;
import br.com.nfe.manager.parser.NFeXmlParser;
import br.com.nfe.manager.repository.NFeRepository;
import br.com.nfe.manager.repository.specification.NFeSpecification;
import org.w3c.dom.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class NFeService {

    private final NFeRepository nfeRepository;
    private final EmpresaService empresaService;
    private final ProdutoService produtoService;
    private final NFeXmlParser xmlParser;

    public NFeService(NFeRepository nfeRepository,
                      EmpresaService empresaService,
                      ProdutoService produtoService) {
        this.nfeRepository = nfeRepository;
        this.empresaService = empresaService;
        this.produtoService = produtoService;
        this.xmlParser = new NFeXmlParser();
    }

    @Transactional
    public NFe importarXml(String xmlContent) {
        Document document = xmlParser.carregarXmlFromString(xmlContent);
        return processarEPersistirNFe(document, xmlContent);
    }

    @Transactional
    public NFe importarXml(InputStream inputStream, String rawXmlString) {
        Document document = xmlParser.carregarXml(inputStream);
        return processarEPersistirNFe(document, rawXmlString);
    }

    private NFe processarEPersistirNFe(Document document, String xmlOriginal) {
        String chaveAcesso = xmlParser.extrairChaveAcesso(document);

        // 1. Verificação de Nota Fiscal Duplicada
        if (nfeRepository.existsByChaveAcesso(chaveAcesso)) {
            throw new NFeDuplicadaException(chaveAcesso);
        }

        // 2. Extração do objeto NFe do XML
        NFe nfeParsed = xmlParser.extrairNFe(document);

        // 3. Obtenção ou cadastro da Empresa emitente
        Empresa empresaPersistida = empresaService.obterOuCriar(
                nfeParsed.getEmpresa().getCnpj(),
                nfeParsed.getEmpresa().getNome()
        );

        // 4. Montagem do objeto NFe com relacionamentos persistidos
        NFe nfeEntity = new NFe(
                nfeParsed.getChaveAcesso(),
                nfeParsed.getNumero(),
                nfeParsed.getData(),
                empresaPersistida,
                nfeParsed.getValorTotal(),
                null
        );

        // 5. Associação dos produtos e itens de nota
        for (ItemNFe itemParsed : nfeParsed.getItens()) {
            Produto produtoPersistido = produtoService.obterOuCriar(
                    itemParsed.getProduto().getCodigo(),
                    itemParsed.getProduto().getNome()
            );

            ItemNFe itemEntity = new ItemNFe(
                    produtoPersistido,
                    itemParsed.getQuantidade(),
                    itemParsed.getPrecoUnitario(),
                    itemParsed.getValorTotal()
            );
            nfeEntity.addItem(itemEntity);
        }

        // 6. Armazenamento do XML original
        nfeEntity.setXmlOriginal(xmlOriginal);

        // 7. Persistência final no repositório JPA
        return nfeRepository.save(nfeEntity);
    }

    @Transactional(readOnly = true)
    public Page<NFe> pesquisar(NFeFilterDTO filter, Pageable pageable) {
        return nfeRepository.findAll(NFeSpecification.comFiltros(filter), pageable);
    }

    @Transactional(readOnly = true)
    public NFe buscarPorId(Long id) {
        return nfeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota fiscal não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public NFe buscarPorChaveAcesso(String chaveAcesso) {
        return nfeRepository.findByChaveAcesso(chaveAcesso)
                .orElseThrow(() -> new ResourceNotFoundException("Nota fiscal não encontrada com a chave de acesso: " + chaveAcesso));
    }

    @Transactional(readOnly = true)
    public String obterXmlOriginal(Long id) {
        NFe nfe = buscarPorId(id);
        if (nfe.getXmlOriginal() == null || nfe.getXmlOriginal().isEmpty()) {
            throw new ResourceNotFoundException("Conteúdo XML original não está disponível para a nota fiscal ID: " + id);
        }
        return nfe.getXmlOriginal();
    }
}
