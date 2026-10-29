package academy.backend.market_pulse.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PriceHistoryStoreReferenceTest {

    private final PriceHistoryStore store = new PriceHistoryStoreReference();

    @Test
    void eachAppendCreatesANewFile(@TempDir Path tempDir) {
        PricePoint first = new PricePoint("SBER", Instant.ofEpochMilli(1_000), new BigDecimal("250.00"));
        PricePoint second = new PricePoint("SBER", Instant.ofEpochMilli(2_000), new BigDecimal("251.50"));

        Path firstFile = store.append(tempDir, first);
        Path secondFile = store.append(tempDir, second);

        assertTrue(java.nio.file.Files.exists(firstFile));
        assertTrue(java.nio.file.Files.exists(secondFile));
        assertEquals(firstFile.getParent(), secondFile.getParent());

        List<PricePoint> loaded = store.load(tempDir, "SBER");
        assertEquals(List.of(first, second), loaded);
    }

    @Test
    void loadingUnknownTickerReturnsEmptyList(@TempDir Path tempDir) {
        assertTrue(store.load(tempDir, "UNKNOWN").isEmpty());
    }
}
