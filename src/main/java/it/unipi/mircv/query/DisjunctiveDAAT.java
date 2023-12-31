package it.unipi.mircv.query;

import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.PostingListBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static it.unipi.mircv.utility.Config.*;
import static it.unipi.mircv.utility.Parameters.*;

public class DisjunctiveDAAT {
    private final int numTermQuery;
    private final InvertedIndexFileHandler invertedIndexHandler;
    private final PostingListBlock[] postingListBlocks;
    private final int[] numBlockRead;
    private final int[] docFreqs;
    private final int[] offsets;
    private final boolean[] endOfPostingListFlag;
    private MinHeapScores heapScores;
    private boolean invalidConstruction;

    public DisjunctiveDAAT(String[] queryTerms) throws IOException {
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        invertedIndexHandler = new InvertedIndexFileHandler();

        //--------------------DEFINE ARRAYS------------------------//
        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs =  new int[numTermQuery];
        offsets = new int[numTermQuery];
        numBlockRead = new int[numTermQuery];
        endOfPostingListFlag = new boolean[numTermQuery];

        //-------------INITIALIZE TERM STATISTICS------------------//
        for (int i = 0; i < numTermQuery; i++)
        {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            if(entryBuffer == null){ //if the ith term is not present in lexicon
                System.out.println(queryTerms[i] + " is not inside the index");
                invalidConstruction = true;
                break;
            }
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);

            if(POSTING_LIST_BLOCK_LENGTH > docFreqs[i]) //if posting list length is less than the block size
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i],docFreqs[i]);
            else  //else posting list length is greather than block size
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i],POSTING_LIST_BLOCK_LENGTH);

            numBlockRead[i] = 1;
        }
        lexiconHandler.close();
    }

    private int getMinDocId() {
        int minDocId = collectionSize;  //value that is greater than any doc id

        //find the current min doc id in the posting lists of the query terms
        for (int i = 0; i < numTermQuery; i++){
            if (endOfPostingListFlag[i]) continue;
            int currentDocId = postingListBlocks[i].getCurrentDocId();
            if(currentDocId<minDocId){
                minDocId = currentDocId;
            }
        }
        return minDocId;
    }

    public ArrayList<Integer> processQuery() throws IOException {
        if(invalidConstruction){
            invertedIndexHandler.close();
            return new ArrayList<>(0);
        }
        heapScores = new MinHeapScores();
        float currentDocScore;
        int minDocId;

        while ((minDocId = getMinDocId()) != collectionSize)
        {
            currentDocScore = 0;
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            int documentLength = docsLen[minDocId];
            for (int i =0; i<numTermQuery;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    currentTf = postingListBlocks[i].getCurrentTf();
                    switch (scoreType){
                        case BM25 ->
                                currentDocScore += ScoreFunction.BM25(currentTf, documentLength, docFreqs[i]);
                        case TFIDF ->
                                currentDocScore += ScoreFunction.TFIDF(currentTf, docFreqs[i]);
                    }

                    if(!endOfPostingListFlag[i] && postingListBlocks[i].next() == -1)  //increment position and if end of block reached then set the flag
                        updatePostingListBlock(i);
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore , minDocId);
        }
        invertedIndexHandler.close();
        return heapScores.getTopDocIdReversed();
    }

    private void updatePostingListBlock(int i) throws IOException {
        /*--------------------------------------
            update the posting list block
            by reading the next block if exist
         --------------------------------------*/
        // read the subsequent block
        int elementToRead = docFreqs[i] - POSTING_LIST_BLOCK_LENGTH*numBlockRead[i];

        //check if exist a subsequent block using the docFreqs which is equal to
        // the length of the posting list
        if (elementToRead > 0)
        {
            if(elementToRead > POSTING_LIST_BLOCK_LENGTH )
                elementToRead = POSTING_LIST_BLOCK_LENGTH;

            postingListBlocks[i] = invertedIndexHandler.getPostingList(
                    offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]), elementToRead);

            numBlockRead[i]++; //increment the number of block read
        }
        else
            endOfPostingListFlag[i]=true;

    }

    public MinHeapScores getHeapScores() {
        return heapScores;
    }
}
