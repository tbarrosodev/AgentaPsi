# AgendaPsi

Projeto acadêmico Android em Kotlin e Jetpack Compose. Telas implementadas: Início e Agenda.

## Abrir e executar

1. Abra esta pasta `AgendaPsi` no Android Studio.
2. Aguarde o Gradle Sync e instale o SDK exigido pelo projeto (API 37, minor 1).
3. Execute em um emulador ou celular com API 26 ou superior.

A configuração atual do daemon Gradle usa JDK 25, conforme `gradle/gradle-daemon-jvm.properties`. O documento de referência indica JDK 21; o grupo deve combinar essa configuração antes de unificar as máquinas.

## Nesta etapa

- Início com resumo diário, próximos atendimentos e filtro por profissional.
- Agenda com seleção de data, dia anterior/seguinte, filtro e lista ordenada, incluindo cancelados.
- Navegação entre Início e Agenda e rotas preparadas para integração das telas dos colegas.
- Novo agendamento encaminha a data e o profissional selecionados. Os destinos ainda não implementados exibem um aviso temporário.
- Quatro previews por tela: conteúdo, vazio, erro e carregamento, todos com as mesmas dimensões.

Os dados são salvos localmente com Room e observados pelos ViewModels de Início e Agenda. A primeira abertura grava dois profissionais, quatro pacientes fictícios e atendimentos de exemplo uma única vez. Datas sem registros mostram a agenda vazia. O repositório compartilhado valida cadastros, conflitos de horário, remarcação, mudanças de situação e arquivamento. Os quatro destinos dos colegas ainda aguardam suas telas.

## Verificação

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Com um aparelho ou emulador conectado, execute também `:app:connectedDebugAndroidTest` para verificar o Room, as operações do repositório e a persistência. Os testes usam bancos próprios, separados do banco do aplicativo.
