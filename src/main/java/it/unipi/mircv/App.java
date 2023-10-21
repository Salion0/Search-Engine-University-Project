package it.unipi.mircv;
import it.unipi.mircv.indexing.BlockReadingHandler;
import it.unipi.mircv.indexing.Index;
import it.unipi.mircv.indexing.PostingElement;
import it.unipi.mircv.indexing.PostingList;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class App
{
    public static void main( String[] args ) throws IOException {
        testBlock(0);
        //Index index = new Index("test_collection.tsv");
    }

    public static void testBlock(int numberOfBlocks) throws IOException {

        String directoryPath = "data./"; // Replace with your file path
        String docIdPath = "docIds";
        String lexiconPath = "lexicon";
        String termFreqPath = "termFreq";

        BlockReadingHandler block = new BlockReadingHandler(directoryPath,lexiconPath,docIdPath,termFreqPath,0);

        ArrayList<String> terms = new ArrayList<String>();
        ArrayList<PostingList> postingList = new ArrayList<>();

        int count = 0;
        while (true) {

            block.readLexiconFile();
            count++;
            if (count == 10) break;

        }
    }

    /*

    public static PostingList readPostingList(int currentOffset, int followingOffset, byte[] buffer, BufferedInputStream docIdBufferedInputStream, BufferedInputStream termFreqBufferedInputStream) throws IOException {

        PostingList postingList = new PostingList();

        //ArrayList<Integer> listDocIds = new ArrayList<Integer>();
        //ArrayList<Integer> listTermFreqs = new ArrayList<Integer>();
        int count = 0;
        for (int i = currentOffset; i < followingOffset; i++,count++) {
            PostingElement postingElement = new PostingElement(docIdBufferedInputStream.read(buffer, i, 1),
                    termFreqBufferedInputStream.read(buffer, i, 1));

            postingList.addPostingElement(postingElement);
        }

        return postingList;
    }


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


