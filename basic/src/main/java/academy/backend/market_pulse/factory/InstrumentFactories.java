package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.factory.impl.BondFactory;
import academy.backend.market_pulse.factory.impl.EtfFactory;
import academy.backend.market_pulse.factory.impl.StockFactory;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.util.Registry;

import java.util.ServiceLoader;

/**
 * Реестр фабрик инструментов. Реализации {@link InstrumentFactory} не регистрируют себя вручную —
 * они обнаруживаются через {@link ServiceLoader} по записи в
 * {@code META-INF/services/academy.backend.market_pulse.factory.InstrumentFactory}. Добавление
 * нового типа инструмента не требует правки этого класса — только новая реализация и строка в
 * файле сервиса.
 */
public final class InstrumentFactories {

    private static final Registry<String, InstrumentFactory> REGISTRY = new Registry<>();

    static {
        // Статический блок фабрики выполняется только при загрузке её класса — форсируем загрузку,
        // иначе реестр останется пустым (тот же нюанс, что и с DriverManager до JDBC 4.0).
        loadClass(StockFactory.class);
        loadClass(BondFactory.class);
        loadClass(EtfFactory.class);
    }

    private InstrumentFactories() {
    }

    public static void register(String type, InstrumentFactory factory) {
        REGISTRY.register(type.toUpperCase(), factory);
    }

    public static Instrument create(String type, String ticker, String name, Currency currency) {
        InstrumentFactory factory = REGISTRY.find(type.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Unknown instrument type: " + type));
        return factory.create(ticker, name, currency);
    }

    private static void loadClass(Class<?> factoryClass) {
        try {
            Class.forName(factoryClass.getName());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}