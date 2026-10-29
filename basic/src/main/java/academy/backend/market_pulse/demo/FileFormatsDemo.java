package academy.backend.market_pulse.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Разговор про форматы перед самостоятельной работой (см. «План семинара.md», семинар 7, этап 4):
 * один и тот же набор данных (тикер → цена) сохраняется и читается четырьмя разными способами —
 * сырые байты, JSON, YAML, CSV. Показывает, что выбор формата — это выбор инструмента под формат
 * данных и потребителя (бинарный протокол, конфиг для человека, таблица для Excel), а не вопрос
 * вкуса.
 */
public class FileFormatsDemo {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) throws IOException {
        Map<String, String> prices = Map.of("SBER", "250.00", "GAZP", "120.50", "LKOH", "7100.00");

        System.out.println("1. InputStream/OutputStream — сырые байты, без знания о формате:");
        rawBytesRoundTrip();

        System.out.println();
        System.out.println("2. Jackson ObjectMapper — JSON:");
        jsonRoundTrip(prices);

        System.out.println();
        System.out.println("3. Jackson ObjectMapper — YAML (тот же API, другой YAMLFactory):");
        yamlRoundTrip(prices);

        System.out.println();
        System.out.println("4. Apache Commons CSV — сторонняя библиотека, не велосипед на split(\",\"):");
        csvRoundTrip(prices);
    }

    /**
     * {@link InputStream}/{@link OutputStream} ничего не знают о структуре данных — это поток
     * байт. Разбор JSON/YAML/CSV строится поверх них (Reader/Writer — поверх символов, форматы —
     * поверх Reader/Writer).
     */
    private static void rawBytesRoundTrip() throws IOException {
        Path path = Files.createTempFile("file-formats-demo", ".bin");
        byte[] payload = "SBER;250.00".getBytes();
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(payload);
        }
        byte[] readBack;
        try (InputStream in = Files.newInputStream(path)) {
            readBack = in.readAllBytes();
        }
        System.out.println("   Записали/прочитали байт: " + readBack.length + " -> " + new String(readBack));
        Files.deleteIfExists(path);
    }

    private static void jsonRoundTrip(Map<String, String> prices) throws IOException {
        Path path = Files.createTempFile("file-formats-demo", ".json");
        JSON.writeValue(path.toFile(), prices);
        System.out.println("   " + Files.readString(path).strip());
        Map<?, ?> readBack = JSON.readValue(path.toFile(), Map.class);
        System.out.println("   Прочитано ключей: " + readBack.size());
        Files.deleteIfExists(path);
    }

    private static void yamlRoundTrip(Map<String, String> prices) throws IOException {
        Path path = Files.createTempFile("file-formats-demo", ".yaml");
        YAML.writeValue(path.toFile(), prices);
        System.out.println("   " + Files.readString(path).strip().replace("\n", " | "));
        Map<?, ?> readBack = YAML.readValue(path.toFile(), Map.class);
        System.out.println("   Прочитано ключей: " + readBack.size());
        Files.deleteIfExists(path);
    }

    private static void csvRoundTrip(Map<String, String> prices) throws IOException {
        Path path = Files.createTempFile("file-formats-demo", ".csv");
        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(path),
                CSVFormat.DEFAULT.builder().setHeader("ticker", "price").build())) {
            for (Map.Entry<String, String> entry : prices.entrySet()) {
                printer.printRecord(entry.getKey(), entry.getValue());
            }
        }
        List<CSVRecord> records;
        try (CSVParser parser = CSVParser.parse(path,
                java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            records = parser.getRecords();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.out.println("   Прочитано строк: " + records.size()
                + ", первая: " + records.get(0).get("ticker") + "=" + records.get(0).get("price"));
        Files.deleteIfExists(path);
    }
}
