# az-func-feedback-report

Azure Function responsável pela geração periódica do relatório semanal de feedbacks da plataforma.

Esta função consulta os feedbacks registrados no banco de dados, consolida as informações relevantes para análise administrativa e armazena o relatório gerado em formato JSON no Azure Blob Storage.

## Objetivo

O módulo `az-func-feedback-report` atende ao requisito de geração de relatório semanal dos feedbacks recebidos pela plataforma.

O relatório contém:

- Período analisado;
- Total de avaliações recebidas;
- Média das notas;
- Quantidade de avaliações por dia;
- Quantidade de avaliações por urgência;
- Lista dos feedbacks considerados no período, contendo descrição, nota, urgência e data de envio.

## Responsabilidade da Function

A função `func-feedback-report` possui uma responsabilidade única: gerar e armazenar o relatório semanal dos feedbacks.

Ela não cria avaliações e não altera os feedbacks existentes. A função apenas consulta os dados já gravados pela aplicação principal, gera um consolidado para acompanhamento administrativo e o envia via e-mail.

## Funcionamento geral

O fluxo da função é:

```text
TimerTrigger
    ↓
func-feedback-report
    ↓
Consulta feedbacks da última semana no banco de dados
    ↓
Calcula total, média, quantidade por dia e quantidade por urgência
    ↓
Serializa o relatório em JSON
    ↓
Armazena o arquivo no Azure Blob Storage
```

## TimerTrigger

A anotação `@TimerTrigger` define a execução automática da função com base em uma expressão NCRONTAB.

No Azure Functions, a expressão NCRONTAB possui seis campos:

```text
{second} {minute} {hour} {day} {month} {day-of-week}
```

## Agenda de execução

A função `func-feedback-report` utiliza o seguinte agendamento:

```text
0 0 3 * * 6
```

Essa expressão significa:

```text
Segundo: 0
Minuto: 0
Hora: 3 UTC
Dia do mês: qualquer dia
Mês: qualquer mês
Dia da semana: sábado
```

Como o Azure Functions interpreta expressões NCRONTAB em UTC por padrão, a função foi configurada para executar às `03:00 UTC`, equivalente a `00:00` em `America/Sao_Paulo`.

Dessa forma, o relatório semanal é gerado todo sábado à meia-noite no horário de São Paulo.

## Período considerado no relatório

A cada execução, a função considera o intervalo entre o início do dia atual em São Paulo e uma semana antes.

Exemplo de execução em um sábado:

```text
Início: sábado anterior, 00:00 America/Sao_Paulo
Fim: sábado atual, 00:00 America/Sao_Paulo
```

Esse modelo evita que o relatório dependa do segundo exato em que a função foi executada e garante um intervalo semanal consistente.

## Nome do arquivo gerado

O relatório é armazenado no Azure Blob Storage em formato JSON.

O nome do arquivo segue o padrão:

```text
reports/weekly/relatorio-semanal-feedbacks-{inicio}-{fim}.json
```

Exemplo:

```text
reports/weekly/relatorio-semanal-feedbacks-20260510-000000-20260517-000000.json
```

## Formato do relatório

O relatório gerado possui estrutura semelhante a:

```json
{
  "inicio": "2026-05-10T00:00:00-03:00",
  "fim": "2026-05-17T00:00:00-03:00",
  "totalAvaliacoes": 3,
  "mediaAvaliacoes": 6.0,
  "quantidadePorDia": {
    "2026-05-10": 1,
    "2026-05-11": 2
  },
  "quantidadePorUrgencia": {
    "BAIXA": 1,
    "MEDIA": 1,
    "ALTA": 1
  },
  "feedbacks": [
    {
      "id": "00000000-0000-0000-0000-000000000000",
      "descricao": "Aula excelente",
      "nota": 10,
      "urgencia": "BAIXA",
      "dataEnvio": "2026-05-10T10:00:00-03:00"
    }
  ]
}
```

## Configurações

As principais configurações utilizadas pela função são:

```properties
app.reports.storage.connection-string
app.reports.storage.container-name

quarkus.datasource.db-kind
quarkus.datasource.jdbc.url
quarkus.datasource.username
quarkus.datasource.password

quarkus.azure.keyvault.secret.endpoint
```

Em ambiente de nuvem, os valores sensíveis devem ser obtidos por variáveis de ambiente ou Azure Key Vault.

Em ambiente local de desenvolvimento, o projeto pode utilizar banco em memória e Azurite para simular o Azure Blob Storage.

## Execução local

Para compilar o projeto e executar os testes:

```bash
mvn clean package
```

Para rodar apenas os testes:

```bash
mvn test
```

Para iniciar a aplicação em modo de desenvolvimento:

```bash
mvn quarkus:dev
```

## Validação do build da Azure Function

Após executar o build:

```bash
mvn clean package
```

é possível validar se os arquivos da Azure Function foram gerados procurando por:

```text
function.json
host.json
```

No PowerShell:

```powershell
Get-ChildItem -Recurse target -Include function.json,host.json
```

O arquivo `function.json` deve conter uma configuração do tipo `timerTrigger` para a função `func-feedback-report`.

## Testes

O projeto possui testes unitários para validar:

- Geração do relatório semanal com feedbacks;
- Geração de relatório vazio quando não há feedbacks no período;
- Cálculo da média das avaliações;
- Agrupamento de avaliações por dia;
- Agrupamento de avaliações por urgência;
- Validação de datas inválidas;
- Serialização e armazenamento do relatório por meio de portas simuladas.

Para executar:

```bash
mvn test
```

## Observabilidade

Durante a execução, a função registra logs com as principais informações do processo:

- Início do período analisado;
- Fim do período analisado;
- Informações do TimerTrigger;
- Total de avaliações processadas;
- Nome do blob gerado;
- URL do blob armazenado.

Esses logs auxiliam na validação da execução em ambiente cloud e na demonstração do funcionamento da solução.

## Estrutura principal

```text
src/main/java/br/com/fiap/techchallenge/report
├── application
│   ├── dto
│   ├── ports
│   └── usecase
├── domain
│   ├── enums
│   └── model
└── infrastructure
    ├── config
    ├── cron
    ├── persistence
    ├── report
    └── storage
```

## Função principal

A função principal do módulo é:

```text
func-feedback-report
```

Localização:

```text
src/main/java/br/com/fiap/techchallenge/report/infrastructure/cron/GenerateReportFunction.java
```

Ela é acionada automaticamente pelo `TimerTrigger` e executa o caso de uso responsável por gerar, serializar e armazenar o relatório semanal.

## Tecnologias utilizadas

- Java;
- Quarkus;
- Azure Functions;
- TimerTrigger;
- Azure Blob Storage;
- Azure Key Vault;
- JPA/Panache;
- Maven;
- JUnit 5.

## Papel no Tech Challenge

Este módulo compõe a solução serverless da plataforma de feedback, atendendo ao requisito de geração de relatórios periódicos para apoiar a análise dos administradores sobre a satisfação dos alunos.

Ele complementa a função de recebimento de avaliações e a rotina de notificação de feedbacks críticos, mantendo a separação de responsabilidades entre os componentes da aplicação.
