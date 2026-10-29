package academy.backend.market_pulse.client.impl;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiClientContractTestBase;
import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Прогон общего контракта ({@link TInvestApiClientContractTestBase}) на эталонной OkHttp-реализации
 * — сверка для семинариста, студентам заранее не показывать (см. «План семинара.md», семинар 7,
 * этап 3).
 */
class OkHttpTInvestApiClientReferenceTest extends TInvestApiClientContractTestBase {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new OkHttpTInvestApiClientReference(properties);
    }
}
