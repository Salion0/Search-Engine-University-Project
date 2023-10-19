package it.unipi.mircv;
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


        byte[] buffer = null;
        int termByteLength = 64;
        int offsetByteLength = 4;
        int elementOfListByteLength = 4;
        int bytesToRead = 64; // Specify the number of bytes you want to read
        int offset = 0;
        int currentOffset = 0;
        int followingOffset = 0;

        ArrayList<String> terms = new ArrayList<String>();
        ArrayList<PostingList> postingList = new ArrayList<>();

        buffer = new byte[bytesToRead];

        FileInputStream docIdFileInputStream = new FileInputStream(directoryPath + docIdPath + "0.dat");
        BufferedInputStream docIdBufferedInputStream = new BufferedInputStream(docIdFileInputStream);

        FileInputStream lexiconFileInputStream = new FileInputStream(directoryPath + lexiconPath + "0.dat");
        BufferedInputStream lexiconBufferedInputStream = new BufferedInputStream(lexiconFileInputStream);

        FileInputStream termFreqFileInputStream = new FileInputStream(directoryPath + termFreqPath + "0.dat");
        BufferedInputStream termFreqBufferedInputStream = new BufferedInputStream(termFreqFileInputStream);

        int count = 0;
        while (true) {

            /*bytesRead = lexiconBufferedInputStream.read(buffer, offsetIncrement, termByteLength); //leggo il primo int

            if (bytesRead == -1) {
                System.out.println("File finito");
                return;
            }*/

            RandomAccessFile lexiconFile = new RandomAccessFile(directoryPath + lexiconPath + "0.dat", "r");
            RandomAccessFile docIdFile = new RandomAccessFile(directoryPath + docIdPath + "0.dat", "r");
            RandomAccessFile termFreqFile = new RandomAccessFile(directoryPath + termFreqPath + "0.dat", "r");

            // Position (offset) we start reading from
            offset = 164; // We start reading from position 0
            // Seek to the desired position
            docIdFile.seek(offset);
            // Read data from that position
            byte[] bufferTest = new byte[4]; // Define a buffer to hold the data
            int numberOfBytesRead = docIdFile.read(bufferTest);

            System.out.println("Numero di bytes letti: " + numberOfBytesRead);

            offset += termByteLength;

            int result = ByteBuffer.wrap(bufferTest).getInt();
            System.out.println("docId: " + result);
            docIdFile.close();
            if (count == 0) break;

            /*
            currentOffset = lexiconBufferedInputStream.read(buffer, offsetIncrement, offsetIncrement + offsetByteLength);
            followingOffset = lexiconBufferedInputStream.read(buffer, offsetIncrement + termByteLength, offsetIncrement + offsetByteLength);

            postingList.add(count,readPostingList(currentOffset,followingOffset,buffer,docIdBufferedInputStream,termFreqBufferedInputStream));
        */}

    }

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


