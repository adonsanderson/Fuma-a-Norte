# Plano de Execução e Tarefas — Fumaça Norte

Este documento descreve o roadmap sequencial do projeto **Fumaça Norte**, estruturado em **6 Marcos** segundo a metodologia **Spec-Driven Development (SDD)**.

---

## Marco 1 — Fundação e Documentação SDD
- [x] Definir visão geral, requisitos funcionais (RF01–RF06) e não funcionais (RNF01–RNF05) em `docs/SPECIFICATION.md`.
- [x] Definir Clean Architecture, Regra de Dependência, UDF, estratégia Offline-First e estrutura de pacotes em `docs/ARCHITECTURE.md`.
- [x] Estruturar o plano de execução completo em 6 Marcos em `docs/TASKS.md`.
- [x] Criar a versão inicial do `README.md` com documentação completa do projeto.
- [ ] Revisar e aprovar os documentos SDD antes de iniciar a implementação de código.

---

## Marco 2 — Configuração Base, Gradle e Camada Domain (Kotlin Puro)
- [ ] Configurar `gradle/libs.versions.toml` e `app/build.gradle.kts` com Koin, Room, Retrofit, Moshi, Kotlinx Serialization, Navigation Compose, MockK e Turbine.
- [ ] Configurar `AndroidManifest.xml` (permissão `INTERNET`, `FumacaNorteApplication`) e metadados do app (`Fumaça Norte`).
- [ ] Criar os modelos puros de domínio (`CurrentWeather`, `HourlyForecast`, `WeatherSnapshot`, `WeatherCondition`, `DomainError`) sem nenhuma dependência de Android/Room/Retrofit.
- [ ] Criar a interface `WeatherRepository` na camada `domain.repository`.
- [ ] Implementar os casos de uso `ObserveManausWeatherUseCase`, `RefreshManausWeatherUseCase` e `ObserveHourlyDetailUseCase`.
- [ ] Escrever testes unitários da camada `domain` utilizando JUnit 4, MockK, Turbine e `kotlinx-coroutines-test`.
- [ ] Validar compilação e aprovação de 100% dos testes unitários do Marco 2.

---

## Marco 3 — Camada Data (Retrofit, Room e Política Offline-First)
- [ ] Implementar `OpenMeteoApiService` e `OpenMeteoResponseDto` para consumo do endpoint `v1/forecast` (Manaus: `latitude=-3.10`, `longitude=-60.02`).
- [ ] Implementar `WeatherRemoteDataSource` com tratamento explícito de erros de rede, HTTP e timeout.
- [ ] Implementar entidades Room (`CurrentWeatherEntity`, `HourlyForecastEntity`, `WeatherWithHourlyRelation`), `WeatherDao` (com `@Transaction` e `Flow`) e `FumacaNorteDatabase`.
- [ ] Implementar funções de mapeamento puro em `WeatherMappers.kt` (`DTO -> Entity`, `Entity -> Domain`).
- [ ] Implementar `WeatherRepositoryImpl` aplicando o padrão Offline-First (Room como Single Source of Truth).
- [ ] Configurar os módulos Koin da camada de dados (`NetworkModule`, `DatabaseModule`, `DataModule`).
- [ ] Escrever testes unitários para os mappers, data sources e `WeatherRepositoryImpl` (cenários: online com sucesso, offline com cache, offline sem cache, preservação de cache em erro de rede) usando MockK e Turbine.
- [ ] Validar compilação e aprovação de 100% dos testes unitários do Marco 3.

---

## Marco 4 — Camada Presentation (ViewModels, UDF e UI Jetpack Compose)
- [ ] Configurar tema Material Design 3 (`Color.kt`, `Type.kt`, `Theme.kt`) e strings localizadas em `res/values/strings.xml`.
- [ ] Implementar `WeatherUiState` (`Loading`, `Success`, `Empty`, `Error`), `WeatherUiEvent` e `WeatherViewModel` com `StateFlow` e UDF.
- [ ] Implementar `HourlyDetailUiState` e `HourlyDetailViewModel` para visualização de detalhes horários.
- [ ] Configurar `DomainModule`, `PresentationModule` e inicializar Koin em `FumacaNorteApplication`.
- [ ] Implementar tela principal `WeatherDashboardScreen` com resumo atual de Manaus, indicador de status offline/última atualização, Pull-to-Refresh e lista horária otimizada.
- [ ] Implementar tela de detalhes `HourlyDetailScreen` com métricas climáticas completas do horário selecionado.
- [ ] Implementar navegação tipada com Navigation Compose (`FumacaNorteNavGraph` e `Routes`), `enableEdgeToEdge()`, acessibilidade e `testTag`s.
- [ ] Escrever testes unitários para `WeatherViewModel` e `HourlyDetailViewModel` com MockK e Turbine cobrindo todos os estados (`Loading`, `Success`, `Empty`, `Error`) e atualização manual.
- [ ] Validar compilação e aprovação de 100% dos testes unitários do Marco 4.

---

## Marco 5 — Polimento Visual, Acessibilidade, Assets e Testes de Integração Local
- [ ] Criar ícone adaptativo customizado do aplicativo e recursos visuais de polimento.
- [ ] Revisar adaptatividade para telas compactas, médias e expandidas (Window Size Classes / `widthIn`).
- [ ] Verificar conformidade de acessibilidade (`contentDescription`, contraste, alvos de toque >= `48.dp`) e ausência de recomposições desnecessárias.
- [ ] Revisar tratamento de erros transitórios, estado `Empty` e comportamento offline em múltiplas rotações/ciclos de vida.
- [ ] Adicionar testes complementares (incluindo Robolectric para fluxos críticos de UI/persistência, se aplicável) e garantir execução integral da suíte de testes.

---

## Marco 6 — Validação Final, Auditoria SDD e Documentação Definitiva
- [ ] Auditar conformidade rigorosa com a Regra de Dependência (garantir zero imports de Android/Room/Retrofit no pacote `domain`).
- [ ] Verificar aderência a todos os requisitos RF01–RF06 e RNF01–RNF05 descritos em `docs/SPECIFICATION.md`.
- [ ] Executar `gradle :app:testDebugUnitTest` e compilação completa do APK de debug sem erros ou warnings críticos.
- [ ] Atualizar `docs/TASKS.md` marcando todas as entregas concluídas.
- [ ] Atualizar `README.md` com instruções finais, resultados dos testes, seção de evidências e limitações conhecidas.
