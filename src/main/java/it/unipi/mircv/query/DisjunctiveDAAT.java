package it.unipi.mircv.query;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.PostingListBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static it.unipi.mircv.Config.*;

public class DisjunctiveDAAT {
    private final int numTermQuery;
    private final DocumentIndexFileHandler documentIndexHandler;
    private final InvertedIndexFileHandler invertedIndexHandler;
    private final PostingListBlock[] postingListBlocks;
    private final int[] numBlockRead;
    private final int[] docFreqs;
    private final int[] offsets;
    private final boolean[] endOfPostingListFlag;

    public DisjunctiveDAAT(String[] queryTerms) throws IOException {
        documentIndexHandler = new DocumentIndexFileHandler();
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
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);

            if(POSTING_LIST_BLOCK_LENGTH > docFreqs[i]) //if posting list length is less than the block size
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i],docFreqs[i]);
            else  //else posting list length is greather than block size
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i],POSTING_LIST_BLOCK_LENGTH);

            numBlockRead[i] = 1;
        }
    }

    private int getMinDocId() {
        int minDocId = collectionSize;  //valore che indica che le posting list sono state raggiunte

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
        MinHeapScores heapScores = new MinHeapScores();
        float currentDocScore;
        int minDocId;
        int count = 0;//DEBUG

        while ((minDocId = getMinDocId()) != collectionSize)
        {
            currentDocScore = 0;
            //System.out.println("minDocId: " + minDocId); //DEBUG
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            //AIUDOO
            int documentLength = documentIndexHandler.readDocumentLength(minDocId);
            //int documentLength = docsLen[minDocId];
            for (int i =0; i<numTermQuery;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    currentTf = postingListBlocks[i].getCurrentTf();
                    currentDocScore += ScoreFunction.BM25(currentTf, documentLength, docFreqs[i]);
                    //currentDocScore += ScoreFunction.computeTFIDF(currentTf, docFreqs[i]);

                    if(endOfPostingListFlag[i] == false && postingListBlocks[i].next() == -1)  //increment position and if end of block reached then set the flag
                        updatePostingListBlock(i);
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore , minDocId);
        }

        return heapScores.getTopDocIdReversed();
    }

    //TODO cambiare la logica di questo metodo con la stessa che c'è in conjunctive DAAT in processQuery
    private void updatePostingListBlock(int i) throws IOException {
        // read the subsequent block
        int elementToRead = docFreqs[i] - POSTING_LIST_BLOCK_LENGTH*numBlockRead[i];
        //System.out.println(elementToRead); //DEBUG

        //check if exist a subsequent block using the docFreqs which is equal to the length of the posting list
        if (elementToRead > 0)
        {
            if(elementToRead > POSTING_LIST_BLOCK_LENGTH )
                elementToRead = POSTING_LIST_BLOCK_LENGTH;

            postingListBlocks[i] = invertedIndexHandler.getPostingList(
                    offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]), elementToRead);

            //System.out.println(this.invertedIndexHandler.getPostingList(offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]), elementToRead)); //DEBUG
            numBlockRead[i]++; // ho letto un altro blocco quindi aumento il campo
        }
        else
            endOfPostingListFlag[i]=true;

    }
}
