package academy.backend.market_pulse.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Контракт, которому должна соответствовать любая реализация {@link TInvestApiClient} — не только
 * опорная {@code HttpClientTInvestApiClient}, но и клиенты на Feign/OkHttp, которые студенты пишут
 * на этапе 3 (см. «План семинара.md», семинар 7). Наследники подставляют свою реализацию в
 * {@link #createClient}; сам тест — против изолированного WireMock-сервера, без Docker и без
 * реального мокового сервера с занятия.
 *
 * <p>Если вы реализуете {@code TInvestApiClient} самостоятельно — скопируйте этот файл, замените
 * {@link #createClient} на вызов конструктора своего клиента и запустите тест на себе.
 */
public abstract class TInvestApiClientContractTestBase {

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    protected abstract TInvestApiClient createClient(TInvestApiProperties properties);

    private TInvestApiProperties propertiesForMock() {
        return new TInvestApiProperties(wireMock.baseUrl(), Duration.ofSeconds(2), Duration.ofSeconds(2));
    }

    @Test
    void fetchQuotes_parsesInstrumentsFromResponse() {
        wireMock.stubFor(get(urlEqualTo("/v1/instruments")).willReturn(okJson("""
                [
                  {"ticker": "SBER", "name": "Сбер", "type": "STOCK", "currency": "RUB", "price": 250.00, "changePercent": 1.20},
                  {"ticker": "OFZ26233", "name": "ОФЗ 26233", "type": "BOND", "currency": "RUB", "price": 780.00, "changePercent": -0.15}
                ]
                """)));

        List<Quote> quotes = createClient(propertiesForMock()).fetchQuotes();

        assertEquals(2, quotes.size());
        Quote sber = quotes.get(0);
        assertEquals("SBER", sber.getInstrument().getTicker());
        assertEquals("STOCK", sber.getInstrument().getType());
        assertEquals(0, new BigDecimal("250.00").compareTo(sber.getPrice()));
        assertEquals(0, new BigDecimal("1.20").compareTo(sber.getChangePercent()));
    }

    @Test
    void fetchQuotes_throwsOnServerError() {
        wireMock.stubFor(get(urlEqualTo("/v1/instruments")).willReturn(aResponse().withStatus(500)));

        TInvestApiClient client = createClient(propertiesForMock());
        assertThrows(TInvestApiException.class, client::fetchQuotes);
    }

    @Test
    void fetchQuotes_handlesEmptyCatalog() {
        wireMock.stubFor(get(urlEqualTo("/v1/instruments")).willReturn(okJson("[]")));

        List<Quote> quotes = createClient(propertiesForMock()).fetchQuotes();

        assertEquals(0, quotes.size());
    }

    @Test
    void fetchQuotes_sendsMockScenarioHeaderWhenConfigured() {
        wireMock.stubFor(get(urlEqualTo("/v1/instruments")).willReturn(okJson("[]")));

        createClient(propertiesForMock().withMockScenario("error")).fetchQuotes();

        wireMock.verify(getRequestedFor(urlEqualTo("/v1/instruments"))
                .withHeader(TInvestApiProperties.MOCK_SCENARIO_HEADER, equalTo("error")));
    }

    @Test
    void fetchQuotes_omitsMockScenarioHeaderByDefault() {
        wireMock.stubFor(get(urlEqualTo("/v1/instruments")).willReturn(okJson("[]")));

        createClient(propertiesForMock()).fetchQuotes();

        wireMock.verify(getRequestedFor(urlEqualTo("/v1/instruments"))
                .withoutHeader(TInvestApiProperties.MOCK_SCENARIO_HEADER));
    }
}
