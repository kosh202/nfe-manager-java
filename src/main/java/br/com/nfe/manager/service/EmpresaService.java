package br.com.nfe.manager.service;

import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Empresa> buscarPorCnpj(String cnpj) {
        return empresaRepository.findByCnpj(cnpj);
    }

    @Transactional
    public Empresa obterOuCriar(String cnpj, String nome) {
        return empresaRepository.findByCnpj(cnpj)
                .map(empresaExistente -> {
                    if (nome != null && !nome.equals(empresaExistente.getNome())) {
                        empresaExistente.setNome(nome);
                        return empresaRepository.save(empresaExistente);
                    }
                    return empresaExistente;
                })
                .orElseGet(() -> empresaRepository.save(new Empresa(cnpj, nome)));
    }
}
