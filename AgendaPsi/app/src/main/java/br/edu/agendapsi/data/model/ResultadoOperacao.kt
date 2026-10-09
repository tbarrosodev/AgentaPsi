package br.edu.agendapsi.data.model

sealed interface ResultadoOperacao {
    data class Sucesso(val id: Long) : ResultadoOperacao
    data class Erro(val mensagem: String) : ResultadoOperacao
}
