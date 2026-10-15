import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * File input/output for DineSync (supporting feature, CO5/CO6).
 *  - reads the starting menu from data/menu.csv (skips malformed records, falls back if missing)
 *  - appends an audit log of important events
 *  - exports reports as text files and reads them back
 * IMPORTANT: restaurant records (orders, bills, customers) are NOT saved to files.
 * They live in arrays in memory and reset when the program restarts.
 */
public class FileStore {
    private final Path outputDir;
    private final List<String> loadMessages = new ArrayList<>();   // generic, type-safe list of Strings

    public FileStore(String outputFolder) {
        this.outputDir = Paths.get(outputFolder);
    }

    public Path getOutputDir() { return outputDir; }

    public List<String> getLoadMessages() { return loadMessages; }

    /** Looks for a data file next to the program: ./data, ../data, or the current folder. */
    public static Path findDataFile(String fileName) {
        String[] candidates = {"data/" + fileName, "../data/" + fileName, fileName};
        for (String c : candidates) {
            Path p = Paths.get(c);
            if (Files.exists(p)) {
                return p;
            }
        }
        return Paths.get("data/" + fileName);   // does not exist; caller handles the missing file
    }

    /**
     * Reads dishes line by line: name,category,type,price,prepMinutes,available(y/n)
     * Lines starting with # are comments. Bad lines are reported and skipped, not fatal.
     * Returns the number of dishes loaded (0 if the file is missing).
     */
    public int loadMenuCsv(Path csv, MenuService menu) {
        Staff system = new Staff("A000", "System", Staff.ADMIN, "0000");
        int loaded = 0;
        int lineNo = 0;
        // try-with-resources: the reader is closed automatically, even if an exception happens
        try (BufferedReader reader = Files.newBufferedReader(csv)) {
            String line;
            while ((line = reader.readLine()) != null) {      // sentinel-controlled loop: null = end of file
                lineNo++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] f = line.split(",");
                if (f.length != 6) {
                    loadMessages.add("line " + lineNo + " skipped: expected 6 fields, found " + f.length);
                    continue;
                }
                try {
                    menu.addDish(system, f[0], Integer.parseInt(f[1].trim()), Integer.parseInt(f[2].trim()),
                            Double.parseDouble(f[3].trim()), Integer.parseInt(f[4].trim()),
                            f[5].trim().equalsIgnoreCase("y"));
                    loaded++;
                } catch (NumberFormatException e) {
                    loadMessages.add("line " + lineNo + " skipped: not a number (" + e.getMessage() + ")");
                } catch (DineSyncException e) {
                    loadMessages.add("line " + lineNo + " skipped: " + e.getMessage());
                }
            }
        } catch (NoSuchFileException e) {
            loadMessages.add("menu file not found: " + csv);
        } catch (IOException e) {
            loadMessages.add("could not read menu file: " + e.getMessage());
        }
        return loaded;
    }

    /** Appends one time-stamped line to audit.log. Returns false (and the program continues) on failure. */
    public boolean appendAudit(String event) {
        try {
            Files.createDirectories(outputDir);
            Path log = outputDir.resolve("audit.log");
            try (BufferedWriter w = Files.newBufferedWriter(log, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                w.write(DateHelper.today() + " " + DateHelper.nowTime() + " | " + event);
                w.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println(Colors.warn("audit log not written: " + e.getMessage()));
            return false;
        }
    }

    /** Writes (or replaces) a report file and returns its path. */
    public Path writeReport(String fileName, String text) throws IOException {
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve(fileName);
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write(text);
        }
        return file;
    }

    /** Reads a whole text file into a list of lines. */
    public List<String> readLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /** Reads file lines as a Stream and counts the lines containing a word (case-insensitive). */
    public long countLinesContaining(Path file, String word) throws IOException {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.filter(l -> l.toLowerCase().contains(word.toLowerCase())).count();
        }
    }

    /** Standalone demo: writes, appends and reads back files in a demo folder. */
    public static void main(String[] args) throws IOException {
        FileStore fs = new FileStore("dinesync-output");
        System.out.println(Colors.title("== FileStore (file I/O) - standalone demo =="));
        MenuService menu = new MenuService();
        int n = fs.loadMenuCsv(findDataFile("menu.csv"), menu);
        System.out.println("Dishes loaded from menu.csv: " + n);
        for (String m : fs.getLoadMessages()) {
            System.out.println("  note: " + m);
        }
        fs.appendAudit("FileStore demo run");
        Path p = fs.writeReport("demo_report.txt", "DineSync demo report\nLine with ALERT\nLast line\n");
        System.out.println("Wrote " + p + " -> lines: " + fs.readLines(p).size()
                + ", lines containing 'alert': " + fs.countLinesContaining(p, "alert"));
    }
}
