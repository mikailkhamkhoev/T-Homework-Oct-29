package academy.backend.market_pulse.client.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import feign.Feign;
import feign.Request;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;

/**
 * ЭТАЛОН для сверки по ходу этапа 3 (см. «План семинара.md», семинар 7) — студентам заранее не
 * показывать, это один из двух вариантов самостоятельной работы (второй — OkHttp,
 * {@link OkHttpTInvestApiClientReference}). Реализует тот же контракт {@link TInvestApiClient},
 * что и опорный {@link HttpClientTInvestApiClient}, поэтому проходит тот же
 * {@code TInvestApiClientContractTest}.
 */
public class FeignTInvestApiClientReference implements TInvestApiClient {

    /** Декларативный контракт Feign — метод вместо ручной сборки {@code HttpRequest}. */
    private interface InstrumentsServiceApi {
        @RequestLine("GET /v1/instruments")
        List<InstrumentQuoteDto> fetchInstruments();
    }

    private final InstrumentsServiceApi api;

    public FeignTInvestApiClientReference(TInvestApiProperties properties) {
        this.api = Feign.builder()
                .decoder(new JacksonDecoder(new ObjectMapper()))
                .options(new Request.Options(
                        properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                        properties.getRequestTimeout().toMillis(), TimeUnit.MILLISECONDS,
                        true))
                .requestInterceptor(template -> {
                    if (properties.getMockScenario() != null) {
                        template.header(TInvestApiProperties.MOCK_SCENARIO_HEADER, properties.getMockScenario());
                    }
                })
                .target(InstrumentsServiceApi.class, properties.getBaseUrl());
    }

    @Override
    public List<Quote> fetchQuotes() {
        try {
            return api.fetchInstruments().stream().map(this::toQuote).toList();
        } catch (feign.RetryableException e) {
            throw new TInvestApiException("Не удалось выполнить запрос к instruments-service", e);
        } catch (feign.FeignException e) {
            throw new TInvestApiException("instruments-service вернул ошибку: " + e.status(), e);
        }
    }

    private Quote toQuote(InstrumentQuoteDto dto) {
        Instrument instrument = InstrumentFactories.create(
                dto.getType(), dto.getTicker(), dto.getName(), Currency.valueOf(dto.getCurrency()));
        return new Quote(instrument, dto.getPrice(), dto.getChangePercent());
    }
}
