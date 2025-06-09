package com.clienteapi.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.clienteapi.backend.dto.ClienteDTO;
import com.clienteapi.backend.dto.EnderecoDTO;
import com.clienteapi.backend.dto.TelefoneDTO;
import com.clienteapi.backend.model.Cliente;
import com.clienteapi.backend.model.Telefone;
import com.clienteapi.backend.repository.ClienteRepository;
import com.clienteapi.backend.repository.TelefoneRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private TelefoneRepository telefoneRepo;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    public void atualizar_deveLancarExcecao_quandoClienteNaoEncontrado() {
        Long id = 1L;
        ClienteDTO dto = new ClienteDTO();

        Mockito.when(clienteRepo.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> clienteService.atualizar(id, dto));
        assertEquals("Cliente não encontrado", ex.getMessage());
    }

    @Test
    public void atualizar_deveLancarExcecao_quandoCpfJaCadastradoParaOutroCliente() {
        Long id = 1L;
        Cliente existente = new Cliente();
        existente.setId(id);
        existente.setCpf("12345678900");
        existente.setNome("Cliente Original");

        ClienteDTO dto = new ClienteDTO();
        dto.setCpf("98765432100");
        dto.setNome("Cliente Original");

        Mockito.when(clienteRepo.findById(id)).thenReturn(Optional.of(existente));
        Mockito.when(clienteRepo.existsByCpf("98765432100")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> clienteService.atualizar(id, dto));
        assertEquals("CPF já cadastrado para outro cliente.", ex.getMessage());
    }

    @Test
    public void atualizar_deveLancarExcecao_quandoNomeJaCadastradoParaOutroCliente() {
        Long id = 1L;
        Cliente existente = new Cliente();
        existente.setId(id);
        existente.setCpf("12345678900");
        existente.setNome("Nome Original");

        ClienteDTO dto = new ClienteDTO();
        dto.setCpf("12345678900");
        dto.setNome("Novo Nome");

        Mockito.when(clienteRepo.findById(id)).thenReturn(Optional.of(existente));
        Mockito.when(clienteRepo.existsByNome("Novo Nome")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> clienteService.atualizar(id, dto));
        assertEquals("Nome já cadastrado para outro cliente.", ex.getMessage());
    }

    @Test
    public void atualizar_deveLancarExcecao_quandoTelefoneJaEmUso() {
        Long id = 1L;

        // Cliente existente que será atualizado
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(id);
        clienteExistente.setCpf("12345678900");
        clienteExistente.setNome("Cliente");
        clienteExistente.setTelefones(new ArrayList<>());
        clienteExistente.setEnderecos(new ArrayList<>());

        // DTO com o telefone que está em uso por outro cliente
        ClienteDTO dto = new ClienteDTO();
        dto.setCpf("12345678900");
        dto.setNome("Cliente");

        TelefoneDTO telefoneDTO = new TelefoneDTO();
        telefoneDTO.setNumero("99999-9999");  // telefone com máscara

        dto.setTelefones(List.of(telefoneDTO));
        dto.setEnderecos(new ArrayList<>());

        // Outro cliente que "possui" o telefone que está em uso
        Cliente outroCliente = new Cliente();
        outroCliente.setId(999L);  // id diferente do cliente que está sendo atualizado

        // Telefone que já existe no banco, associado ao outro cliente
        Telefone telefoneExistente = new Telefone();
        telefoneExistente.setNumero("999999999"); // número sem máscara
        telefoneExistente.setCliente(outroCliente);

        // Mock das chamadas do repositório
        Mockito.when(clienteRepo.findById(id)).thenReturn(Optional.of(clienteExistente));
        Mockito.when(telefoneRepo.findByNumero("999999999")).thenReturn(telefoneExistente);

        // Executa e verifica se a exceção foi lançada corretamente
        RuntimeException ex = assertThrows(RuntimeException.class, () -> clienteService.atualizar(id, dto));
        assertEquals("Telefone 9 9999-9999 já em uso.", ex.getMessage());
    }


    @Test
    public void atualizar_deveAtualizarCliente_quandoDadosValidos() {
        Long id = 1L;
        Cliente existente = new Cliente();
        existente.setId(id);
        existente.setCpf("12345678900");
        existente.setNome("Nome Antigo");
        existente.setTelefones(new ArrayList<>());
        existente.setEnderecos(new ArrayList<>());

        ClienteDTO dto = new ClienteDTO();
        dto.setCpf("98765432100");
        dto.setNome("Nome Atualizado");

        TelefoneDTO telefoneDTO = new TelefoneDTO();
        telefoneDTO.setNumero("99999-9999");

        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setLogradouro("Rua A");
        enderecoDTO.setNumero("123");
        enderecoDTO.setComplemento("Apto 1");
        enderecoDTO.setBairro("Centro");
        enderecoDTO.setCidade("Cidade X");
        enderecoDTO.setEstado("UF");
        enderecoDTO.setCep("12345-678");

        dto.setTelefones(List.of(telefoneDTO));
        dto.setEnderecos(List.of(enderecoDTO));

        Mockito.when(clienteRepo.findById(id)).thenReturn(Optional.of(existente));
        Mockito.when(clienteRepo.existsByCpf("98765432100")).thenReturn(false);
        Mockito.when(clienteRepo.existsByNome("Nome Atualizado")).thenReturn(false);
        Mockito.when(telefoneRepo.existsByNumero("999999999")).thenReturn(false);
        Mockito.when(clienteRepo.save(Mockito.any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteDTO atualizado = clienteService.atualizar(id, dto);

        assertNotNull(atualizado);
        assertEquals("Nome Atualizado", atualizado.getNome());
        assertEquals("987.654.321-00", atualizado.getCpf());
        assertEquals(1, atualizado.getTelefones().size());
        assertEquals("9 9999-9999", atualizado.getTelefones().get(0).getNumero());
        assertEquals(1, atualizado.getEnderecos().size());
        assertEquals("Rua A", atualizado.getEnderecos().get(0).getLogradouro());
    }
}
