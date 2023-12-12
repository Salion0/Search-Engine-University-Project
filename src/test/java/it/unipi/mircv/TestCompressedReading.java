package it.unipi.mircv;

import org.junit.jupiter.api.Assertions;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;


public class TestCompressedReading {
    static BufferedReader readerForCompressedCase;
    static BufferedReader reader;
    static FileInputStream fis;
    static GZIPInputStream gzis;
    static InputStreamReader inputStreamReader;
    public static void compressedRead(String[] args) throws IOException {
        fis = new FileInputStream("collection.tar.gz");
        gzis = new GZIPInputStream(fis);
        inputStreamReader = new InputStreamReader(gzis, StandardCharsets.UTF_8);
        readerForCompressedCase = new BufferedReader(inputStreamReader); // reader for the compressed version
        reader = new BufferedReader(new FileReader("collection.tsv")); // reader for the uncompressed version

        compareCollectionReading();
    }

    public static void compareCollectionReading() throws IOException {
        readerForCompressedCase.mark(1024); // 1024 represents how many bytes it can read before it becomes no longer valid
        String[] values = readerForCompressedCase.readLine().split("\t"); // to check for metadata
        readerForCompressedCase.reset(); // bring back the reader to the first line
        readerForCompressedCase.skip(values[0].length() - 1); // skip metadata

        singlePassInMemoryIndexing();
    }

    private static void singlePassInMemoryIndexing() throws IOException {

        int count = 0;
        while (true)
        {
            System.out.println(count++);
            String lineCompressedCase = readerForCompressedCase.readLine();
            String lineUncompressedCase = reader.readLine();
            if (lineUncompressedCase == null && lineCompressedCase.startsWith("\0\0\0\0\0"))
            {
                //we reached the end of the file -> close file readers and break
                reader.close();
                readerForCompressedCase.close();
                break;
            }
            Assertions.assertEquals(lineUncompressedCase,lineCompressedCase);
        }

        System.out.println("\ntest on the Compressed Reading of the collection --> SUCCESSFUL");
    }
}
