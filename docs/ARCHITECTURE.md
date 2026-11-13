# Arquitetura de Software — Fumaça Norte

**Projeto:** Fumaça Norte (`fumaca-norte-android`)  
**Padrão Arquitetural:** Clean Architecture + MVVM + UDF + Offline-First  
**Pacote Base:** `com.example.fumacanorte`  

---

## 1. Visão Geral da Clean Architecture

O **Fumaça Norte** adota a **Clean Architecture** combinada ao padrão **MVVM (Model-View-ViewModel)** na camada de apresentação. O objetivo é desacoplar regras de negócio dos detalhes de infraestrutura (banco SQLite, cliente HTTP, framework Android e biblioteca de UI), garantindo manutenibilidade, escalabilidade e testabilidade unitária isolada em JVM.

O código é dividido em três camadas concêntricas e bem delimitadas:

1. **Domain (`domain`)** — Núcleo de regras de negócio, modelos puros, contratos de repositório e casos de uso.
2. **Data (`data`)** — Implementação dos contratos definidos no domínio, orquestração Offline-First, fontes de dados remotas (Retrofit) e locais (Room), além de mappers.
3. **Presentation (`presentation`)** — Telas Jetpack Compose, componentes de UI, estados imutáveis (`UiState`), eventos de usuário e `ViewModel`s com `StateFlow`.

Além dessas camadas, o módulo de inicialização e DI (`di` e `FumacaNorteApplication`) conecta as implementações concretas às abstrações usando **Koin**.

---

## 2. Regra de Dependência

A **Regra de Dependência** estabelece que as dependências de código-fonte devem apontar **exclusivamente de fora para dentro** (em direção à camada `domain`):

```text
+-------------------------------------------------------------------+
|                         PRESENTATION                              |
|  (Jetpack Compose, ViewModels, StateFlow, UiState, Mappers UI)    |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                            DOMAIN                                 |
|   (Modelos Puros, Repository Interfaces, UseCases, Result/Errors) |
|         * ZERO dependências de Android, Room ou Retrofit *        |
+---------------------------------+---------------------------------+
                                  ^
                                  | (implementa interfaces de Domain)
+---------------------------------+---------------------------------+
|                             DATA                                  |
| (RepositoryImpl, Room DAO/Entities, Retrofit API/DTOs, Mappers)   |
+-------------------------------------------------------------------+
```

### Garantias Obrigatórias
- **Isolamento do Domain:** Nenhum arquivo dentro de `com.example.fumacanorte.domain` pode importar `android.*`, `androidx.*`, `androidx.room.*`, `retrofit2.*`, `okhttp3.*` ou `com.squareup.moshi.*`.
- **Inversão de Dependência (DIP):** A camada `domain` declara a interface `WeatherRepository`. A camada `data` implementa `WeatherRepositoryImpl` e a injeta via Koin. A camada `presentation` consome apenas os `UseCases` do domínio (ou modelos puros de domínio mapeados para UI), nunca acessando `WeatherDao` ou `OpenMeteoApi` diretamente.
- **Fronteira de Modelos:**
  - `data.remote.dto.*` representa estritamente o JSON da API Open-Meteo.
  - `data.local.entity.*` representa estritamente as tabelas do Room.
  - `domain.model.*` representa os conceitos de negócio limpos e independentes de persistência/rede.
  - `presentation.model.*` (quando necessário) formata dados para exibição em tela sem vazar lógica de infraestrutura.

---

## 3. Fluxo de Dados Unidirecional (UDF)

Na camada `presentation`, o aplicativo utiliza **Unidirectional Data Flow (UDF)** com `ViewModel` e `StateFlow`:

```text
   +---------------------+         UiEvent / Intent         +--------------------+
   |   Jetpack Compose   | -------------------------------> |     ViewModel      |
   |   (Stateless UI)    |                                  |  (viewModelScope)  |
   +---------------------+ <------------------------------- +--------------------+
                                StateFlow<WeatherUiState>             |
                                                                      | Invoca UseCases
                                                                      v
                                                            +--------------------+
                                                            |   Domain UseCases  |
                                                            +--------------------+
```

### Princípios do UDF no Fumaça Norte
1. **Estado Único e Imutável:** Cada tela observa um único `StateFlow<UiState>` via `collectAsStateWithLifecycle()`. O `UiState` representa explicitamente os estados `Loading`, `Success`, `Empty` e `Error` (via `sealed interface` / `data class` imutável).
2. **Ações Explícitas (`UiEvent` / Métodos de Intenção):** Interações do usuário (ex.: puxar para atualizar, clicar em "Tentar novamente", selecionar um horário) disparam eventos claros para o `ViewModel`, nunca alterando estado diretamente na View.
3. **Efeitos Únicos (`Channel` / `SharedFlow` ou campos consumíveis):** Mensagens transitórias (como aviso de falha de sincronização enquanto exibe cache offline) são tratadas de forma determinística para evitar reexibição indevida após rotação de tela.

---

## 4. Estratégia Offline-First (Room como Single Source of Truth)

O **Fumaça Norte** implementa o padrão **Single Source of Truth (SSOT)** centrado no banco de dados local **Room**:

```text
                    +----------------------------------+
                    |      UI / WeatherViewModel       |
                    +----------------------------------+
                       ^                            |
     Flow<Weather?>    |                            | refreshWeather()
     (observa sempre   |                            v
      o banco local)   |             +-----------------------------+
                       |             |    WeatherRepositoryImpl    |
                       |             +-----------------------------+
                       |                |                       ^
                       |   grava        |                       | DTO JSON
                       |   transacional |                       |
                       v                v                       |
              +--------------------------+           +-----------------------+
              |   Room (WeatherDao)      |           |  OpenMeteoApiService  |
              |  Single Source of Truth  |           |      (Retrofit)       |
              +--------------------------+           +-----------------------+
```

### Fluxo de Leitura e Sincronização
1. **Leitura Reativa Contínua (`ObserveWeatherUseCase`):**
   - A UI se inscreve no fluxo exposto pelo repositório (`observeManausWeather(): Flow<WeatherSnapshot?>`), que por sua vez observa diretamente as consultas `@Query` do `WeatherDao` no Room.
   - Qualquer inserção ou atualização no banco local propaga automaticamente o novo estado para a UI.
2. **Política de Sincronização (`RefreshWeatherUseCase`):**
   - Ao iniciar a tela ou mediante gesto manual de atualização (*Pull-to-Refresh*), o `ViewModel` aciona o `RefreshWeatherUseCase`.
   - O repositório executa a chamada `GET v1/forecast` via `OpenMeteoApiService` dentro de `Dispatchers.IO`.
   - **Em caso de sucesso:** Os DTOs são validados, convertidos para Entities (`CurrentWeatherEntity` e lista de `HourlyForecastEntity`) e gravados atomicamente no Room por meio de uma operação `@Transaction` (`replaceWeatherData(...)`). O `Flow` do Room emite automaticamente os dados atualizados.
   - **Em caso de falha (sem internet, timeout, erro 5xx/4xx, falha de parsing):**
     - Nenhum dado existente no Room é apagado.
     - O repositório retorna um resultado tipado de falha (`Result.failure` ou `SyncResult.Error(AppError)`).
     - Se houver cache local, a UI permanece no estado `Success` exibindo os dados armazenados junto à data/hora da última sincronização e um aviso discreto de que está operando em modo offline.
     - Se o banco local estiver vazio, a UI transiciona para o estado `Error` com opção de nova tentativa.

### Benefícios da Estratégia
- **Latência Zero na Abertura:** O usuário visualiza imediatamente a última previsão conhecida sem esperar handshake TLS/HTTP.
- **Consistência Transacional:** Atualizações parciais nunca deixam condições atuais de um dia misturadas com previsão horária de outro.
- **Resiliência Total:** Perdas momentâneas de rede durante deslocamentos não quebram a experiência.

---

## 5. Estrutura de Pacotes Planejada (`com.example.fumacanorte`)

```text
app/src/main/java/com/example/fumacanorte/
├── FumacaNorteApplication.kt              # Inicialização do Koin (startKoin)
├── di/
│   ├── NetworkModule.kt                   # OkHttp, Moshi, Retrofit, OpenMeteoApiService
│   ├── DatabaseModule.kt                  # RoomDatabase, WeatherDao
│   ├── DataModule.kt                      # WeatherRepositoryImpl, Mappers, Dispatchers
│   ├── DomainModule.kt                    # UseCases
│   └── PresentationModule.kt              # ViewModels
│
├── domain/                                # [KOTLIN PURO - SEM IMPORTS ANDROID]
│   ├── model/
│   │   ├── CurrentWeather.kt              # Modelo de domínio das condições atuais
│   │   ├── HourlyForecast.kt              # Modelo de domínio da previsão horária
│   │   ├── WeatherSnapshot.kt             # Agregado atual + lista horária + metadados
│   │   ├── WeatherCondition.kt            # Enum mapeando códigos WMO
│   │   └── DomainError.kt                 # Erros tipados de domínio (Network, Timeout, Unknown)
│   ├── repository/
│   │   └── WeatherRepository.kt           # Interface do repositório (observe + refresh + observeHourlyById)
│   └── usecase/
│       ├── ObserveManausWeatherUseCase.kt # Observa fluxo contínuo do cache SSOT
│       ├── RefreshManausWeatherUseCase.kt # Dispara sincronização com Open-Meteo
│       └── ObserveHourlyDetailUseCase.kt  # Observa detalhe de um registro horário específico
│
├── data/                                  # [INFRAESTRUTURA: ROOM + RETROFIT]
│   ├── remote/
│   │   ├── api/
│   │   │   └── OpenMeteoApiService.kt     # Interface Retrofit (v1/forecast)
│   │   ├── dto/
│   │   │   └── OpenMeteoResponseDto.kt    # DTOs Moshi (CurrentDto, HourlyDto, ResponseDto)
│   │   └── source/
│   │       └── WeatherRemoteDataSource.kt # Abstração de chamada remota + tratamento HTTP
│   ├── local/
│   │   ├── db/
│   │   │   └── FumacaNorteDatabase.kt     # @Database Room
│   │   ├── dao/
│   │   │   └── WeatherDao.kt              # Queries Flow + @Transaction upsert
│   │   └── entity/
│   │       ├── CurrentWeatherEntity.kt    # Tabela current_weather
│   │       ├── HourlyForecastEntity.kt    # Tabela hourly_forecast
│   │       └── WeatherWithHourlyRelation.kt # @Relation Room para leitura atômica
│   ├── mapper/
│   │   └── WeatherMappers.kt              # Conversões DTO -> Entity -> Domain
│   └── repository/
│       └── WeatherRepositoryImpl.kt       # Implementação Offline-First (SSOT)
│
└── presentation/                          # [JETPACK COMPOSE + MVVM + UDF]
    ├── MainActivity.kt                    # Activity principal (enableEdgeToEdge + NavHost)
    ├── navigation/
    │   ├── FumacaNorteNavGraph.kt         # Grafo de navegação Compose tipado (@Serializable)
    │   └── Routes.kt                      # Rotas Dashboard e HourlyDetail
    ├── theme/
    │   ├── Color.kt                       # Paleta M3 adaptada à temática amazônica/climática
    │   ├── Type.kt                        # Tipografia Material 3
    │   └── Theme.kt                       # FumacaNorteTheme
    ├── weather/
    │   ├── WeatherUiState.kt              # Sealed interface (Loading, Success, Empty, Error)
    │   ├── WeatherUiEvent.kt              # Eventos de entrada da UI
    │   ├── WeatherViewModel.kt            # ViewModel com StateFlow e UDF
    │   ├── WeatherDashboardScreen.kt      # Tela principal (atual + lista horária)
    │   └── components/
    │       ├── CurrentWeatherHeroCard.kt  # Cartão de destaque das condições atuais de Manaus
    │       ├── HourlyForecastItem.kt      # Card de item horário otimizado para LazyColumn/Row
    │       ├── SyncStatusBanner.kt        # Indicador de modo offline / última atualização
    │       └── WeatherStateViews.kt       # Componentes Loading, Empty e Error
    ├── detail/
    │   ├── HourlyDetailUiState.kt         # Estado da tela de detalhes horários
    │   ├── HourlyDetailViewModel.kt       # ViewModel dos detalhes de uma hora selecionada
    │   └── HourlyDetailScreen.kt          # Tela completa de inspeção horária
    └── util/
        └── WeatherConditionFormatter.kt   # Mapeamento WeatherCondition -> Texto PT-BR / Ícone M3
```

---

## 6. Estratégia de Testes por Camada

| Camada | Escopo de Teste | Ferramentas | Localização |
| :--- | :--- | :--- | :--- |
| **Domain** | Regras de negócio, casos de uso, mapeamento de códigos WMO | JUnit 4, MockK, Turbine, Coroutines Test | `src/test/.../domain/` |
| **Data** | Mappers DTO/Entity/Domain, `WeatherRepositoryImpl` (SSOT, cache + falha de rede, atualização transacional) | JUnit 4, MockK, Turbine, Coroutines Test | `src/test/.../data/` |
| **Presentation** | Transições de `StateFlow<WeatherUiState>` (`Loading`, `Success`, `Empty`, `Error`), Pull-to-Refresh, detalhe horário | JUnit 4, MockK, Turbine, Coroutines Test | `src/test/.../presentation/` |
