/**
 * Autor: Marco Ezequiel Cedro Barros Borges
 * Data: 8 de jun. de 2025
 */

package com.clienteapi.backend.dto;

import com.clienteapi.backend.util.FormatUtil;

public class TelefoneDTO {

    private String numero;

    public String getNumero() {
    	 return FormatUtil.formatarTelefone(this.numero);
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
