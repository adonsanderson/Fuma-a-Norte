# Fumaça Norte

Aplicativo Android nativo de monitoramento meteorológico **Offline-First** focado em **Manaus (AM)**, construído com **Kotlin**, **Jetpack Compose**, **Clean Architecture** e **Spec-Driven Development (SDD)**.

---

## Objetivo

Oferecer uma interface rápida, clara e resiliente a quedas de conectividade para acompanhamento das condições climáticas atuais e previsão horária de Manaus (`latitude = -3.10`, `longitude = -60.02`), garantindo que os dados mais recentes permaneçam sempre acessíveis localmente mesmo quando o dispositivo estiver offline.

---

## Funcionalidades Implementadas

- **Monitoramento Climático de Manaus (RF01):** Exibição de temperatura atual, sensação térmica, umidade relativa, precipitação, velocidade do vento e condição climática (padrão WMO).
- **Persistência Local Automática (RF02):** Armazenamento transacional do último snapshot meteorológico e previsões horárias em SQLite via Room (`@Transaction replaceWeatherData`).
- **Operação Offline-First (RF03):** Leitura reativa contínua a partir do banco local como *Single Source of Truth*, preservando dados em caso de indisponibilidade de rede e sinalizando o status via banner não obstrutivo.
- **Atualização Manual (RF04):** Suporte a *Pull-to-Refresh* (`PullToRefreshBox`) e botão de recarregamento na barra superior com proteção contra requisições duplicadas.
- **Detalhes Horários (RF05):** Listagem horária com navegação tipada (`Navigation Compose`) para visualização detalhada de cada faixa de horário prevista.
- **Gestão Explícita de Estados de UI (RF06):** Tratamento visual dedicado para os estados `Loading`, `Success`, `Empty` e `Error`, com layout adaptativo (`BoxWithConstraints`) para telas compactas e expandidas.

---

## Tecnologias e Bibliotecas

- **Linguagem:** Kotlin (`2.2.10`)
- **Interface:** Jetpack Compose + Material Design 3 + Navigation Compose (`2.8.9`) + Fontes locais (`Space Grotesk` e `Plus Jakarta Sans`)
- **Assincronismo:** Kotlin Coroutines & Flow / StateFlow (`1.10.2`)
- **Injeção de Dependências:** Koin (`4.0.2`)
- **Rede:** Retrofit (`2.12.0`) + OkHttp (`4.10.0`) + Moshi (`1.15.2` com codegen KSP)
- **Persistência Local:** Room (`2.7.0` com KSP)
- **Testes Unitários e Locais:** JUnit 4 + MockK (`1.13.16`) + Turbine (`1.2.0`) + `kotlinx-coroutines-test` + Robolectric (`4.16.1`)

---

## API REST Utilizada

- **Serviço:** [Open-Meteo Weather Forecast API](https://open-meteo.com/en/docs)
- **URL Base:** `https://api.open-meteo.com/`
- **Endpoint:** `GET v1/forecast`
- **Parâmetros de Localização:** `latitude=-3.10&longitude=-60.02&timezone=America/Manaus&forecast_days=2`
- **Autenticação:** Não requer chave de API (*API Key*) para uso público não comercial.

---

## Arquitetura

O projeto segue rigorosamente a **Clean Architecture** associada ao padrão **MVVM** e **Unidirectional Data Flow (UDF)**:

- **`domain`**: Regras de negócio, modelos puros (`WeatherSnapshot`, `CurrentWeather`, `HourlyForecast`, `WeatherCondition`, `DomainError`, `DomainResult`), interface `WeatherRepository` e casos de uso (`ObserveManausWeatherUseCase`, `RefreshManausWeatherUseCase`, `ObserveHourlyDetailUseCase`). Totalmente livre de dependências Android, Room, Retrofit, Moshi ou Koin.
- **`data`**: Fontes de dados remota (`OpenMeteoApiService`, `WeatherRemoteDataSourceImpl`) e local (`FumacaNorteDatabase`, `WeatherDao`), DTOs, Entities, Mappers (`WeatherMappers.kt`) e `WeatherRepositoryImpl`.
- **`presentation`**: Telas Jetpack Compose (`WeatherDashboardScreen`, `HourlyDetailScreen`), componentes reutilizáveis, `WeatherUiState` / `HourlyDetailUiState` imutáveis, `ViewModel`s expondo `StateFlow` e navegação tipada (`FumacaNorteNavGraph`).

Mais detalhes em [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) e [`docs/SPECIFICATION.md`](docs/SPECIFICATION.md).

---

## Estratégia Offline-First

1. O **Room** atua como **Single Source of Truth (SSOT)**. A camada de apresentação observa exclusivamente fluxos (`Flow`) emitidos pelo banco local.
2. Quando uma sincronização é solicitada (abertura do app, botão de atualizar ou *Pull-to-Refresh*), o repositório consulta a API Open-Meteo em `Dispatchers.IO`.
3. Em caso de sucesso, os dados antigos são substituídos atomicamente dentro de uma `@Transaction` no Room, disparando automaticamente a atualização da UI.
4. Em caso de erro de rede ou ausência de internet, o cache local é preservado integralmente e a UI continua funcional indicando o horário da última atualização válida e um banner dispensável (`SyncStatusBanner`).

---

## Instruções de Configuração e Execução

### Pré-requisitos
- Android Studio (versão recomendada: Ladybug ou superior)
- JDK 11 ou superior
- Android SDK com `compileSdk` 36 e `minSdk` 24

### Passos para Execução
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/fumaca-norte-android.git
   cd fumaca-norte-android
   ```
2. Abra o projeto no Android Studio e aguarde a sincronização do Gradle.
3. Não é necessário configurar chaves de API em `local.properties` ou variáveis de ambiente, pois a API Open-Meteo é pública.
4. Gere o APK de debug ou execute em um dispositivo/emulador:
   ```bash
   ./gradlew assembleDebug
   ```
   *(Em ambientes com `gradle` global instalado sem wrapper: `gradle :app:assembleDebug`)*

---

## Instruções de Testes

A suíte completa de testes unitários e de integração local (Domain, Data, ViewModels, Room in-memory e Compose UI via Robolectric) executa na JVM sem necessidade de emulador:

```bash
./gradlew :app:testDebugUnitTest
```
*(Ou `gradle :app:testDebugUnitTest`)*

### Cobertura Implementada
- **Domain:** `WeatherConditionTest`, `ObserveManausWeatherUseCaseTest`, `RefreshManausWeatherUseCaseTest`, `ObserveHourlyDetailUseCaseTest`
- **Data:** `WeatherMappersTest`, `WeatherRemoteDataSourceImplTest`, `WeatherRepositoryImplTest`, `FumacaNorteDatabaseRobolectricTest`
- **Presentation & UI:** `WeatherViewModelTest`, `HourlyDetailViewModelTest`, `WeatherScreensRobolectricTest`

---

## Evidências

- **Status do Projeto:** Todos os 6 Marcos do plano SDD (`docs/TASKS.md`) concluídos.
- **Compilação (`assembleDebug`):** Validada com sucesso (incluindo KSP para Room e Moshi).
- **Suíte de Testes (`:app:testDebugUnitTest`):** 39 testes executados localmente na JVM com **100% de aprovação** (MockK, Turbine, `kotlinx-coroutines-test` e Robolectric).
- **Auditoria Clean Architecture:** Verificada ausência total de dependências Android/Room/Retrofit no pacote `com.example.fumacanorte.domain`.

---

## Limitações Conhecidas

- **Localidade Fixa:** Nesta versão, o monitoramento é focado exclusivamente nas coordenadas geográficas de Manaus (`-3.10, -60.02`), sem seletor dinâmico de múltiplas cidades ou uso de GPS.
- **Conectividade Inicial:** Na primeira execução com banco local vazio, é necessário acesso à internet ao menos uma vez para popular o cache inicial do Room.
- **Escopo de Qualidade do Ar:** Embora o nome do projeto remeta ao monitoramento regional da região Norte ("Fumaça Norte"), esta versão consome exclusivamente variáveis meteorológicas do endpoint `v1/forecast` da Open-Meteo.

