# Especificação de Software — Fumaça Norte

**Projeto:** Fumaça Norte (`fumaca-norte-android`)  
**Versão da Especificação:** 1.0.0  
**Plataforma:** Android (Kotlin / Jetpack Compose)  
**Paradigma:** Offline-First & Clean Architecture  

---

## 1. Visão Geral da Aplicação

### 1.1 Apresentação
O **Fumaça Norte** é um aplicativo Android nativo de monitoramento meteorológico contínuo focado na cidade de **Manaus (AM)**, projetado segundo a arquitetura **Offline-First** e os princípios de **Clean Architecture** e **Spec-Driven Development (SDD)**. O aplicativo coleta, armazena localmente e apresenta condições climáticas atuais e previsões horárias detalhadas para as coordenadas geográficas de Manaus (`latitude = -3.10`, `longitude = -60.02`).

### 1.2 Objetivo
Fornecer aos usuários da região amazônica uma ferramenta confiável, responsiva e resiliente a falhas de conectividade para consultar parâmetros meteorológicos críticos (temperatura, umidade relativa, precipitação, velocidade do vento, sensação térmica e código climático WMO), mantendo histórico e última previsão disponível localmente mesmo em cenários sem acesso à internet.

### 1.3 Problema Resolvido
Na região Norte do Brasil — e especificamente na área metropolitana de Manaus e seu entorno fluvial/florestal — a conectividade móvel pode sofrer oscilações frequentes ou interrupções completas. Aplicativos meteorológicos puramente dependentes de rede exibem telas vazias ou erros bloqueantes quando o dispositivo fica offline. Além disso, usuários necessitam visualizar rapidamente a evolução horária das condições climáticas (como chuvas intensas, ondas de calor e baixa umidade associados a períodos de estiagem e queimadas) sem depender de recarregamentos constantes.

O **Fumaça Norte** resolve esse problema adotando o banco de dados local **Room** como **Single Source of Truth (SSOT)**: toda leitura da interface ocorre a partir do armazenamento local reativo (`Flow`), enquanto sincronizações remotas via **Retrofit** atualizam o cache local de forma assíncrona e transparente.

---

## 2. Justificativa Técnica da API REST (Open-Meteo)

### 2.1 Dados da API
- **Provedor:** Open-Meteo Weather Forecast API
- **Documentação Oficial:** https://open-meteo.com/en/docs
- **URL Base:** `https://api.open-meteo.com/`
- **Endpoint Principal:** `GET v1/forecast`
- **Coordenadas Fixas (Manaus - AM):**
  - `latitude=-3.10`
  - `longitude=-60.02`
  - `timezone=America/Manaus`
- **Exemplo Completo de Requisição:**
  ```text
  https://api.open-meteo.com/v1/forecast?latitude=-3.10&longitude=-60.02&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m&timezone=America/Manaus&forecast_days=2
  ```

### 2.2 Motivos da Escolha
1. **Acesso Público sem Chave de API (Zero-Friction):** Para uso não comercial, a Open-Meteo não exige cadastro, tokens ou gerenciamento de chaves secretas (`API Key`), eliminando riscos de exposição de credenciais no repositório e simplificando a execução imediata do projeto em qualquer ambiente.
2. **Alta Confiabilidade e Modelos Meteorológicos Consagrados:** Combina modelos numéricos globais e regionais de alta resolução (como ECMWF, GFS e ICON), oferecendo precisão consistente para a bacia amazônica.
3. **Contrato JSON Limpo e Previsível:** O payload retornado utiliza estruturas tipadas (`current`, `current_units`, `hourly`, `hourly_units`), facilitando o mapeamento seguro para DTOs com Moshi/Kotlinx Serialization e evitando parsing complexo.
4. **Suporte Nativo a Fuso Horário e Parâmetros Seletivos:** Permite solicitar apenas os campos necessários (`current` e `hourly`), reduzindo drasticamente o consumo de banda móvel — característica essencial para um aplicativo pensado para regiões com conexão limitada.
5. **Padronização WMO:** Utiliza códigos meteorológicos padrão da Organização Meteorológica Mundial (`weather_code`), permitindo mapeamento determinístico de ícones e descrições climáticas em português na camada de apresentação.

---

## 3. Requisitos Funcionais (RF)

### RF01 — Consulta Meteorológica Online (Manaus)
- **Descrição:** O aplicativo deve consumir o endpoint `v1/forecast` da API Open-Meteo para as coordenadas `latitude = -3.10` e `longitude = -60.02`, obtendo os dados meteorológicos atuais (`current`) e a série de previsão horária (`hourly`).
- **Dados Obrigatórios (Atuais):** Temperatura (`temperature_2m`), umidade relativa (`relative_humidity_2m`), sensação térmica (`apparent_temperature`), precipitação (`precipitation`), velocidade do vento (`wind_speed_10m`), código climático (`weather_code`) e timestamp da leitura.
- **Dados Obrigatórios (Horários):** Lista de horários (`time`), temperatura horária, umidade relativa horária, probabilidade de precipitação, precipitação, código climático e velocidade do vento.
- **Critério de Aceite:** Ao executar uma sincronização com internet disponível, a resposta HTTP 200 é deserializada, convertida em modelos de domínio e refletida na interface em até poucos segundos.

### RF02 — Persistência Local com Room (Single Source of Truth)
- **Descrição:** Todos os dados meteorológicos obtidos com sucesso da API devem ser persistidos localmente em banco de dados SQLite gerenciado pelo **Room**.
- **Comportamento:**
  - Armazenar o snapshot das condições atuais de Manaus com timestamp da última sincronização (`lastUpdatedEpochMillis`).
  - Armazenar os registros da previsão horária associados ao snapshot, substituindo dados obsoletos de forma transacional (`@Transaction`) para garantir consistência.
- **Critério de Aceite:** Após uma consulta bem-sucedida, encerrar e reabrir o aplicativo sem conexão deve exibir integralmente os últimos dados salvos.

### RF03 — Modo Offline-First
- **Descrição:** A interface do usuário deve observar continuamente o banco local Room por meio de `Flow` reativo, nunca dependendo diretamente da resposta da rede para renderizar conteúdo já existente em cache.
- **Comportamento:**
  - Ao abrir o app, dados em cache são emitidos imediatamente.
  - Em paralelo, uma sincronização em segundo plano tenta buscar dados atualizados.
  - Se a requisição falhar (dispositivo sem rede, timeout ou erro HTTP) e houver dados em cache, o app continua exibindo os dados armazenados e informa discretamente o usuário (ex.: indicador de modo offline / data da última atualização + banner/snackbar não bloqueante).
- **Critério de Aceite:** Modo avião ativado com cache prévio não resulta em tela branca nem em erro fatal; os dados locais permanecem navegáveis.

### RF04 — Atualização Manual (Pull-to-Refresh e Ação Explícita)
- **Descrição:** O usuário deve poder disparar manualmente uma nova sincronização com a API Open-Meteo a qualquer momento através de gesto *Pull-to-Refresh* ou botão explícito de atualização.
- **Comportamento:**
  - Durante o recarregamento manual, exibir feedback visual de progresso sem apagar os dados atualmente visíveis na tela (quando houver cache).
  - Evitar requisições duplicadas simultâneas caso uma atualização já esteja em andamento.
- **Critério de Aceite:** Puxar a lista para baixo ou tocar no botão de atualizar dispara a chamada ao `RefreshWeatherUseCase` e atualiza o timestamp exibido em caso de sucesso.

### RF05 — Visualização de Detalhes Horários
- **Descrição:** O usuário deve poder visualizar a previsão horária em uma lista estruturada e selecionar um horário específico para inspecionar seus detalhes completos.
- **Comportamento:**
  - A tela principal exibe o resumo climático atual de Manaus e a lista/carrossel de previsões horárias.
  - Ao tocar em um item horário, o aplicativo apresenta uma visualização detalhada (tela de detalhe ou painel dedicado via navegação tipada) contendo hora, descrição climática WMO, temperatura, umidade, probabilidade de chuva, volume de precipitação e velocidade do vento.
- **Critério de Aceite:** Clicar em qualquer item horário exibe os valores exatos correspondentes àquele registro persistido no Room, funcionando tanto online quanto offline.

### RF06 — Tratamento Visual dos Estados de UI (`Loading`, `Success`, `Empty`, `Error`)
- **Descrição:** Toda a interface reativa deve modelar explicitamente os quatro estados fundamentais de apresentação através de contratos imutáveis (`UiState`).
- **Comportamento por Estado:**
  - **Loading:** Exibir indicador de carregamento inicial (skeleton ou `CircularProgressIndicator` centralizado) quando não houver dados em cache, ou indicador discreto de sincronização (`isRefreshing = true`) quando já houver dados visíveis.
  - **Success:** Renderizar painel principal de Manaus, metadados da última atualização, status de conectividade/cache e lista de previsões horárias.
  - **Empty:** Quando o banco local estiver vazio e ainda não houver dados sincronizados (ex.: primeiro acesso concluído sem registros), exibir estado vazio informativo com botão de "Tentar novamente".
  - **Error:** Quando não houver dados em cache e a sincronização falhar, exibir tela de erro amigável com mensagem clara (sem stack traces), explicação de falha de rede e botão de ação para nova tentativa. Quando já houver cache e a atualização falhar, manter o estado `Success` com aviso não obstrutivo (`syncErrorMessage`).
- **Critério de Aceite:** Cada estado possui representação visual distinta, acessível e testável.

---

## 4. Requisitos Não Funcionais (RNF)

### RNF01 — Clean Architecture e Isolamento de Camadas
- **Descrição:** O projeto deve seguir estritamente a divisão em três camadas: **Presentation**, **Domain** e **Data**.
- **Restrição Crítica:** A camada `domain` deve ser 100% Kotlin puro (`kotlin-stdlib` e `kotlinx.coroutines`), sendo estritamente proibida qualquer importação de `android.*`, `androidx.*`, `androidx.room.*`, `retrofit2.*` ou `okhttp3.*`.

### RNF02 — Programação Assíncrona com Coroutines e Flow
- **Descrição:** Todas as operações de I/O (rede e banco de dados) devem ser executadas fora da *Main Thread* utilizando funções `suspend` e fluxos reativos `Flow` com dispatchers apropriados (`Dispatchers.IO` na camada de dados, `viewModelScope` na apresentação).
- **Segurança Estruturada:** Cancelamento automático de corrotinas atrelado ao ciclo de vida e tratamento explícito de exceções sem capturar `CancellationException`.

### RNF03 — Interface em Jetpack Compose com Otimização de Recomposição
- **Descrição:** Toda a UI deve ser construída em Jetpack Compose seguindo Material Design 3 (M3), suporte edge-to-edge e acessibilidade (`contentDescription`, alvos de toque mínimos de `48.dp`).
- **Performance:** Utilizar classes `@Immutable` / modelos estáveis no `UiState`, `remember` / `derivedStateOf` quando aplicável, `keys` explícitas em `LazyColumn` / `LazyRow` e separação entre composables *stateful* (rotas) e *stateless* (componentes visuais) para minimizar recomposições desnecessárias.

### RNF04 — Injeção de Dependências com Koin
- **Descrição:** Toda a resolução de dependências entre DataSources, DAOs, Repositórios, UseCases e ViewModels deve ser gerenciada pelo **Koin** (`koin-android`, `koin-androidx-compose`), organizada em módulos dedicados por camada (`networkModule`, `databaseModule`, `dataModule`, `domainModule`, `presentationModule`).

### RNF05 — Qualidade, Testabilidade e Testes Unitários
- **Descrição:** As camadas `domain`, `data` e `presentation` (ViewModels) devem possuir cobertura de testes unitários automatizados executáveis localmente na JVM sem necessidade de emulador.
- **Stack de Testes:** JUnit 4, **MockK** (mocks e verificações comportamentais), **Turbine** (asserções determinísticas sobre `Flow` e `StateFlow`) e `kotlinx-coroutines-test` (`StandardTestDispatcher` / `runTest`).
