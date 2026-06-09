# UrbanSense — Plataforma de Inteligência Urbana

> *"Não apenas prevemos o clima. Explicamos como ele afeta sua cidade."*

## 🎬 Demonstração em Vídeo

<!-- Cole aqui o link do YouTube após a gravação -->
> 🔗 **[Assista a demonstração completa no YouTube](#)**

---

## 👥 Integrantes

| Nome | RM |
|---|---|
| Adao Yuri | RM559223 |
| João Vitor Santana | RM560781 |
| Julia Rodrigues | RM559781 |
| Felipe Soares | RM559175 |
| Henrique Souza | RM99742 |

---

## 🏙️ Sobre o Projeto

O **UrbanSense** é uma plataforma de inteligência urbana que conecta **dados climáticos + mobilidade urbana + IA** para prever impactos reais no dia a dia dos cidadãos de São Paulo.

Diferente de um app comum de previsão do tempo, o UrbanSense responde perguntas como:

- *"Devo levar guarda-chuva hoje?"*
- *"A Linha 10 vai atrasar por causa da chuva?"*
- *"Quais regiões têm risco de alagamento agora?"*
- *"Devo sair mais cedo para evitar o trânsito?"*

---

## 🏗️ Arquitetura de Microsserviços

| Serviço | Porta | Responsabilidade |
|---|---|---|
| weather-service | 8080 | Clima atual, histórico, risco de chuva |
| alert-service | 8081 | Regras de negócio, alertas, RabbitMQ + DLQ |
| mobility-service | 8082 | 12 linhas metrô SP, risco por linha |
| ai-advisor-service | 8083 | Spring AI, RAG, Tooling, MCP |
| api-gateway | 8000 | Roteamento + Landing Page |

---

## ✅ Requisitos Atendidos

| Requisito | Implementação |
|---|---|
| API REST + Spring Boot | 4 microsserviços Spring Boot 3.3 |
| Boas práticas REST | Arquitetura Hexagonal, DTOs, RFC 7807 |
| Persistência relacional | PostgreSQL + JPA + Flyway migrations |
| Spring Security + JWT | JwtTokenProvider + JwtAuthenticationFilter |
| HATEOAS | EntityModel + CollectionModel com links |
| Cache | @Cacheable Caffeine (TTL 10min) |
| CORS | Configurado em todos os serviços |
| Swagger / OpenAPI | SpringDoc em todos os serviços |
| Microsserviços | 4 serviços independentes + API Gateway |
| Mensageria | RabbitMQ Topic Exchange + DLQ |
| API não-CRUD | Análise de risco, alertas por regras de negócio |
| Feign + fallback | OpenWeatherClient + WeatherServiceClient |
| Spring AI — Tooling | @Tool no UrbanIntelTools |
| Spring AI — RAG | Base de conhecimento com histórico urbano de SP |
| Spring AI — MCP | McpController expõe ferramentas no padrão MCP |

---

## 🚀 Como Rodar Localmente

### Pré-requisitos

- Java 21 ([download](https://adoptium.net))
- Maven 3.9+ ([download](https://maven.apache.org))
- Docker Desktop ([download](https://www.docker.com/products/docker-desktop))
- Ollama ([download](https://ollama.com))
- Chave API OpenWeather ([grátis](https://openweathermap.org/api))

### 1. Clone o repositório

```bash
git clone https://github.com/Yuri-t0/urbansense-api.git
cd urbansense-api
```

### 2. Instale o modelo de IA

```bash
ollama pull qwen2.5:0.5b
```

### 3. Suba a infraestrutura

```bash
docker compose up -d postgres-weather postgres-alert postgres-mobility postgres-ai rabbitmq
```

### 4. Suba cada serviço em um terminal separado

**Terminal 1 — Weather Service:**
```powershell
cd weather-service
$env:JWT_SECRET="urban-intel-super-secret-key-2024-must-be-long-enough"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:OPENWEATHER_API_KEY="sua_chave_aqui"
mvn spring-boot:run -DskipTests
```

**Terminal 2 — Alert Service:**
```powershell
cd alert-service
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/urbanintel_alerts"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:JWT_SECRET="urban-intel-super-secret-key-2024-must-be-long-enough"
$env:SPRING_RABBITMQ_HOST="localhost"
$env:SERVER_PORT="8081"
mvn spring-boot:run -DskipTests
```

**Terminal 3 — Mobility Service:**
```powershell
cd mobility-service
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5435/urbanintel_mobility"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:JWT_SECRET="urban-intel-super-secret-key-2024-must-be-long-enough"
$env:SPRING_RABBITMQ_HOST="localhost"
$env:SERVER_PORT="8082"
mvn spring-boot:run -DskipTests
```

**Terminal 4 — AI Advisor:**
```powershell
cd ai-advisor-service
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5434/urbanintel_ai"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:JWT_SECRET="urban-intel-super-secret-key-2024-must-be-long-enough"
$env:SPRING_RABBITMQ_HOST="localhost"
$env:SERVER_PORT="8083"
mvn spring-boot:run -DskipTests
```

**Terminal 5 — API Gateway:**
```powershell
cd api-gateway
mvn spring-boot:run
```

### 5. Acesse a plataforma

| URL | Descrição |
|---|---|
| http://localhost:8000 | Landing Page |
| http://localhost:8080/swagger-ui.html | Weather Service |
| http://localhost:8081/swagger-ui.html | Alert Service |
| http://localhost:8082/swagger-ui.html | Mobility Service |
| http://localhost:8083/swagger-ui.html | AI Advisor |
| http://127.0.0.1:15672 | RabbitMQ (guest/guest) |

### 6. Login para testar endpoints protegidos

```bash
POST http://localhost:8080/api/v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

---

## 📝 Licença

Projeto acadêmico — FIAP 2026 · Java Advanced
