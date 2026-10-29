package academy.backend.market_pulse.repository;

import academy.backend.market_pulse.exception.DuplicateTickerException;
import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

/**
 * Хранилище инструментов поверх внутреннего массива фиксированного размера. Перебор — через
 * {@link Iterable}/{@link Iterator}, без раскрытия массива наружу. Заглушка: подключение
 * реального источника данных запланировано на семинар 5.
 */
public class InstrumentRepository implements Iterable<Instrument> {

    private final Map<String, Instrument> instruments = new HashMap<>();

    public InstrumentRepository() {
        // Стартовые данные для демонстрации CLI: без них каждый запуск начинается с пустого списка.
        add(new Stock("SBER", "Сбер", Currency.RUB, "Финансы", new BigDecimal("6.5")));
        add(new Bond("OFZ26233", "ОФЗ 26233", Currency.RUB, new BigDecimal("8.5"), 2036));
        add(new Etf("FXUS", "FinEx FXUS", Currency.USD, "S&P 500"));
    }

    public void add(Instrument instrument) {
        if (instruments.containsKey(instrument.getTicker())) {
            throw new DuplicateTickerException(instrument.getTicker());
        }
        instruments.put(instrument.getTicker(), instrument);
    }

    public Optional<Instrument> findByTicker(String ticker) {
        for (Instrument instrument : this) {
            if (instrument.getTicker().equalsIgnoreCase(ticker)) {
                return Optional.of(instrument);
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterator<Instrument> iterator() {
        return instruments.values().iterator();
    }

    public Stream<Instrument> stream() {
        return instruments.values().stream();
    }
}
