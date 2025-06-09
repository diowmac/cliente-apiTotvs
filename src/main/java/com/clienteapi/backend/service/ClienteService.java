package com.clienteapi.backend.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.clienteapi.backend.dto.ClienteDTO;
import com.clienteapi.backend.dto.EnderecoDTO;
import com.clienteapi.backend.dto.TelefoneDTO;
import com.clienteapi.backend.model.Cliente;
import com.clienteapi.backend.model.Endereco;
import com.clienteapi.backend.model.Telefone;
import com.clienteapi.backend.repository.ClienteRepository;
import com.clienteapi.backend.repository.TelefoneRepository;

import jakarta.transaction.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepo;
    private final TelefoneRepository telefoneRepo;

    public ClienteService(ClienteRepository clienteRepo, TelefoneRepository telefoneRepo) {
        this.clienteRepo = clienteRepo;
        this.telefoneRepo = telefoneRepo;
    }

    @Transactional
    public ClienteDTO salvar(ClienteDTO dto) {
    	
    	if (dto.getCpf() == null || dto.getCpf().trim().isEmpty()) {
    	    throw new RuntimeException("CPF é obrigatório.");
    	}
    	
        if (clienteRepo.existsByCpf(limparMascara(dto.getCpf()))) {
            throw new RuntimeException("CPF já cadastrado.");
        }

        if (clienteRepo.existsByNome(dto.getNome())) {
            throw new RuntimeException("Nome duplicado.");
        }

        Cliente cliente = toEntity(dto);

        for (Telefone tel : cliente.getTelefones()) {
            if (telefoneRepo.existsByNumero(tel.getNumero())) {
                throw new RuntimeException("Telefone já em uso.");
            }
            tel.setCliente(cliente);
        }

        for (Endereco end : cliente.getEnderecos()) {
            end.setCliente(cliente);
        }

        Cliente salvo = clienteRepo.save(cliente);
        return toDTO(salvo);
    }

    public List<ClienteDTO> listarTodos() {
        return clienteRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return toDTO(cliente);
    }

    public void excluir(Long id) {
        clienteRepo.deleteById(id);
    }

    // ========================
    // Conversores DTO ↔ Entity
    // ========================

    private ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setCpf(limparMascara(cliente.getCpf()));
        dto.setNome(cliente.getNome());

        dto.setTelefones(
            cliente.getTelefones().stream().map(t -> {
                TelefoneDTO telDto = new TelefoneDTO();
                telDto.setNumero(limparMascara(t.getNumero()));
                return telDto;
            }).collect(Collectors.toList())
        );

        dto.setEnderecos(
            cliente.getEnderecos().stream().map(e -> {
                EnderecoDTO endDto = new EnderecoDTO();
                endDto.setLogradouro(e.getLogradouro());
                endDto.setNumero(e.getNumero());
                endDto.setComplemento(e.getComplemento());
                endDto.setBairro(e.getBairro());
                endDto.setCidade(e.getCidade());
                endDto.setEstado(e.getEstado());
                endDto.setCep(limparMascara(e.getCep()));
                return endDto;
            }).collect(Collectors.toList())
        );

        return dto;
    }

    private Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setCpf(limparMascara(dto.getCpf()));
        cliente.setNome(dto.getNome());

        // Telefones - evita NPE com verificação null
        if (dto.getTelefones() != null) {
            cliente.setTelefones(
                dto.getTelefones().stream().map(t -> {
                    Telefone tel = new Telefone();
                    tel.setNumero(limparMascara(t.getNumero()));
                    tel.setCliente(cliente);
                    return tel;
                }).collect(Collectors.toList())
            );
        } else {
            cliente.setTelefones(Collections.emptyList());
        }

        // Endereços - evita NPE com verificação null
        if (dto.getEnderecos() != null) {
            cliente.setEnderecos(
                dto.getEnderecos().stream().map(e -> {
                    Endereco end = new Endereco();
                    end.setLogradouro(e.getLogradouro());
                    end.setNumero(e.getNumero());
                    end.setComplemento(e.getComplemento());
                    end.setBairro(e.getBairro());
                    end.setCidade(e.getCidade());
                    end.setEstado(e.getEstado());
                    end.setCliente(cliente);
                    end.setCep(limparMascara(e.getCep()));
                    return end;
                }).collect(Collectors.toList())
            );
        } else {
            cliente.setEnderecos(Collections.emptyList());
        }

        return cliente;
    }

    
    @Transactional
    public ClienteDTO atualizar(Long id, ClienteDTO dto) {
        Cliente clienteExistente = clienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        String cpfLimpo = limparMascara(dto.getCpf());

        if (!clienteExistente.getCpf().equals(cpfLimpo) && clienteRepo.existsByCpf(cpfLimpo)) {
            throw new RuntimeException("CPF já cadastrado para outro cliente.");
        }

        if (!clienteExistente.getNome().equals(dto.getNome()) && clienteRepo.existsByNome(dto.getNome())) {
            throw new RuntimeException("Nome já cadastrado para outro cliente.");
        }

        clienteExistente.setCpf(cpfLimpo);
        clienteExistente.setNome(dto.getNome());

        // Atualiza Telefones
        clienteExistente.getTelefones().clear();
        List<Telefone> telefonesAtualizados = dto.getTelefones().stream().map(t -> {
            String numero = limparMascara(t.getNumero());
            Telefone existente = telefoneRepo.findByNumero(numero);
            if (existente != null && !existente.getCliente().getId().equals(clienteExistente.getId())) {
                throw new RuntimeException("Telefone " + t.getNumero() + " já em uso.");
            }
            Telefone tel = new Telefone();
            tel.setNumero(numero);
            tel.setCliente(clienteExistente);
            return tel;
        }).collect(Collectors.toList());
        clienteExistente.getTelefones().addAll(telefonesAtualizados);

        // Atualiza Endereços
        clienteExistente.getEnderecos().clear();
        List<Endereco> enderecosAtualizados = dto.getEnderecos().stream().map(e -> {
            Endereco end = new Endereco();
            end.setLogradouro(e.getLogradouro());
            end.setNumero(e.getNumero());
            end.setComplemento(e.getComplemento());
            end.setBairro(e.getBairro());
            end.setCidade(e.getCidade());
            end.setEstado(e.getEstado());
            end.setCliente(clienteExistente);
            end.setCep(limparMascara(e.getCep()));
            return end;
        }).collect(Collectors.toList());
        clienteExistente.getEnderecos().addAll(enderecosAtualizados);

        // Salva e retorna DTO atualizado
        Cliente salvo = clienteRepo.save(clienteExistente);
        return toDTO(salvo);
    }


    private String limparMascara(String valor) {
        if (valor == null) return null;
        return valor.replaceAll("\\D", "");
    }
}
