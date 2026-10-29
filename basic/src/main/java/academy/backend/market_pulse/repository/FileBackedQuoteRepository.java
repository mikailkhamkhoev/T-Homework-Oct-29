package academy.backend.market_pulse.repository;

import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Кеширующий доступ к котировкам, сохранённым на диске (см. «План семинара.md», семинар 7,
 * этап 8). Читает файл один раз и держит результат в памяти — {@link #all()} не ходит на диск на
 * каждый вызов, а только после {@link #invalidate()}, вызываемого {@link QuoteFileWatcher} при
 * изменении файла. Без этого кеша разговор про инвалидацию был бы ни о чём.
 */
public class FileBackedQuoteRepository {

    private final QuoteFileStore store;
    private final Path path;
    private volatile List<Quote> cache;

    public FileBackedQuoteRepository(QuoteFileStore store, Path path) {
        this.store = store;
        this.path = path;
    }

    public List<Quote> all() {
        List<Quote> snapshot = cache;
        if (snapshot == null) {
            synchronized (this) {
                snapshot = cache;
                if (snapshot == null) {
                    snapshot = store.load(path);
                    cache = snapshot;
                }
            }
        }
        return snapshot;
    }

    /** Перезаписывает файл и сразу обновляет кеш — следующий {@link #all()} не идёт на диск. */
    public void save(List<Quote> quotes) {
        store.save(path, quotes);
        cache = quotes;
    }

    /**
     * Сбрасывает кеш: следующий {@link #all()} перечитает файл с диска. Вызывается
     * {@link QuoteFileWatcher} при обнаружении внешнего изменения файла — например, если файл
     * обновили не через {@link #save}, а другим процессом или вручную.
     */
    public void invalidate() {
        cache = null;
    }

    public Path getPath() {
        return path;
    }
}
