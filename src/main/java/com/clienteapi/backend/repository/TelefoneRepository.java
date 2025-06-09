/**
 * Autor: Marco Ezequiel Cedro Barros Borges
 * Data: 8 de jun. de 2025
 */

package com.clienteapi.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clienteapi.backend.model.Telefone;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
    boolean existsByNumero(String numero);
    Telefone findByNumero(String numero); // ou Optional<Telefone> ...


}
