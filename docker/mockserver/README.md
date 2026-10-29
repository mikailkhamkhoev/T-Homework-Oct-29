# Моковый instruments-service (Docker)

Имитирует ручку `GET /v1/instruments` T-Invest API для семинара 7 (см. «План семинара.md» в
`basic/docs/seminars/seminar7/`). Поведение переключается заголовком `X-Mock-Scenario`.

## Запуск

```bash
docker compose -f docker/mockserver/docker-compose.yml up -d
```

Поднимется на `http://localhost:1080` — ровно то, что подставлено по умолчанию в
`TInvestApiProperties.localMock()`.

## Проверка

```bash
curl http://localhost:1080/v1/instruments
curl -H "X-Mock-Scenario: error" http://localhost:1080/v1/instruments
curl -H "X-Mock-Scenario: timeout" http://localhost:1080/v1/instruments   # отвечает с задержкой 10с
```

Из Java-клиента сценарий включается переменной окружения `MARKET_PULSE_MOCK_SCENARIO`
(`error` | `timeout` | `ok`) — клиент отправляет её заголовком `X-Mock-Scenario`:

```
MARKET_PULSE_MOCK_SCENARIO=error java -cp ... academy.backend.market_pulse.demo.Main update
```

## Остановка

```bash
docker compose -f docker/mockserver/docker-compose.yml down
```

## Важно для семинариста

Поднять и прогнать все три `curl`-команды выше **заранее**, на той машине, с которой будете вести
занятие — не на самом семинаре. Этот `docker-compose.yml` и `expectations.json` не запускались и не
проверялись при подготовке (в среде, где готовился этот код, не было доступа к Docker-демону) —
перед использованием убедитесь, что `mockserver/mockserver:5.15.0` — актуальный тег образа, и что
формат `expectations.json` (ключ `delay`, вложенный `body.json`) совпадает с версией MockServer,
которую вы фактически поднимете.
