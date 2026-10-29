package academy.backend.market_pulse.client.impl;

import java.io.IOException;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * ЭТАЛОН для сверки по ходу этапа 3 (см. «План семинара.md», семинар 7) — студентам заранее не
 * показывать, это один из двух вариантов самостоятельной работы (второй — Feign,
 * {@link FeignTInvestApiClientReference}). Реализует тот же контракт {@link TInvestApiClient}, что
 * и опорный {@link HttpClientTInvestApiClient}, поэтому проходит тот же
 * {@code TInvestApiClientContractTest}.
 */
public class OkHttpTInvestApiClientReference implements TInvestApiClient {

    private final OkHttpClient httpClient;
    private final TInvestApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OkHttpTInvestApiClientReference(TInvestApiProperties properties) {
        this.properties = properties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getRequestTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public List<Quote> fetchQuotes() {
        Request.Builder requestBuilder = new Request.Builder()
                .url(properties.getBaseUrl() + "/v1/instruments")
                .header("Accept", "application/json")
                .get();
        if (properties.getMockScenario() != null) {
            requestBuilder.header(TInvestApiProperties.MOCK_SCENARIO_HEADER, properties.getMockScenario());
        }
        Request request = requestBuilder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new TInvestApiException("instruments-service вернул неожиданный статус: " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new TInvestApiException("instruments-service вернул пустой ответ");
            }
            List<InstrumentQuoteDto> dtos = objectMapper.readValue(body.string(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, InstrumentQuoteDto.class));
            return dtos.stream().map(this::toQuote).toList();
        } catch (IOException e) {
            throw new TInvestApiException("Не удалось выполнить запрос к instruments-service", e);
        }
    }

    private Quote toQuote(InstrumentQuoteDto dto) {
        Instrument instrument = InstrumentFactories.create(
                dto.getType(), dto.getTicker(), dto.getName(), Currency.valueOf(dto.getCurrency()));
        return new Quote(instrument, dto.getPrice(), dto.getChangePercent());
    }
}
