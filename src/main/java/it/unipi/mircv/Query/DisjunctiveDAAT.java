package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Index.PostingListBlock;

import java.io.IOException;
import java.util.ArrayList;

public class DisjunctiveDAAT {
    public DisjunctiveDAAT() throws IOException {
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        MinHeapScores heapScores = new MinHeapScores();

    }
    public ArrayList<Integer> DAAT() throws IOException {

        float currentDocScore;
        int minDocId;

        int count = 0;//DEBUG
        while ((minDocId = getMinDocId()) != this.collectionSize) {
            currentDocScore = 0;
            System.out.println("minDocId: " + minDocId);
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            int documentLength = documentIndexHandler.readDocumentLength(minDocId);
            for (int i =0; i<numTermQuery;i++) {
                PostingListBlock postingListBlock = postingListBlocks.get(i);
                if (postingListBlock.getCurrentDocId() == minDocId) {

                    currentTf = postingListBlock.getCurrentTf();
                    //currentDocScore += docPartialScore(currentTf); //questa era la versione originale dove usavamo le frequency per lo score
                    currentDocScore += computeBM25(currentTf,documentLength,docFreqs[i]);

                    //increment the position in the posting list
                    if(postingListBlock.next() == -1){         //increment position and if end of block reached then set the flag
                        endOfPostingListBlockFlag[i] = true;
                    }
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore, minDocId);
            updatePostingListBlocks();


            if(count == 0) { //DEBUG
                for(PostingListBlock plb: postingListBlocks){
                    System.out.print("Print postingListBlocks");
                    System.out.println(plb);
                }
            }
            count++; //DEBUG
        }

        //System.out.println("DEBUGGG --> " + heapScores.getDocId((float) 3.1066878)); //DEBUG
        return heapScores.getTopDocIdReversed();
    }
}
