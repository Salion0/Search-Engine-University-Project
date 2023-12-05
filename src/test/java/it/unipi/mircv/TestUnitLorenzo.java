package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Index.PostingElement;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Query.ScoreFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static it.unipi.mircv.Config.collectionSize;

import org.junit.jupiter.api.Assertions;


public class TestUnitLorenzo {
    static PostingListBlock[] postingListBlocks = new PostingListBlock[5];
    static boolean[] endOfPostingListFlag = new boolean[5];

    public static void main(String[] args) throws IOException {
        collectionSize = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            postingListBlocks[i] = new PostingListBlock();
            endOfPostingListFlag[i] = false;
        }



        postingListBlocks[0].addPostingElement(new PostingElement(2,4));
        postingListBlocks[0].addPostingElement(new PostingElement(5,1));
        postingListBlocks[0].addPostingElement(new PostingElement(6,3));
        postingListBlocks[0].setFields(3);

        postingListBlocks[1].addPostingElement(new PostingElement(1,2));
        postingListBlocks[1].addPostingElement(new PostingElement(2,3));
        postingListBlocks[1].addPostingElement(new PostingElement(24,7));
        postingListBlocks[1].setFields(3);

        postingListBlocks[2].addPostingElement(new PostingElement(7,2));
        postingListBlocks[2].addPostingElement(new PostingElement(10,6));
        postingListBlocks[2].addPostingElement(new PostingElement(11,2));
        postingListBlocks[2].setFields(3);

        postingListBlocks[3].addPostingElement(new PostingElement(2,5));
        postingListBlocks[3].addPostingElement(new PostingElement(4,5));
        postingListBlocks[3].addPostingElement(new PostingElement(5,3));
        postingListBlocks[3].setFields(3);

        postingListBlocks[4].addPostingElement(new PostingElement(1,4));
        postingListBlocks[4].addPostingElement(new PostingElement(7,1));
        postingListBlocks[4].addPostingElement(new PostingElement(9,1));
        postingListBlocks[4].setFields(3);

        int[] arraysOfMinDocIds = {1,2,4,5,6,7,9,10,11,24};
        int[] docFreqs = {3,3,3,3,3};
        int[] arraysOfResults = new int[arraysOfMinDocIds.length];

        int minDocId;
        int count = 0;
        while((minDocId = getMinDocId()) != collectionSize) {
            arraysOfResults[count] = minDocId;
            for (int i =0; i<5;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    docFreqs[i]--;
                    if (docFreqs[i] == 0)
                        endOfPostingListFlag[i] = true;
                    else
                        postingListBlocks[i].next();
                }
            }
            count++;
        }



        // Assertion to check if arrays are equal
        Assertions.assertArrayEquals(arraysOfResults, arraysOfMinDocIds); //, "Arrays are not equal");


    }


    private static int getMinDocId() {
        int minDocId = collectionSize;  //valore che indica che le posting list sono state raggiunte

        //find the current min doc id in the posting lists of the query terms
        for (int i = 0; i < 5; i++){
            if (endOfPostingListFlag[i]) continue;
            int currentDocId = postingListBlocks[i].getCurrentDocId();
            if(currentDocId<minDocId){
                minDocId = currentDocId;
            }
        }
        return minDocId;
    }
}
