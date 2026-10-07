# AgendaPsi — Tela 1

Primeira tela do projeto acadêmico: Início, em Kotlin e Jetpack Compose Material 3.

## Abrir no Android Studio

1. Use **File → Open** e selecione esta pasta `AgendaPsi` (a pasta que contém `settings.gradle.kts`).
2. Configure o **Gradle JDK como 21** e aguarde o Gradle Sync.
3. Instale pelo SDK Manager a plataforma exigida pelo `compileSdk` (API 37, minor 1, conforme o documento fornecido).
4. Execute em um emulador ou celular com Android 8.0/API 26 ou superior.

As versões de Gradle, AGP, Compose, Activity e Lifecycle seguem a base do documento. O plugin Compose deve estar alinhado ao compilador Kotlin integrado ao AGP; caso a sincronização detecte divergência, alinhe a versão antes de executar.

## O que funciona nesta entrega

- Data atual no fuso America/Sao_Paulo, atualizada ao voltar ao app e a cada minuto.
- Filtro por profissional, preservado ao recriar a Activity.
- Total diário, incluindo cancelados, e quatro indicadores derivados dos mesmos registros.
- Próximos três atendimentos pendentes ainda não iniciados, em ordem de horário.
- Identificação por ID, modalidade, profissional e situação por extenso.
- Tema claro, margens de 16 dp, cartões de 12 dp e botões de 8 dp.
- Conteúdo rolável e indicadores em uma coluna quando necessário para fonte ampliada.
- Componentes dos estados carregando, erro com callback de nova tentativa e vazio.

**Esta etapa usa dados fictícios em memória.** Eles são gerados para o dia atual; não há gravação, cadastro nem conexão Room ainda. Depois das 17h, a lista de próximos pode estar vazia mesmo com registros no resumo. Isso é esperado.

Agenda, Pacientes, Novo agendamento e Detalhes exibem uma mensagem informando que serão implementados depois. Os callbacks já estão separados para conectar a navegação. A tela não declara sucesso de uma ação ainda inexistente.

## Arquivos para estudar

- `MainActivity.kt`: inicia o tema e conecta os dados de demonstração à tela; atualiza o relógio conforme o ciclo de vida.
- `ui/inicio/InicioScreen.kt`: desenha a interface e oferece previews normal, fonte ampliada, vazio, erro e carregando.
- `ui/inicio/InicioState.kt`: modelos e função `resumirDia`, sem dependência de Android.
- `ui/inicio/InicioIcons.kt`: ícones vetoriais locais.
- `ui/theme/Theme.kt`: cores, formas e tipografia centralizadas.
- `app/src/test/.../ResumoDiaTest.kt`: testes de totais, filtro, ordenação e limites de horário.

Abra `InicioScreen.kt` e selecione **Split** ou **Design** para ver os previews após sincronizar o projeto.

## Integração futura

O ViewModel deve observar o Room e converter o resultado em `InicioState`. Entregue o estado à `InicioScreen` e conecte os callbacks às rotas. Remova a fonte `atendimentosDemo` da execução normal. Reutilize o enum de situação do modelo definitivo em vez de manter duas definições. O estado de erro está disponível na interface, mas não é produzido pela fonte fixa de demonstração.

## Verificação

Verificação nesta entrega: a tentativa com JDK 21 não chegou à compilação. O modo offline falhou porque faltam no cache os artefatos `gradle:9.3.1`, `builder:9.3.1` e `protos:32.3.1`. A tentativa online foi interrompida após permanecer na preparação do Gradle. Portanto, APK, testes automatizados e previews renderizados ainda não foram validados. Execute o Gradle Sync com acesso aos repositórios para baixar as dependências.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

No emulador, alterne os profissionais, aumente a fonte, gire a tela e confira os atalhos. TalkBack e layout renderizado ainda exigem validação no dispositivo; previews declarados no código não equivalem a testes executados.

Referência dos componentes: [Navigation bar — Android Developers](https://developer.android.com/develop/ui/compose/components/navigation-bar).
