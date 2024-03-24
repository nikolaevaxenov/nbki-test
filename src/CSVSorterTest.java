import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVSorterTest {
    @BeforeEach
    public void setUp() {
        String tempDirectory = "csv-temp";
        File tempDir = new File(tempDirectory);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
    }

    @Test
    void testCSVSorter() throws IOException {
        List<String> testData = new ArrayList<>();
        testData.add("8, \"Buff\",      \"Bif\",        \"632-79-9939\", 46.0,    20.0,    30.0,    40.0,    50.0,   \"B+\"");
        testData.add("2, \"Alfred\",    \"University\", \"123-12-1234\", 41.0,    97.0,    96.0,    97.0,    48.0,   \"D+\"");
        testData.add("1, \"Alfalfa\",   \"Aloysius\",   \"123-45-6789\", 40.0,    90.0,   100.0,    83.0,    49.0,   \"D-\"");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("grades-test.csv"))) {
            for (String line : testData) {
                writer.write(line);
                writer.newLine();
            }
        }

        CSVSorter sorter = new CSVSorter();
        sorter.start("grades-test.csv");

        List<String> sortedData = readDataFromFile();

        Collections.sort(testData);

        assertEquals(testData, sortedData);
    }

    @AfterEach
    void tearThis(){
        File testFile = new File("grades-test.csv");
        File testFileSorted = new File("grades-test-sorted.csv");
        testFile.delete();
        testFileSorted.delete();
    }

    private List<String> readDataFromFile() throws IOException {
        List<String> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("grades-test-sorted.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
        }
        return data;
    }
}