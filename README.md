# API Raízes do Nordeste (Back-end)

Projeto Multidisciplinar – Trilha **Back-end** – UNINTER.
API REST em **Java 21 + Spring Boot 3**: pedidos em vários canais, estoque por unidade e **pagamento simulado (mock)**.

**Fluxo principal entregue (Fluxo A):** Pedido → Pagamento mock → Atualização de status.

## Links

| Item | Endereço |
|---|---|
| Repositório | https://github.com/zfrosty7/raizes-do-nordeste-api.git |
| Swagger (local) | http://localhost:8080/swagger-ui.html |
| Coleção Postman | `postman/raizes-do-nordeste.postman_collection.json` |
| DER | `docs/diagramas/der.png` |

## 1. Requisitos

- Java 21 (JDK) – teste com `java -version`
- Maven 3.9 ou superior – teste com `mvn -version`
- Banco: **H2** (já vem junto, não precisa instalar)
- Dependências: o Maven baixa sozinho

## 2. Variáveis de ambiente

Veja o arquivo `.env.example`. **São todas opcionais** (existe um valor padrão para cada uma).
O Spring Boot não lê o arquivo `.env` sozinho: se quiser mudar um valor, defina a variável no terminal antes de iniciar.

```bash
export JWT_SECRET="um-segredo-grande-com-mais-de-32-caracteres"   # Linux/macOS
$env:JWT_SECRET="um-segredo-grande-com-mais-de-32-caracteres"     # Windows PowerShell
```

## 3. Instalar as dependências

```bash
mvn clean install -DskipTests
```

## 4. Banco de dados e dados de teste (seed)

Não precisa fazer nada. Ao iniciar a API:
1. o Hibernate (ORM) cria as tabelas sozinho, a partir das classes da pasta `model` (arquivo do banco: `./data/raizes.mv.db`);
2. a classe `config/DadosIniciais` cadastra 2 unidades, 9 produtos e 2 usuários (só se o banco estiver vazio).

Para **zerar o banco**: pare a API e apague a pasta `data/`.

| Usuário (senha `Senha@123`) | Perfil | O que pode |
|---|---|---|
| `cliente@raizes.com` | CLIENTE | criar pedido, pagar, ver os próprios pedidos |
| `gerente@raizes.com` | GERENTE | atualizar o status dos pedidos e ver todos os pedidos |

## 5. Iniciar a API

```bash
mvn spring-boot:run
```

A API fica em `http://localhost:8080`.

## 6. Swagger (documentação)

Abra http://localhost:8080/swagger-ui.html. Para as rotas protegidas: faça `POST /auth/login`, copie o `accessToken`, clique em **Authorize** e cole o token.

## 7. Como rodar os testes

Os testes estão na coleção Postman (**16 cenários: 7 positivos e 9 negativos**).

1. Inicie a API com o banco recém-criado (sem a pasta `data/`).
2. No Postman: **Import** → escolha `postman/raizes-do-nordeste.postman_collection.json`.
3. Clique na coleção → **Run** → **Run Raizes...** (as pastas rodam na ordem 01 a 05; tokens e ids são guardados sozinhos).

Opcional, pelo terminal: `newman run postman/raizes-do-nordeste.postman_collection.json`.
Ao repetir os testes o estoque vai diminuindo; apague a pasta `data/` para recomeçar.

| ID | Cenário | Endpoint | Esperado |
|---|---|---|---|
| T01 | Login válido | POST /auth/login | 200 + accessToken |
| T02 | Senha inválida | POST /auth/login | 401 |
| T03 | Cadastro com consentimento LGPD | POST /auth/register | 201 |
| T04 | Cadastro sem consentimento | POST /auth/register | 422 |
| T05 | Cardápio da unidade 1 | GET /unidades/1/cardapio | 200 |
| T06 | Sem token | GET /pedidos | 401 |
| T07 | Pedido válido (TOTEM) | POST /pedidos | 201, AGUARDANDO_PAGAMENTO |
| T08 | Sem canalPedido | POST /pedidos | 422 |
| T09 | Quantidade negativa | POST /pedidos | 422 |
| T10 | Produto inexistente | POST /pedidos | 404 |
| T11 | Estoque insuficiente | POST /pedidos | 409 |
| T12 | Cliente muda status | PATCH /pedidos/{id}/status | 403 |
| T13 | Pagamento mock recusado | POST /pedidos/{id}/pagamentos | 201, RECUSADO, PAGAMENTO_RECUSADO |
| T14 | Pagamento mock aprovado | POST /pedidos/{id}/pagamentos | 201, APROVADO, PAGO |
| T15 | Gerente atualiza status | PATCH /pedidos/{id}/status | 200, EM_PREPARO |
| T16 | Filtro por canal | GET /pedidos?canalPedido=TOTEM | 200, só TOTEM |

## 8. Endpoints

| Método | Rota | Quem pode | O que faz |
|---|---|---|---|
| POST | /auth/register | todos | Cadastra cliente (exige `consentimentoLgpd: true`) |
| POST | /auth/login | todos | Faz login e devolve o token |
| GET | /unidades/{id}/cardapio | logado | Produtos e estoque da unidade |
| POST | /pedidos | CLIENTE | Cria pedido (`canalPedido` obrigatório) |
| GET | /pedidos | logado | Lista pedidos (filtro `canalPedido`; `page` e `limit`) |
| GET | /pedidos/{id} | logado | Consulta um pedido (cliente só o seu) |
| PATCH | /pedidos/{id}/status | GERENTE | Atualiza o status |
| POST | /pedidos/{id}/pagamentos | CLIENTE | Pagamento mock (`simularResultado`: APROVADO ou RECUSADO) |

Exemplo de criação de pedido:

```json
{
  "canalPedido": "TOTEM",
  "unidadeId": 1,
  "formaPagamento": "MOCK",
  "itens": [ { "produtoId": 1, "quantidade": 2 } ]
}
```

**Padrão de erro** (o mesmo JSON em todas as falhas):

```json
{
  "error": "ESTOQUE_INSUFICIENTE",
  "message": "Não há quantidade suficiente para um ou mais itens.",
  "details": [ { "field": "itens[0].quantidade", "issue": "Disponível: 2" } ],
  "timestamp": "2026-06-23T12:00:00Z",
  "path": "/pedidos"
}
```

Códigos usados: 200, 201, 400, 401 (sem token), 403 (sem permissão), 404, 409 (regra de negócio), 422 (dados inválidos), 500.

## 9. Estrutura do projeto

```
src/main/java/br/com/raizes/
├── controller/  # recebem as requisições HTTP e chamam o service
├── service/     # regras de negócio (estoque, status, pagamento, login)
├── repository/  # conversa com o banco de dados
├── model/       # classes que viram tabelas + enums
├── dto/         # formatos dos JSONs de entrada e saída
├── security/    # token JWT e verificação de perfil
├── exception/   # padrão de erro
└── config/      # Swagger e dados iniciais (seed)
```

Caminho de uma requisição: `controller → service → repository → banco`.

## 10. Segurança e LGPD (mínimo)

| Tema | O que foi feito |
|---|---|
| Autenticação | Login devolve um token JWT; as rotas protegidas conferem o token (401 se ausente ou inválido) |
| Autorização | Perfis CLIENTE e GERENTE; cada rota confere o perfil (403); o cliente só vê os próprios pedidos |
| Senha | Só o hash BCrypt é guardado; nunca é devolvido nas respostas |
| Dados pessoais coletados | Nome e e-mail |
| Finalidade | Identificar o cliente e processar os pedidos dele |
| Base legal | Consentimento (LGPD art. 7º, I) e execução de contrato (art. 7º, V) |
| Consentimento | Obrigatório no cadastro; gravado em `consentimento_lgpd` e `consentimento_em` |
| Logs | Ações sensíveis (cadastro, pedido criado, pagamento, mudança de status) são escritas no log do console, só com ids. **Não há tabela de auditoria** (escolha de simplicidade), por isso não há teste automatizado de logs |
| Retenção / anonimização | **Proposta, não implementada:** ao pedido de exclusão, trocar nome e e-mail por valores anônimos e manter os pedidos sem identificação |

## 11. Implementado x apenas proposto

**Implementado:** cadastro e login (JWT, perfis), cardápio por unidade, pedidos com `canalPedido`, estoque por unidade,
pagamento mock (aprovado/recusado), status do pedido, filtro e paginação, erro padronizado, Swagger.

**Proposto (somente documentado):** fidelidade (pontos e resgate), promoções e campanhas, cadastro de produtos e unidades pela API,
entrada de estoque, anonimização de dados, tabela de auditoria.

## 12. Simplificações (e por quê)

- Os relacionamentos entre tabelas são guardados como números (ex.: `cliente_id`); só `item_pedido.pedido_id` é chave estrangeira criada pelo banco. Fica mais fácil de entender.
- O estoque fica dentro do produto, e cada produto pertence a uma unidade.
- O Hibernate cria as tabelas (`ddl-auto=update`). Em produção usaríamos migrations (Flyway).
- O token é conferido de forma explícita em cada rota. Em produção usaríamos o Spring Security.
