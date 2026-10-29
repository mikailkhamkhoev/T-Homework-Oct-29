package academy.backend.market_pulse.client.impl;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;

/**
 * Опорная реализация {@link TInvestApiClient} — пишется вживую семинаристом (см. «План семинара.md»,
 * семинар 7, этап 2). {@link HttpClient} создаётся один раз в конструкторе и переиспользуется на все
 * запросы — повторное TCP/TLS-рукопожатие на каждый вызов было бы дорогим и бессмысленным, раз клиент
 * всё равно стучится в один и тот же адрес. Запросы и ответы логируются (этап 2) — {@link Logger} из
 * стандартной библиотеки, без внешнего логгера: тема семинара — сеть, а не выбор логирующего фреймворка.
 */
public class HttpClientTInvestApiClient implements TInvestApiClient {

    private static final Logger LOGGER = System.getLogger(HttpClientTInvestApiClient.class.getName());

    private final HttpClient httpClient;
    private final TInvestApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpClientTInvestApiClient(TInvestApiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

    @Override
    public List<Quote> fetchQuotes() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/v1/instruments"))
                .timeout(properties.getRequestTimeout())
                .header("Accept", "application/json")
                .GET();
        if (properties.getMockScenario() != null) {
            requestBuilder.header(TInvestApiProperties.MOCK_SCENARIO_HEADER, properties.getMockScenario());
        }
        HttpRequest request = requestBuilder.build();

        LOGGER.log(Level.INFO, "-> {0} {1}", request.method(), request.uri());

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "-> {0} {1}: ошибка соединения — {2}",
                    request.method(), request.uri(), e.getMessage());
            throw new TInvestApiException("Не удалось выполнить запрос к instruments-service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TInvestApiException("Запрос к instruments-service прерван", e);
        }

        LOGGER.log(Level.INFO, "<- {0} {1}: статус {2}, тело {3} байт",
                request.method(), request.uri(), response.statusCode(), response.body().length());

        if (response.statusCode() != 200) {
            throw new TInvestApiException(
                    "instruments-service вернул неожиданный статус: " + response.statusCode());
        }

        List<InstrumentQuoteDto> dtos;
        try {
            dtos = objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, InstrumentQuoteDto.class));
        } catch (IOException e) {
            throw new TInvestApiException("Не удалось разобрать ответ instruments-service", e);
        }

        return dtos.stream().map(this::toQuote).toList();
    }

    private Quote toQuote(InstrumentQuoteDto dto) {
        Instrument instrument = InstrumentFactories.create(
                dto.getType(), dto.getTicker(), dto.getName(), Currency.valueOf(dto.getCurrency()));
        return new Quote(instrument, dto.getPrice(), dto.getChangePercent());
    }
}
