package br.edu.agendapsi.data.local

import androidx.room.TypeConverter
import br.edu.agendapsi.data.model.Modalidade
import br.edu.agendapsi.data.model.StatusAgendamento

class Converters {
    @TypeConverter fun statusParaTexto(v: StatusAgendamento): String = v.name
    @TypeConverter fun textoParaStatus(v: String): StatusAgendamento = StatusAgendamento.valueOf(v)
    @TypeConverter fun modalidadeParaTexto(v: Modalidade): String = v.name
    @TypeConverter fun textoParaModalidade(v: String): Modalidade = Modalidade.valueOf(v)
}
