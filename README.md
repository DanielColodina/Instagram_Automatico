# Instagram Automático

Aplicação Java com Spring Boot que gera uma arte pela OpenAI, aplica uma logo oficial e publica automaticamente em uma conta profissional do Instagram.

## Recursos

- Geração de imagem pela API da OpenAI
- Aplicação automática da logo oficial
- Publicação pela Instagram API
- Agendamento configurável com fuso horário
- Controle persistente contra publicações duplicadas
- Execução com Docker Compose

## Configuração

Copie `config/secrets.example.properties` para `config/secrets.properties` e preencha suas próprias credenciais. O arquivo real de credenciais é ignorado pelo Git.

A imagem precisa estar disponível por HTTPS no endereço definido em `app.public-base-url`. O exemplo do Nginx está em `deploy/nginx-generated.conf`.

## Executar

```bash
mvn clean verify
docker compose up -d --build
```

O Docker Compose ativa o agendador. A execução direta permanece com o agendador desativado por segurança.

## Segurança

Credenciais, imagens geradas, logs, arquivos de estado e chaves privadas não fazem parte deste repositório.
