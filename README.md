# Pizzaria Kafka — Spring Boot + Java 17

API de pedidos para pizzaria, com persistência PostgreSQL e publicação/consumo de eventos Kafka.

## Stack
- Java 17, Spring Boot 3.4, Spring Web, Bean Validation
- Apache Kafka (modo KRaft não usado; Zookeeper incluído conforme solicitado)
- Kafka UI, PostgreSQL 16, Flyway, Spring Data JPA
- Maven, Docker Compose, Actuator

## Arquitetura

```text
Cliente HTTP
   |
   v
OrderController -> OrderService -> PostgreSQL
                       |
                       +--> Kafka: pizzaria.orders.created.v1
                       |         |
                       |         +--> OrderCreatedConsumer (grupo pizzaria-kitchen)
                       |
                       +--> Kafka: pizzaria.orders.status.v1
```
O produtor envia eventos com a chave `orderId`, mantendo eventos do mesmo pedido na mesma partição.
Os tópicos são criados automaticamente pela aplicação com 3 partições e fator de replicação 1 (ambiente local).

## Pré-requisitos
- Docker + Docker Compose
- JDK 17+
- Maven 3.9+ (para executar fora do Docker)

## Subir o projeto completo

```bash
docker compose up -d --build
docker compose ps
```

O Compose sobe Zookeeper, Kafka, Kafka UI, PostgreSQL e a API Spring Boot. Para executar a API localmente com Maven, pare o serviço de API com `docker compose stop api` e rode `mvn spring-boot:run`.

Interfaces:
- API: http://localhost:8080
- Kafka UI: http://localhost:8081
- PostgreSQL: localhost:5432
- Kafka para aplicações locais: `localhost:9092`
- Kafka entre containers: `kafka:29092`

Aguarde Kafka e PostgreSQL ficarem saudáveis antes de iniciar a API.

## Executar a aplicação

```bash
mvn clean spring-boot:run
```

Ou gerar o JAR:
```bash
mvn clean package
java -jar target/pizzaria-kafka-1.0.0.jar
```


## API

### Criar pedido
`POST /api/v1/orders`

```json
{
  "customerName": "João Silva",
  "customerPhone": "19999999999",
  "deliveryAddress": "Rua das Pizzas, 123",
  "notes": "Sem cebola",
  "items": [
    { "pizzaName": "Calabresa", "size": "GRANDE", "quantity": 2, "unitPrice": 49.90 },
    { "pizzaName": "Portuguesa", "size": "MEDIA", "quantity": 1, "unitPrice": 39.90 }
  ]
}
```

Tamanhos aceitos: `PEQUENA`, `MEDIA`, `GRANDE`, `FAMILIA`.
O preço é informado na requisição para manter o exemplo simples. Em produção, o preço deve vir de um catálogo confiável no servidor.

### Listar pedidos
`GET /api/v1/orders`
`GET /api/v1/orders?status=RECEIVED`

### Consultar pedido
`GET /api/v1/orders/{id}`

### Atualizar status
`PATCH /api/v1/orders/{id}/status`

```json
{ "status": "PREPARING" }
```

Fluxo de status sugerido: `RECEIVED` → `PREPARING` → `READY` → `OUT_FOR_DELIVERY` → `DELIVERED`.
`CANCELLED` também está disponível. O domínio impede mudanças depois de `DELIVERED` ou `CANCELLED`; regras de transição mais estritas podem ser adicionadas conforme as políticas da pizzaria.

## Eventos Kafka

- `pizzaria.orders.created.v1`: evento de pedido criado com cliente, total e itens.
- `pizzaria.orders.status.v1`: evento de alteração de status.
- Consumidor `pizzaria-kitchen`: recebe `OrderCreatedEvent` e registra o pedido no log, como exemplo de integração com a cozinha.

Inspecione os tópicos, mensagens e consumer groups pelo Kafka UI.

## Testes

```bash
mvn test
```

## Configuração por variáveis de ambiente

| Variável | Padrão |
|---|---|
| `SERVER_PORT` | `8080` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/pizzaria` |
| `DB_USERNAME` | `pizzaria` |
| `DB_PASSWORD` | `pizzaria` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

## Teste manual da API com cURL

Com os containers em execução, é possível testar a API manualmente usando `curl`, sem precisar de ferramentas como Postman.

### 1. Verificar se a API está disponível

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:

```json
{"status":"UP"}
```

### 2. Criar um pedido

Execute a requisição abaixo para cadastrar um pedido com duas pizzas:

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Joao Silva",
    "customerPhone": "19999999999",
    "deliveryAddress": "Rua das Pizzas, 123",
    "notes": "Sem cebola",
    "items": [
      {
        "pizzaName": "Calabresa",
        "size": "GRANDE",
        "quantity": 2,
        "unitPrice": 49.90
      },
      {
        "pizzaName": "Portuguesa",
        "size": "MEDIA",
        "quantity": 1,
        "unitPrice": 39.90
      }
    ]
  }'
```

O valor total esperado é **R$ 139,70**. A resposta deve conter o identificador (`id`) do pedido e o status inicial `RECEIVED`.

Copie o `id` retornado para utilizá-lo nas próximas requisições.

### 3. Consultar o pedido criado

Substitua `<ID_DO_PEDIDO>` pelo identificador retornado na etapa anterior:

```bash
curl http://localhost:8080/api/v1/orders/<ID_DO_PEDIDO>
```

Verifique se o pedido contém os dados do cliente, os itens, o valor total e o status `RECEIVED`.

### 4. Listar todos os pedidos

```bash
curl http://localhost:8080/api/v1/orders
```

A resposta deve incluir o pedido criado anteriormente, demonstrando que ele foi persistido e pode ser recuperado pela API.

Também é possível filtrar os pedidos pelo status:

```bash
curl "http://localhost:8080/api/v1/orders?status=RECEIVED"
```

### 5. Atualizar o status do pedido

Para simular o início da preparação na cozinha:

```bash
curl -X PATCH http://localhost:8080/api/v1/orders/<ID_DO_PEDIDO>/status \
  -H "Content-Type: application/json" \
  -d '{"status":"PREPARING"}'
```

Consulte novamente o pedido para verificar a atualização:

```bash
curl http://localhost:8080/api/v1/orders/<ID_DO_PEDIDO>
```

O status esperado é `PREPARING`.

### 6. Verificar os eventos Kafka

Acompanhe os logs da aplicação:

```bash
docker compose logs -f api
```

Ao criar um pedido, o consumidor deve registrar uma mensagem indicando o recebimento do evento pela cozinha.

Também é possível acessar a [Kafka UI](http://localhost:8081) e inspecionar o tópico `pizzaria.orders.created.v1`.

### Resultado esperado

Ao concluir o fluxo, será possível verificar que:

- A API recebe e valida a requisição de criação do pedido.
- O pedido é persistido no PostgreSQL.
- O pedido pode ser recuperado pelos endpoints de consulta.
- A criação do pedido gera um evento Kafka consumido pelo componente da cozinha.
- O status do pedido pode ser atualizado e consultado posteriormente.

Esses passos constituem um **teste manual de integração do fluxo principal**. Eles não substituem uma suíte automatizada de testes de integração.


## Decisões e próximos passos para produção

- **Outbox Pattern:** neste projeto didático, a publicação Kafka ocorre junto ao fluxo transacional, mas banco e Kafka não compartilham uma transação atômica. Uma falha entre o commit no banco e a entrega do evento pode gerar inconsistência. Para produção, persista o evento em uma tabela outbox na mesma transação e publique-o com um relay/CDC (por exemplo, Debezium).
- **Idempotência:** os eventos incluem `eventId`; consumidores reais devem registrar eventos processados para evitar efeitos duplicados.
- **Resiliência:** configure retry/backoff e Dead Letter Topic para falhas de consumo.
- **Segurança:** adicione autenticação/autorização, secrets fora do compose e TLS/SASL no Kafka.
- **Observabilidade:** Actuator expõe health/info/metrics; adicione tracing e métricas Kafka para produção.
- **Preço:** valide preços no backend usando um catálogo, em vez de confiar no valor informado pelo cliente.
- Compose é configurado para desenvolvimento local, com um broker e fator de replicação 1; não representa uma topologia HA.
