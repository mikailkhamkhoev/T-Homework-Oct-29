package academy.backend.market_pulse.client;

import java.util.List;

import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Клиент к {@code instruments-service} T-Invest API (за моковым сервером на занятии — см. «План
 * семинара.md», семинар 7). Один интерфейс, несколько реализаций (см. пакет
 * {@code client.impl} — {@code HttpClientTInvestApiClient} опорная, плюс Feign/OkHttp на выбор
 * студента на этапе 3) — вызывающий код не знает и не должен знать, какая из них используется.
 */
public interface TInvestApiClient {

    /**
     * Текущий набор инструментов с котировками. Бросает {@link TInvestApiException} при сетевой
     * ошибке, неожиданном статусе ответа или битом JSON — вызывающий код решает, что делать
     * дальше, а не получает {@code null} или пустой список на месте ошибки.
     */
    List<Quote> fetchQuotes();
}
