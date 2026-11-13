# Fumaça Norte

Aplicativo Android nativo de monitoramento meteorológico **Offline-First** focado em **Manaus (AM)**, construído com **Kotlin**, **Jetpack Compose**, **Clean Architecture** e **Spec-Driven Development (SDD)**.

---

## Objetivo

Oferecer uma interface rápida, clara e resiliente a quedas de conectividade para acompanhamento das condições climáticas atuais e previsão horária de Manaus (`latitude = -3.10`, `longitude = -60.02`), garantindo que os dados mais recentes permaneçam sempre acessíveis localmente mesmo quando o dispositivo estiver offline.

---

## Funcionalidades Planejadas

- **Monitoramento Climático de Manaus (RF01):** Exibição de temperatura atual, sensação térmica, umidade relativa, precipitação, velocidade do vento e condição climática (padrão WMO).
- **Persistência Local Automática (RF02):** Armazenamento transacional do último snapshot meteorológico e previsões horárias em SQLite via Room.
- **Operação Offline-First (RF03):** Leitura reativa contínua a partir do banco local como *Single Source of Truth*, preservando dados em caso de indisponibilidade de rede e sinalizando o status de sincronização.
- **Atualização Manual (RF04):** Suporte a *Pull-to-Refresh* e botão de recarregamento para sincronização sob demanda.
- **Detalhes Horários (RF05):** Listagem horária com navegação para visualização detalhada de cada faixa de horário prevista.
- **Gestão Explícita de Estados de UI (RF06):** Tratamento visual dedicado para os estados `Loading`, `Success`, `Empty` e `Error`.

---

## Tecnologias e Bibliotecas

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose + Material Design 3 + Navigation Compose
- **Assincronismo:** Kotlin Coroutines & Flow / StateFlow
- **Injeção de Dependências:** Koin
- **Rede:** Retrofit + OkHttp + Moshi
- **Persistência Local:** Room (SQLite + KSP)
- **Testes Unitários:** JUnit 4 + MockK + Turbine + `kotlinx-coroutines-test`

---

## API REST Utilizada

- **Serviço:** [Open-Meteo Weather Forecast API](https://open-meteo.com/en/docs)
- **URL Base:** `https://api.open-meteo.com/`
- **Endpoint:** `GET v1/forecast`
- **Parâmetros de Localização:** `latitude=-3.10&longitude=-60.02&timezone=America/Manaus`
- **Autenticação:** Não requer chave de API (*API Key*) para uso público não comercial.

---

## Arquitetura

O projeto segue rigorosamente a **Clean Architecture** associada ao padrão **MVVM** e **Unidirectional Data Flow (UDF)**:

- **`domain`**: Regras de negócio, modelos puros (`WeatherSnapshot`, `CurrentWeather`, `HourlyForecast`), interfaces de repositório e casos de uso (`ObserveManausWeatherUseCase`, `RefreshManausWeatherUseCase`, `ObserveHourlyDetailUseCase`). Totalmente livre de dependências Android, Room ou Retrofit.
- **`data`**: Fontes de dados remota (`OpenMeteoApiService`) e local (`FumacaNorteDatabase`, `WeatherDao`), DTOs, Entities, Mappers e `WeatherRepositoryImpl`.
- **`presentation`**: Telas Jetpack Compose, componentes reutilizáveis, `WeatherUiState` imutável e `ViewModel`s expondo `StateFlow`.

Mais detalhes em [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) e [`docs/SPECIFICATION.md`](docs/SPECIFICATION.md).

---

## Estratégia Offline-First

1. O **Room** atua como **Single Source of Truth (SSOT)**. A camada de apresentação observa exclusivamente fluxos (`Flow`) emitidos pelo banco local.
2. Quando uma sincronização é solicitada (abertura do app ou *Pull-to-Refresh*), o repositório consulta a API Open-Meteo em `Dispatchers.IO`.
3. Em caso de sucesso, os dados antigos são substituídos atomicamente dentro de uma `@Transaction` no Room, disparando automaticamente a atualização da UI.
4. Em caso de erro de rede ou ausência de internet, o cache local é preservado integralmente e a UI continua funcional indicando o horário da última atualização válida.

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
4. Execute o aplicativo em um emulador ou dispositivo físico Android via Android Studio ou linha de comando:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Instruções de Testes

Os testes unitários das camadas `domain`, `data` e `presentation` executam diretamente na JVM com MockK e Turbine, sem necessidade de emulador:

```bash
./gradlew :app:testDebugUnitTest
```

---

## Evidências

> **Status:** Em desenvolvimento (Marco 1 — Fundação e Documentação SDD concluída; implementação das camadas e capturas de tela serão adicionadas nos próximos marcos).

---

## Limitações Conhecidas

- **Localidade Fixa:** Nesta versão, o monitoramento é focado exclusivamente nas coordenadas geográficas de Manaus (`-3.10, -60.02`), sem seletor dinâmico de múltiplas cidades ou uso de GPS.
- **Conectividade Inicial:** Na primeira execução com banco local vazio, é necessário acesso à internet ao menos uma vez para popular o cache inicial do Room.
- **Escopo de Qualidade do Ar:** Embora o nome do projeto remeta ao monitoramento regional da região Norte ("Fumaça Norte"), esta especificação inicial consome exclusivamente variáveis meteorológicas do endpoint `v1/forecast` da Open-Meteo.
