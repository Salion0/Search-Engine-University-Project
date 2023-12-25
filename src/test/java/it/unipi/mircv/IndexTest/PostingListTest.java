package it.unipi.mircv.IndexTest;


import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingList;
import org.junit.jupiter.api.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostingListTest {
    static PostingList pl;

    @Test
    void getBytesTest(){

        //create a posting list
        pl = new PostingList();
        pl.addPostingElement(new PostingElement(1, 1));
        pl.addPostingElement(new PostingElement(2, 2));
        pl.addPostingElement(new PostingElement(3, 3));
        pl.addPostingElement(new PostingElement(4, 4));
        pl.addPostingElement(new PostingElement(5, 5));

        //get the posting list data
        byte[][] postingListData = pl.getBytes();
        ByteBuffer docIdsBuffer = ByteBuffer.wrap(postingListData[0]);
        ByteBuffer termFreqsBuffer = ByteBuffer.wrap(postingListData[1]);

        //create two arrays
        ArrayList<Integer> docIdsArray = new ArrayList<>();
        ArrayList<Integer> termFreqsArray = new ArrayList<>();

        //fill the arrays
        for(int i =0;i<postingListData[0].length/4;i++){
            docIdsArray.add(docIdsBuffer.getInt());
            termFreqsArray.add(termFreqsBuffer.getInt());
        }

        
        //assert length
        assertEquals(20, postingListData[0].length);
        System.out.println("Length test assert: TRUE");

        //assert docIds
        for(int i=0;i<postingListData[0].length/4;i++){
            assertEquals(i+1, docIdsArray.get(i));
        }
        System.out.println("Doc ids test assert: TRUE");

        //assert termFreqs
        for(int i=0;i<postingListData[0].length/4;i++){
            assertEquals(i+1, termFreqsArray.get(i));
        }
        System.out.println("Term freqs test assert: TRUE");




    }

}
