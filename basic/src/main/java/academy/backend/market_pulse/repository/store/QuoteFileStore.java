package academy.backend.market_pulse.repository.store;

import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.model.Quote;

/**
 * Контракт хранения котировок в файле — реализуется студентами на этапе 6 (см. «План семинара.md»,
 * семинар 7). Формат файла не навязывается (JSON, CSV, построчный текст — на выбор), важен только
 * контракт: что сохранили, то и прочитали. Проверяется тестом {@code QuoteFileStoreContractTestBase}.
 */
public interface QuoteFileStore {

    /** Полностью перезаписывает файл по пути {@code path} текущим набором котировок. */
    void save(Path path, List<Quote> quotes);

    /** Читает котировки из файла по пути {@code path}; пустой список, если файла ещё нет. */
    List<Quote> load(Path path);
}
