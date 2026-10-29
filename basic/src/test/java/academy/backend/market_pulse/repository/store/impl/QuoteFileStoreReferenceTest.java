package academy.backend.market_pulse.repository.store.impl;

import academy.backend.market_pulse.repository.store.QuoteFileStore;
import academy.backend.market_pulse.repository.store.QuoteFileStoreContractTestBase;

/**
 * Прогон общего контракта ({@link QuoteFileStoreContractTestBase}) на эталонной реализации —
 * сверка для семинариста, студентам заранее не показывать (см. «План семинара.md», семинар 7,
 * этап 6).
 */
class QuoteFileStoreReferenceTest extends QuoteFileStoreContractTestBase {

    @Override
    protected QuoteFileStore createStore() {
        return new QuoteFileStoreReference();
    }
}
