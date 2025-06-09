/**
 * Autor: Marco Ezequiel Cedro Barros Borges
 * Data: 8 de jun. de 2025
 */

package com.clienteapi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clienteapi.backend.model.Endereco;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
}
