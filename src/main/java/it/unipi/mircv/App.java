package it.unipi.mircv;
import it.unipi.mircv.indexing.Index;
import it.unipi.mircv.indexing.PostingElement;
import it.unipi.mircv.indexing.PostingList;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class App
{
    public static void main( String[] args ) throws IOException {
        //testBlock(0);
        Index index = new Index("test_collection.tsv");
    }

    public static void testBlock(int numberOfBlocks) throws IOException {

        String directoryPath = "data./"; // Replace with your file path
        String docIdPath = "docIds";
        String lexiconPath = "lexicon";
        String termFreqPath = "termFreq";


        byte[] buffer = null;
        int bytesRead = 4;
        int termByteLength = 64;
        int offsetByteLength = 4;
        int elementOfListByteLength = 4;
        int bytesToRead = 64; // Specify the number of bytes you want to read
        int offsetIncrement = 0;
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
            bytesRead = lexiconBufferedInputStream.read(buffer, offsetIncrement, offsetIncrement + termByteLength); //leggo il primo int

            if (bytesRead == -1) {
                System.out.println("File finito");
                return;
            }

            System.out.println(buffer.length);
            String term = new String(buffer,StandardCharsets.UTF_8);
            terms.add(term);
            //terms.add(count,String.valueOf(lexiconBufferedInputStream.read(buffer, offsetIncrement, bytesRead + offsetIncrement)));
            offsetIncrement += termByteLength;
            System.out.println(terms.get(0));
            if (count == 0) return;
            currentOffset = lexiconBufferedInputStream.read(buffer, offsetIncrement, offsetIncrement + offsetByteLength);
            followingOffset = lexiconBufferedInputStream.read(buffer, offsetIncrement + termByteLength, offsetIncrement + offsetByteLength);

            postingList.add(count,readPostingList(currentOffset,followingOffset,buffer,docIdBufferedInputStream,termFreqBufferedInputStream));
        }

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
}


