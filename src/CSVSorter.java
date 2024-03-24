import java.io.*;
import java.util.*;

public class CSVSorter {
    private int chunkMaxSize = 1024;
    private String outputFile = "output.csv";

    public CSVSorter(int chunkMaxSize) {
        this.chunkMaxSize = chunkMaxSize;
    }

    public CSVSorter() {
    }

    private List<File> splitToFiles(String inputFile) {
        this.outputFile = new File(inputFile).getName().substring(0, inputFile.lastIndexOf(".")) + "-sorted.csv";

        List<File> tempFiles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int fileCounter = 0;
            while ((line = reader.readLine()) != null) {
                PriorityQueue<String> sortedLines = new PriorityQueue<>((o1, o2) -> {
                    int id1 = Integer.parseInt(o1.split(",")[0]);
                    int id2 = Integer.parseInt(o2.split(",")[0]);
                    return Integer.compare(id1, id2);
                });
                sortedLines.add(line);

                int chunkSize = line.length();
                while (chunkSize <= this.chunkMaxSize && (line = reader.readLine()) != null) {
                    sortedLines.add(line);
                    chunkSize += line.length();
                }

                File tempFile = new File("csv-temp", "tempfile-" + fileCounter + ".temp");
                tempFiles.add(tempFile);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    Iterator<String> it = sortedLines.iterator();
                    if (it.hasNext()) {
                        writer.write(Objects.requireNonNull(sortedLines.poll()));
                        while (it.hasNext()) {
                            writer.newLine();
                            writer.write(Objects.requireNonNull(sortedLines.poll()));
                        }
                    }
                }

                fileCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tempFiles;
    }

    private void merge(List<File> tempFiles) {
        PriorityQueue<BufferedReader> readers = new PriorityQueue<>(
                (br1, br2) -> {
                    try {
                        br1.mark(this.chunkMaxSize);
                        br2.mark(this.chunkMaxSize);
                        String line1 = br1.readLine();
                        String line2 = br2.readLine();
                        br1.reset();
                        br2.reset();

                        if (line1 == null || line2 == null) {
                            return line1 == null ? -1 : 1;
                        }

                        int id1 = Integer.parseInt(line1.split(",")[0]);
                        int id2 = Integer.parseInt(line2.split(",")[0]);

                        return Integer.compare(id1, id2);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        for (File tempFile : tempFiles) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(tempFile));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            readers.add(reader);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile))) {
            while (!readers.isEmpty()) {
                BufferedReader reader = readers.poll();
                String line = reader.readLine();
                if (line != null) {
                    writer.write(line);
                    writer.newLine();
                    readers.add(reader);
                } else {
                    reader.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanUp() {
        File[] tempFiles = new File("csv-temp").listFiles();
        if (tempFiles != null) {
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
        }
    }

    public void start(String inputFile) {
        merge(splitToFiles(inputFile));
        cleanUp();
    }
}
