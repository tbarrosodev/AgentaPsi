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

Os registros ainda são fictícios e ficam em memória. A amostra é criada para o dia atual e compartilhada entre as duas telas. Datas sem registros mostram a agenda vazia. Room, ViewModel e gravação serão integrados em outra etapa; carregamento e erro estão disponíveis nos componentes e previews, mas a fonte fixa não produz esses estados em execução.

## Verificação

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```
