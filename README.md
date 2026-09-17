# Mundo Viagens

Aplicação de reservas com React/TypeScript, API Java 21/Spring Boot e PostgreSQL no Supabase.

O navegador chama a API Java; somente a API acessa o banco. Cadastro e login usam JWT da própria aplicação e senhas BCrypt em `app_users`. **Supabase Auth não é usado** e não é necessário colocar `anon key` ou `service_role key` no frontend.

## Executar com Supabase

Pré-requisitos: Node.js 22.9+ e Java 21. Maven é usado se estiver instalado; caso contrário, o Maven Wrapper baixa a versão necessária. A primeira instalação precisa de acesso à internet.

```sh
npm ci
npm run setup
```

O setup cria `.env` com um segredo JWT aleatório e preserva um arquivo existente. Esse arquivo é ignorado pelo Git.

1. Crie ou abra seu projeto Supabase e vá a **Connect → Session pooler**.
2. No `.env` da raiz, preencha `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. Copie o host e o usuário exibidos pelo painel; a senha é a senha do banco, não a da conta Supabase.
3. Use a conexão JDBC na porta **5432**, com TLS:

```dotenv
DB_URL=jdbc:postgresql://SEU_HOST.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.SEU_PROJECT_REF
DB_PASSWORD="sua-senha-do-banco"
```

O pooler de sessão permite usar redes IPv4 e conexões persistentes da API. Não use o pooler de transações na porta 6543 nesta configuração. Se usar conexão direta, copie o host e o usuário dessa opção no painel e confirme a conectividade da sua rede. Para validar também o certificado do servidor, use `sslmode=verify-full` e configure o certificado raiz indicado pelo Supabase. Consulte a [documentação de conexões do Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres).

Inicie em dois terminais, na raiz do projeto:

```sh
npm run api:dev
```

```sh
npm run dev
```

Abra **http://localhost:3000**. O Vite encaminha `/api` para `http://localhost:8080`. O comando da API carrega o `.env` da raiz automaticamente. Para mudar a porta da API, defina `PORT` e ajuste `API_PROXY_TARGET`.

Alternativa para executar apenas a API com Docker:

```sh
docker compose up --build
```

O Compose conecta ao Supabase; ele não cria um PostgreSQL local. O frontend continua sendo iniciado com `npm run dev`.

## Banco e migrações

Na primeira inicialização, Flyway cria o schema `mundo_viagens` e aplica as migrações de `backend/src/main/resources/db/migration`. Hibernate valida o resultado; não apaga nem recria tabelas.

Tabelas: `app_users`, `hoteis`, `reservas`, `cancelamentos`, `flight_routes` e `flyway_schema_history`.

Mantenha `mundo_viagens` fora dos schemas expostos na Data API do Supabase. Não são necessárias policies para acesso direto do navegador: as permissões de clientes e funcionários são verificadas pela API Java. Credenciais `DB_*` e `JWT_SECRET` pertencem somente ao servidor; nenhuma delas deve receber o prefixo `VITE_`.

Se você já usava uma versão anterior com dados no schema `public`, o novo schema não importa esses dados automaticamente. Faça backup e migre as tabelas **junto com `flyway_schema_history`** para `mundo_viagens` antes de iniciar a nova versão. Não edite migrações já aplicadas nem habilite `baseline-on-migrate` para ocultar divergências. `DB_SCHEMA` permite manter outro schema privado quando necessário.

## Primeiro acesso administrativo

Cadastre sua conta pela interface. Depois, execute no SQL Editor do seu próprio projeto Supabase, substituindo o e-mail:

```sql
UPDATE mundo_viagens.app_users
SET role = 'DONA'
WHERE email = 'seu-email@exemplo.com';
```

Atualize a página ou entre novamente. Acesse **Painel Admin** para cadastrar hotéis, consultar reservas, confirmar pagamentos simulados e bloquear/desbloquear clientes. Os perfis `ATENDENTE`, `GERENTE` e `DONA` têm as permissões administrativas implementadas no projeto. Novos cadastros sempre recebem `CLIENTE`.

Os hotéis podem usar nomes como `Rio de Janeiro` ou `Rio de Janeiro (GIG)`. A comparação ignora acentos, letras maiúsculas e o sufixo do aeroporto.

## Funcionalidades e limites

- Busca por origem, destino e data; sem data, procura voos para daqui a sete dias.
- Rotas demonstrativas: São Paulo → Rio, São Paulo → Salvador, Rio → Miami e São Paulo → Lisboa.
- Reservas com passagem e hospedagem opcional de **3 diárias**, a partir da chegada; uma reserva ocupa um assento e, se houver hotel, um quarto.
- Preços e disponibilidade são calculados no servidor. Reservas pendentes também ocupam capacidade; cancelamento libera vagas. Bloqueios no banco evitam vender a última vaga simultaneamente.
- Cartão confirma a reserva automaticamente **em simulação**. Boleto fica pendente até confirmação pela agência. Não há cobrança real, boleto bancário nem integração real com companhia aérea.
- Cancelamento antes da partida: multa de 20% se faltarem menos de 48 horas. Três cancelamentos nos últimos 30 dias bloqueiam novas reservas; o cliente mantém acesso ao histórico e a agência pode desbloqueá-lo.
- O total e os dados de voo/hotel ficam registrados na reserva, mesmo que o hotel seja alterado depois.

## Verificações

```sh
npm run lint       # TypeScript em modo estrito
npm test           # Cliente HTTP e retorno após autenticação
npm run build      # Verificação de tipos e build de produção
npm run api:test   # Integração da API, migrações, permissões e concorrência
npm run api:build  # Testes e JAR em backend/target
```

Os testes Java usam H2 em modo PostgreSQL e um schema separado no banco em memória. Não acessam o Supabase nem exigem suas credenciais. Eles não substituem a validação da conexão com seu projeto Supabase.

## Publicação

Publique `dist/` em um serviço de arquivos estáticos com fallback das rotas para `index.html`. Publique o JAR ou o container Java em um serviço com Java 21 e configure as variáveis de servidor do `.env.example`.

Se frontend e API tiverem domínios diferentes, defina `VITE_API_URL=https://sua-api.exemplo.com/api` **antes de gerar o build** e `CORS_ORIGINS=https://seu-site.exemplo.com` na API. Se mantiver `VITE_API_URL=/api`, o servidor que hospeda o frontend precisa encaminhar `/api` à API; o proxy do Vite só existe durante desenvolvimento e preview.

## Problemas comuns

- **Não conecta ao banco:** confira host, porta 5432, usuário completo do pooler, senha do banco, projeto ativo e restrições de rede do Supabase.
- **Frontend não conecta à API:** verifique se `npm run api:dev` iniciou, confira `API_PROXY_TARGET` e reinicie o Vite após alterar o `.env`.
- **Resposta inválida da API:** `VITE_API_URL` ou o proxy está retornando HTML em vez de JSON.
- **Maven Wrapper não baixa:** verifique acesso a `repo.maven.apache.org` ou instale Maven; os scripts preferem o Maven instalado.
- **Painel administrativo não aparece:** confirme a alteração de `role` no banco correto e atualize a página.
- **Hotel não aparece:** confira a cidade do hotel e o destino do voo. A disponibilidade de quartos é validada ao confirmar a reserva.
