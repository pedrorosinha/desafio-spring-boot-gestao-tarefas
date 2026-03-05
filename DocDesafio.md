# 📋 Desafio Java Spring Boot - Sistema de Gestão de Tarefas

## 📄 Enunciado do Desafio

- PDF do desafio: https://drive.google.com/file/d/1zWW_UnJXOOCpHc_CVn6_dggIsdx4b6Qn/view?usp=sharing

## 📝 Sobre o Projeto

Sistema de gerenciamento de projetos e tarefas desenvolvido em **Java 25** com **Spring Boot 4.0.3**, implementando controle de acesso baseado em roles (ADMIN, GERENTE, COLABORADOR) e autenticação JWT.

O sistema permite:
- ✅ Cadastro e gerenciamento de usuários
- ✅ Criação e controle de projetos
- ✅ Atribuição e acompanhamento de tarefas
- ✅ Autenticação segura com JWT
- ✅ Controle de permissões por role
- ✅ Documentação interativa com Swagger

## 🛠️ Tecnologias Utilizadas

- **Java 25**
- **Spring Boot 4.0.3**
	- Spring Data JPA
	- Spring Security
	- Spring Validation
- **H2 Database** (em memória)
- **JWT** (JSON Web Token) para autenticação
- **Lombok** para redução de boilerplate
- **Swagger/OpenAPI 3** para documentação
- **JUnit 5** e **Mockito** para testes
- **Maven** para gerenciamento de dependências

## 🚀 Como Rodar o Projeto

### Pré-requisitos

- Java 25 (JDK)
- Maven 3.8+

### Passos para executar

1. **Clone o repositório**
```bash
git clone <url-do-repositorio>
cd gestao-tarefas
```

2. **Compile o projeto**
```bash
./mvnw clean install
```

3. **Execute a aplicação**
```bash
./mvnw spring-boot:run
```

4. **Acesse a aplicação**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console
	- JDBC URL: `jdbc:h2:mem:gestao-tarefas`
	- Username: `sa`
	- Password: `root`

## 🧪 Executar Testes

```bash
# Todos os testes
./mvnw test

# Testes específicos
./mvnw test -Dtest=UsuarioControllerTest
./mvnw test -Dtest=ProjetoServiceTest

# Com cobertura
./mvnw verify
```

**Cobertura de Testes:**
- 108 testes implementados
- Testes unitários (controllers, services, exceptions)
- Testes de integração (autenticação JWT end-to-end)

## 📚 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── br/com/dbserver/gestao_tarefas/
│   │       ├── config/          # Configurações (Swagger)
│   │       ├── controller/      # Endpoints REST
│   │       ├── domain/          # Entidades e DTOs
│   │       ├── exception/       # Tratamento de erros
│   │       ├── repository/      # Camada de persistência
│   │       ├── security/        # JWT e autenticação
│   │       └── service/         # Lógica de negócio
│   └── resources/
│       └── application.properties
└── test/
		└── java/
				└── br/com/dbserver/gestao_tarefas/
						├── controller/      # Testes unitários
						├── integration/     # Testes de integração
						└── service/         # Testes de serviços
```

## 🔐 Autenticação e Autorização

### Roles do Sistema

- **ADMIN**: Acesso total (CRUD em todos os recursos)
- **GERENTE**: Gerencia projetos e delega tarefas
- **COLABORADOR**: Visualiza e atualiza suas próprias tarefas

### Fluxo de Autenticação

1. **Cadastrar usuário**
```bash
POST /usuarios/cadastrar
Content-Type: application/json

{
	"nome": "Admin User",
	"email": "admin@email.com",
	"senha": "senha123",
	"role": "ADMIN"
}
```

2. **Fazer login**
```bash
POST /auth/login
Content-Type: application/json

{
	"email": "admin@email.com",
	"senha": "senha123"
}
```

Resposta:
```json
{
	"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

3. **Usar o token nas requisições**
```bash
GET /projetos
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📖 Documentação da API (Swagger)

Acesse http://localhost:8080/swagger-ui.html após iniciar a aplicação.

### Principais Endpoints

#### Autenticação
- `POST /auth/login` - Login e geração de token JWT

#### Usuários
- `POST /usuarios/cadastrar` - Criar novo usuário (público)
- `GET /usuarios` - Listar usuários (ADMIN)

#### Projetos
- `POST /projetos/cadastrar` - Criar projeto (ADMIN/GERENTE)
- `GET /projetos` - Listar todos os projetos
- `GET /projetos/ativos` - Listar projetos ativos
- `GET /projetos/{id}` - Buscar projeto por ID
- `PUT /projetos/{id}` - Atualizar projeto (ADMIN/GERENTE responsável)
- `DELETE /projetos/{id}` - Desativar projeto (ADMIN/GERENTE responsável)

#### Tarefas
- `POST /tarefas/cadastrar` - Criar tarefa (ADMIN/GERENTE)
- `GET /tarefas` - Listar todas as tarefas
- `GET /tarefas/{id}` - Buscar tarefa por ID
- `GET /tarefas/projeto/{projetoId}` - Listar tarefas por projeto
- `GET /tarefas/responsavel/{responsavelId}` - Listar tarefas por responsável
- `GET /tarefas/status/{status}` - Listar tarefas por status
- `PUT /tarefas/{id}` - Atualizar tarefa
- `DELETE /tarefas/{id}/cancelar` - Cancelar tarefa

## 🎯 Regras de Negócio Implementadas

### Projetos
- Apenas ADMIN ou GERENTE podem criar projetos
- O criador se torna o gerente responsável
- Apenas o gerente responsável ou ADMIN podem editar/desativar
- Projetos inativos não podem receber novas tarefas
- Não é possível reativar projetos desativados

### Tarefas
- Apenas ADMIN ou GERENTE podem criar tarefas
- COLABORADOR pode atualizar apenas suas próprias tarefas
- Status possíveis: PENDENTE, EM_ANDAMENTO, CONCLUIDA, CANCELADA
- Prioridades: BAIXA, MEDIA, ALTA
- Tarefas CANCELADAS não podem ser marcadas como CONCLUIDAS
- Tarefas só podem ser criadas em projetos ativos

### Usuários
- Apenas usuários ativos podem fazer login
- Senhas são criptografadas com BCrypt
- Email deve ser único no sistema

## 🔧 Configurações

### application.properties

```properties
# Aplicação
spring.application.name=gestao-tarefas

# Banco H2
spring.datasource.url=jdbc:h2:mem:gestao-tarefas
spring.datasource.username=sa
spring.datasource.password=root
spring.h2.console.enabled=true

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
```

### Ambiente de Testes

O arquivo `application-test.properties` configura um banco H2 separado para testes:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
```

## 🐛 Troubleshooting

### Erro: "Port 8080 already in use"
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Erro: "JWT token expired"
O token JWT tem validade de 24 horas. Faça login novamente para obter um novo token.

### Console H2 não abre
Verifique se `spring.h2.console.enabled=true` está configurado no `application.properties`.
