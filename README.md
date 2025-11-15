# Microservices E-commerce Backend

Backend em microsserviços escalável e resiliente para e-commerce, desenvolvido com Kotlin, Spring Boot, RabbitMQ e Docker.

## Arquitetura

Este projeto implementa uma arquitetura de microsserviços com os seguintes componentes:

### Microserviços

- **API Gateway** (Porta 8080) - Ponto de entrada único para todas as requisições
- **Products Service** (Porta 8081) - Gerenciamento de produtos e estoque
- **Users Service** (Porta 8082) - Autenticação e gerenciamento de usuários
- **Orders Service** (Porta 8083) - Processamento de pedidos
- **Notifications Service** (Porta 8084) - Envio de notificações por email
- **Analytics Service** (Porta 8085) - Coleta e análise de métricas

### Infraestrutura

- **PostgreSQL** (Porta 5432) - Banco de dados relacional
- **RabbitMQ** (Porta 5672, Management 15672) - Message broker para comunicação assíncrona
- **Redis** (Porta 6379) - Cache em memória

## Stack Tecnológica

- **Kotlin** - Linguagem de programação
- **Spring Boot 3.2** - Framework backend
- **Spring Cloud Gateway** - API Gateway
- **PostgreSQL** - Banco de dados
- **RabbitMQ** - Message broker
- **Redis** - Cache
- **Flyway** - Migrations de banco de dados
- **Docker & Docker Compose** - Containerização e orquestração
- **JUnit 5 & MockK** - Testes
- **OpenAPI/Swagger** - Documentação de API

## Características

### Padrões de Arquitetura

- ✅ **Event-Driven Architecture** - Comunicação assíncrona via RabbitMQ
- ✅ **Database per Service** - Cada serviço possui seu próprio banco de dados
- ✅ **API Gateway Pattern** - Ponto de entrada único
- ✅ **Circuit Breaker** - Resiliência com Resilience4j
- ✅ **Service Discovery** - Comunicação entre serviços
- ✅ **Caching** - Redis para cache distribuído

### Funcionalidades

- 🛍️ **Catálogo de Produtos** - CRUD completo, busca, categorias, controle de estoque
- 👤 **Gestão de Usuários** - Registro, login, JWT authentication
- 💳 **Processamento de Pedidos** - Criação, rastreamento, atualização de status
- 📧 **Notificações** - Email para confirmação de pedidos e atualizações
- 📊 **Analytics** - Métricas de vendas, usuários, produtos

### Resiliência e Escalabilidade

- Circuit Breakers para tolerância a falhas
- Cache distribuído com Redis
- Filas de mensagens com RabbitMQ
- Health checks e monitoring
- Containerização com Docker
- Horizontal scaling ready

## Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+
- (Opcional) JDK 17+ para desenvolvimento local
- (Opcional) Gradle 8.5+ para desenvolvimento local

## Como Executar

### 1. Clone o repositório

```bash
git clone <repository-url>
cd microservices-e-commerce-backend
```

### 2. Inicie todos os serviços com Docker Compose

```bash
docker-compose up -d
```

Este comando irá:
- Criar e configurar todos os bancos de dados PostgreSQL
- Iniciar o RabbitMQ e Redis
- Construir e iniciar todos os microsserviços
- Configurar a rede e volumes

### 3. Aguarde todos os serviços iniciarem

```bash
docker-compose ps
```

Todos os serviços devem estar com status "Up (healthy)".

### 4. Acesse os serviços

- **API Gateway**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Products Service Swagger**: http://localhost:8081/swagger-ui.html
- **Users Service Swagger**: http://localhost:8082/swagger-ui.html
- **Orders Service Swagger**: http://localhost:8083/swagger-ui.html
- **Analytics Service Swagger**: http://localhost:8085/swagger-ui.html

## Endpoints Principais

### Via API Gateway (http://localhost:8080)

#### Products

```bash
# Listar produtos
GET /api/products

# Criar produto
POST /api/products
{
  "name": "Laptop Gaming",
  "description": "High-performance gaming laptop",
  "price": 2999.99,
  "stock": 50,
  "category": "Electronics"
}

# Buscar produtos
GET /api/products/search?searchTerm=laptop
```

#### Users

```bash
# Registrar usuário
POST /api/users/register
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe"
}

# Login
POST /api/users/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Orders

```bash
# Criar pedido
POST /api/orders
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddress": "123 Main St, City, State",
  "paymentMethod": "credit_card"
}

# Listar pedidos do usuário
GET /api/orders/user/1
```

#### Analytics

```bash
# Resumo de analytics
GET /api/analytics/summary

# Eventos recentes
GET /api/analytics/events/recent?hours=24
```

## Eventos RabbitMQ

Os serviços se comunicam via eventos assíncronos:

### Events Publicados

- **product.created** - Novo produto criado
- **product.updated** - Produto atualizado
- **product.stock.updated** - Estoque atualizado
- **user.registered** - Novo usuário registrado
- **order.created** - Novo pedido criado
- **order.status.updated** - Status do pedido atualizado

### Consumers

- **Notifications Service** - Escuta todos os eventos e envia notificações
- **Analytics Service** - Escuta todos os eventos e coleta métricas

## Desenvolvimento Local

### Executar serviço individual

```bash
cd products-service
./gradlew bootRun
```

### Executar testes

```bash
# Todos os testes
./gradlew test

# Testes de um serviço específico
cd products-service
./gradlew test
```

### Build

```bash
# Build de todos os serviços
./gradlew build

# Build de um serviço específico
cd products-service
./gradlew build
```

## Estrutura do Projeto

```
.
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
├── products-service/         # Serviço de Produtos
├── users-service/           # Serviço de Usuários
├── orders-service/          # Serviço de Pedidos
├── notifications-service/   # Serviço de Notificações
├── analytics-service/       # Serviço de Analytics
├── docker-compose.yml       # Orquestração de containers
├── init-databases.sql       # Script de inicialização dos bancos
└── README.md               # Esta documentação
```

Cada serviço possui a seguinte estrutura:

```
service-name/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/ecommerce/service/
│   │   │       ├── controller/      # REST Controllers
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access
│   │   │       ├── model/          # Domain models
│   │   │       ├── dto/            # Data transfer objects
│   │   │       ├── config/         # Configurações
│   │   │       └── exception/      # Custom exceptions
│   │   └── resources/
│   │       ├── application.yml     # Configuração
│   │       └── db/migration/       # Flyway migrations
│   └── test/                       # Testes
├── build.gradle.kts               # Build configuration
└── Dockerfile                     # Container image
```

## Monitoramento e Saúde

Cada serviço expõe endpoints de actuator:

```bash
# Health check
GET http://localhost:8081/actuator/health

# Métricas
GET http://localhost:8081/actuator/metrics

# Prometheus metrics
GET http://localhost:8081/actuator/prometheus
```

## Troubleshooting

### Serviços não iniciam

```bash
# Ver logs
docker-compose logs -f service-name

# Reiniciar serviços
docker-compose restart

# Recriar containers
docker-compose down
docker-compose up -d --build
```

### Banco de dados não conecta

```bash
# Verificar status do PostgreSQL
docker-compose ps postgres

# Ver logs do PostgreSQL
docker-compose logs postgres

# Recriar banco de dados
docker-compose down -v
docker-compose up -d
```

### RabbitMQ não está funcionando

```bash
# Acessar management UI
http://localhost:15672 (guest/guest)

# Ver logs
docker-compose logs rabbitmq
```

## Próximos Passos

- [ ] Adicionar autenticação JWT no API Gateway
- [ ] Implementar Service Discovery com Eureka
- [ ] Adicionar Distributed Tracing com Zipkin
- [ ] Implementar API Rate Limiting
- [ ] Adicionar Kubernetes deployment files
- [ ] Implementar SAGA pattern para transações distribuídas
- [ ] Adicionar métricas com Prometheus e Grafana
- [ ] Implementar testes de integração end-to-end

## Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT.

## Autores

Desenvolvido com ❤️ para demonstrar arquitetura de microsserviços moderna