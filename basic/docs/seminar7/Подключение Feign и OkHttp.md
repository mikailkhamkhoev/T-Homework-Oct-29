# Подключение Feign и OkHttp

Раздаточный материал к этапу 3 семинара 7 (см. «План семинара.md»). Обе зависимости уже добавлены в
`pom.xml` проекта — ничего подключать не нужно, только написать класс. Выберите одну библиотеку, а
не обе.

Обе реализации должны реализовывать интерфейс `academy.backend.market_pulse.client.TInvestApiClient`
и обращаться к тому же мокового серверу (`GET {baseUrl}/v1/instruments`), что и опорная реализация
на `HttpClient` (`HttpClientTInvestApiClient`) — контракт один на всех.

## Feign

Декларативный клиент: вместо того чтобы вручную собирать запрос, описываете интерфейс с
аннотациями, а Feign сам генерирует реализацию.

```java
interface InstrumentsServiceApi {
    @RequestLine("GET /v1/instruments")
    List<InstrumentQuoteDto> fetchInstruments();
}

InstrumentsServiceApi api = Feign.builder()
        .decoder(new JacksonDecoder(new ObjectMapper()))
        .target(InstrumentsServiceApi.class, properties.getBaseUrl());
```

Дальше — `api.fetchInstruments()` возвращает уже разобранный список DTO; остаётся замапить каждый в
`Quote` через `InstrumentFactories.create(...)`, как это сделано в `HttpClientTInvestApiClient`.

Ошибки: `Feign` бросает `feign.FeignException` на HTTP-ошибки (4xx/5xx) и `feign.RetryableException`
на сетевые сбои (соединение не установлено) — оба стоит ловить и оборачивать в
`TInvestApiException`, как и в опорной реализации.

## OkHttp

Более низкоуровневый клиент — сами собираете запрос и разбираете ответ, зато полный контроль над
интерсепторами, таймаутами, пулом соединений.

```java
void example() {
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .readTimeout(properties.getRequestTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .build();

    Request request = new Request.Builder()
            .url(properties.getBaseUrl() + "/v1/instruments")
            .get()
            .build();

    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new TInvestApiException("instruments-service вернул неожиданный статус: " + response.code());
        }
        String json = response.body().string();
        // дальше — ObjectMapper.readValue(json, ...), как в HttpClient-варианте
    }
}
```

`Response` реализует `Closeable` — обязательно закрывать (`try-with-resources`), иначе соединение не
вернётся в пул.

## Проверка своей реализации

Скопируйте `src/test/java/academy/backend/market_pulse/client/TInvestApiClientContractTestBase.java` в
новый класс (например, `MyTInvestApiClientTest`), замените `createClient(...)` на вызов конструктора
своего класса и запустите тест. Он поднимает изолированный WireMock-сервер — Docker и моковый сервер
с занятия для этого не нужны.
