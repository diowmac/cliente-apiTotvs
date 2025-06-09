/**
 * Autor: Marco Ezequiel Cedro Barros Borges
 * Data: 8 de jun. de 2025
 */

package com.clienteapi.backend.util;

public class FormatUtil {
	
	 public static String formatarCpfCnpj(String cpfCnpj) {
	        if (cpfCnpj == null) return null;
	        String numeros = cpfCnpj.replaceAll("\\D", "");
	        if (numeros.length() == 11) {
	            return numeros.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
	        } else if (numeros.length() == 14) {
	            return numeros.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
	        }
	        return cpfCnpj; // Retorna como veio se não bate com CPF/CNPJ
	    }

	    public static String formatarTelefone(String telefone) {
	        if (telefone == null) return null;
	        String sTel = telefone.replaceAll("\\D", "");
	        boolean bZero = sTel.startsWith("0");
	        if (bZero) sTel = sTel.substring(1);

	        switch (sTel.length()) {
	            case 8:
	                sTel = sTel.replaceFirst("(\\d{4})(\\d{4})", "$1-$2");
	                break;
	            case 9:
	                sTel = sTel.replaceFirst("(\\d)(\\d{4})(\\d{4})", "$1 $2-$3");
	                break;
	            case 10:
	                sTel = sTel.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
	                break;
	            case 11:
	                sTel = sTel.replaceFirst("(\\d{2})(\\d)(\\d{4})(\\d{4})", "($1) $2 $3-$4");
	                break;
	            case 12:
	                sTel = sTel.replaceFirst("(\\d{2})(\\d{2})(\\d{4})(\\d{4})", "$1($2)$3-$4");
	                break;
	            case 13:
	                sTel = sTel.replaceFirst("(\\d{2})(\\d{2})(\\d)(\\d{4})(\\d{4})", "$1($2) $3 $4-$5");
	                break;
	            default:
	                return telefone;
	        }

	        return bZero ? "0" + sTel : sTel;
	    }

	    public static String formatarCep(String cep) {
	        if (cep == null) return null;
	        String numeros = cep.replaceAll("\\D", ""); 
	        if (numeros.length() == 8) {
	            return numeros.replaceFirst("(\\d{5})(\\d{3})", "$1-$2");
	        }
	        return cep; 
	    }

	    
	}
