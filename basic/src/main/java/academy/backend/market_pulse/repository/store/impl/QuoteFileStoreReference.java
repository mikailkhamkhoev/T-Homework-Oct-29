package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import academy.backend.market_pulse.repository.store.QuoteFileStore;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;

/**
 * ЭТАЛОН для сверки по ходу этапа 6 (см. «План семинара.md», семинар 7) — студентам заранее не
 * показывать, это самостоятельная работа. Формат — построчный текст,
 * {@code TICKER;TYPE;NAME;CURRENCY;PRICE;CHANGE_PERCENT} на строку; простой и читаемый глазами без
 * инструментов, но не выдерживает значения с {@code ;} внутри (для учебного проекта — приемлемо,
 * название инструмента таких символов не содержит).
 */
public class QuoteFileStoreReference implements QuoteFileStore {

    private static final String DELIMITER = ";";

    @Override
    public void save(Path path, List<Quote> quotes) {
        List<String> lines = quotes.stream().map(this::toLine).toList();
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить котировки в " + path, e);
        }
    }

    @Override
    public List<Quote> load(Path path) {
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Quote> quotes = new ArrayList<>(lines.size());
            for (String line : lines) {
                if (!line.isBlank()) {
                    quotes.add(toQuote(line));
                }
            }
            return quotes;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать котировки из " + path, e);
        }
    }

    private String toLine(Quote quote) {
        Instrument instrument = quote.getInstrument();
        return String.join(DELIMITER,
                instrument.getTicker(),
                instrument.getType(),
                instrument.getName(),
                instrument.getCurrency().name(),
                quote.getPrice().toPlainString(),
                quote.getChangePercent().toPlainString());
    }

    private Quote toQuote(String line) {
        String[] parts = line.split(DELIMITER, -1);
        String ticker = parts[0];
        String type = parts[1];
        String name = parts[2];
        Currency currency = Currency.valueOf(parts[3]);
        BigDecimal price = new BigDecimal(parts[4]);
        BigDecimal changePercent = new BigDecimal(parts[5]);

        Instrument instrument = InstrumentFactories.create(type, ticker, name, currency);
        return new Quote(instrument, price, changePercent);
    }
}
