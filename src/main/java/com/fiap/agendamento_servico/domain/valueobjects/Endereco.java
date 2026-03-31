package com.fiap.agendamento_servico.domain.valueobjects;

import java.util.regex.Pattern;

public record Endereco(
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String cidade
) {
    private static final Pattern PADRAO_CEP = Pattern.compile("^\\d{5}(-|\\.)\\d{3}$|^\\d{8}$");

    public Endereco {
        validarCEP(cep);
    }

    private static void validarCEP(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new IllegalArgumentException("CEP não pode ser nulo ou vazio");
        }

        if (!PADRAO_CEP.matcher(cep).matches()) {
            throw new IllegalArgumentException(
                    String.format("CEP inválido: '%s'. Utilize o formato XXXXX-XXX, XXXXX.XXX ou XXXXXXXX", cep)
            );
        }
    }

    public String getCepFormatado() {
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        if (cepLimpo.length() == 8) {
            return cepLimpo.substring(0, 5) + "-" + cepLimpo.substring(5);
        }
        return cep;
    }

    @Override
    public String toString() {
        return String.format("%s, %s %s - %s, %s %s", 
                rua, numero, 
                (complemento != null && !complemento.isBlank()) ? complemento : "", 
                bairro, cidade, getCepFormatado()).replaceAll("\\s+", " ");
    }
}
