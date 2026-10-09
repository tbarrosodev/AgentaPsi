package br.edu.agendapsi.ui.inicio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.agendapsi.data.repository.ClinicaRepository
import br.edu.agendapsi.ui.components.paraApresentacao
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class InicioUiState(
    val dia: LocalDate,
    val profissionalId: Long? = null,
    val profissionais: List<Profissional> = emptyList(),
    val conteudo: InicioState = InicioState.Carregando
)

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(
    private val repository: ClinicaRepository,
    private val clock: Clock,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val agora = MutableStateFlow(LocalDateTime.now(clock))
    private val profissional = savedState.getStateFlow<Long?>("profissionalId", null)
    private val tentativa = MutableStateFlow(0)

    val state: StateFlow<InicioUiState> = combine(
        agora.map { it.toLocalDate() }.distinctUntilChanged(), profissional, tentativa
    ) { dia, id, _ -> dia to id }.flatMapLatest { (dia, id) ->
        combine(repository.observarAgenda(dia.toEpochDay(), id), repository.observarProfissionais(), agora) {
            registros, catalogo, horario ->
            InicioUiState(dia, id, catalogo.map { Profissional(it.id, it.nome) },
                InicioState.Disponivel(resumirDia(registros.map { it.paraApresentacao() }, horario, id)))
        }.onStart { emit(InicioUiState(dia, id)) }
            .catch { erro ->
                if (erro is CancellationException) throw erro
                emit(InicioUiState(dia, id, conteudo = InicioState.Erro))
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState(agora.value.toLocalDate()))

    fun selecionarProfissional(id: Long?) { savedState["profissionalId"] = id }
    fun atualizarRelogio() { agora.value = LocalDateTime.now(clock) }
    fun tentarNovamente() { atualizarRelogio(); tentativa.value++ }

    companion object {
        fun factory(repository: ClinicaRepository, clock: Clock) = viewModelFactory {
            initializer { InicioViewModel(repository, clock, createSavedStateHandle()) }
        }
    }
}
