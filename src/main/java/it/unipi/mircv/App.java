package it.unipi.mircv;
import it.unipi.mircv.indexing.BlockMerger;
import it.unipi.mircv.indexing.Index;

import java.io.*;


public class App
{

    public static void main( String[] args ) throws IOException {
        Index index = new Index("test_collection.tsv");
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger(numberOfBlocks);
        blockMerger.mergeBlocks();

        System.out.println(index.getDocumentLength(0));
    }


    /*
    public void testWord() throws FileNotFoundException {

        int[] buffer = new int[64];

        String striga = "Hello World!";
        ByteBuffer stringaBuffer = ByteBuffer.allocate(64);
        stringaBuffer.put(striga.getBytes());
        FileOutputStream fos = new FileOutputStream("test.dat");
        fos.write(stringaBuffer.array());

        FileInputStream test = new FileInputStream("test.dat");
        BufferedInputStream testBuff = new BufferedInputStream(test);
        bytesRead = testBuff.read(buffer, offsetIncrement, offsetIncrement + termByteLength); //leggo il primo int

        String termTest = new String(buffer,StandardCharsets.UTF_8);
        System.out.println(termTest.charAt(10));
    }*/
}


