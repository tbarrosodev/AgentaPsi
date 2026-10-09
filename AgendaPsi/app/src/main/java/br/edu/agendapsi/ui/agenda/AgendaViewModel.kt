package br.edu.agendapsi.ui.agenda

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.agendapsi.data.repository.ClinicaRepository
import br.edu.agendapsi.ui.components.paraApresentacao
import br.edu.agendapsi.ui.inicio.Profissional
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class AgendaUiState(
    val dia: LocalDate,
    val profissionalId: Long? = null,
    val profissionais: List<Profissional> = emptyList(),
    val conteudo: AgendaState = AgendaState.Carregando
)

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(
    private val repository: ClinicaRepository,
    clock: Clock,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val dia = savedState.getStateFlow("diaEpoch", LocalDate.now(clock).toEpochDay())
    private val profissional = savedState.getStateFlow<Long?>("profissionalId", null)
    private val tentativa = MutableStateFlow(0)

    val state: StateFlow<AgendaUiState> = combine(dia, profissional, tentativa) { data, id, _ ->
        LocalDate.ofEpochDay(data) to id
    }.flatMapLatest { (data, id) ->
        combine(repository.observarAgenda(data.toEpochDay(), id), repository.observarProfissionais()) { registros, catalogo ->
            AgendaUiState(data, id, catalogo.map { Profissional(it.id, it.nome) },
                AgendaState.Disponivel(registros.map { it.paraApresentacao() }))
        }.onStart { emit(AgendaUiState(data, id)) }
            .catch { erro ->
                if (erro is CancellationException) throw erro
                emit(AgendaUiState(data, id, conteudo = AgendaState.Erro))
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaUiState(LocalDate.ofEpochDay(dia.value)))

    fun selecionarDia(data: LocalDate) { savedState["diaEpoch"] = data.toEpochDay() }
    fun selecionarProfissional(id: Long?) { savedState["profissionalId"] = id }
    fun tentarNovamente() { tentativa.value++ }

    companion object {
        fun factory(repository: ClinicaRepository, clock: Clock) = viewModelFactory {
            initializer { AgendaViewModel(repository, clock, createSavedStateHandle()) }
        }
    }
}
