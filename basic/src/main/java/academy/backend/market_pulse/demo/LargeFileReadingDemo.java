package academy.backend.market_pulse.demo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Демонстрация к риторическому вопросу этапа 7 (см. «План семинара.md», семинар 7): «а что, если
 * файл большой?». {@code Files.readAllLines} читает файл целиком в память — на файле в несколько
 * гигабайт это легко упирается в {@code OutOfMemoryError} ещё до того, как можно будет хоть что-то
 * с данными сделать. {@code Files.lines()} отдаёт ленивый {@code Stream<String>}, читающий файл
 * построчно по мере обхода, — в памяти в любой момент времени только одна строка, а не весь файл.
 * Не внедряется в проект — только демонстрация, котировки в проекте маленькие.
 */
public class LargeFileReadingDemo {

    public static void main(String[] args) throws IOException {
        Path path = args.length > 0 ? Path.of(args[0]) : createSampleFile();

        System.out.println("1. Files.readAllLines — весь файл целиком в List<String> в куче:");
        long linesAllAtOnce = Files.readAllLines(path).size();
        System.out.println("   Прочитано строк: " + linesAllAtOnce);

        System.out.println();
        System.out.println("2. Files.lines() — построчный Stream, файл не загружается в память целиком:");
        try (var lines = Files.lines(path)) {
            long count = lines.count(); // терминальная операция — конвейер отрабатывает лениво, строка за строкой
            System.out.println("   Прочитано строк: " + count);
        }
    }

    private static Path createSampleFile() {
        try {
            Path path = Files.createTempFile("large-file-demo", ".txt");
            path.toFile().deleteOnExit();
            try (var writer = Files.newBufferedWriter(path)) {
                for (int i = 0; i < 100_000; i++) {
                    writer.write("SBER;STOCK;Сбер;RUB;250.00;1.20");
                    writer.newLine();
                }
            }
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
