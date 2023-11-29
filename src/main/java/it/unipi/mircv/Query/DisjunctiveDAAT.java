package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.PostingListBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static it.unipi.mircv.Config.POSTING_LIST_BLOCK_LENGTH;
import static it.unipi.mircv.Config.collectionSize;

public class DisjunctiveDAAT {
    private final int numTermQuery;
    private final DocumentIndexHandler documentIndexHandler;
    private final InvertedIndexHandler invertedIndexHandler;
    private PostingListBlock[] postingListBlocks;
    private final int[] numBlockRead;
    private final int[] docFreqs;
    private final int[] offsets;
    private final boolean[] endOfPostingListBlockFlag;
    private final boolean[] endOfPostingListFlag;

    public DisjunctiveDAAT(String[] queryTerms) throws IOException {
        documentIndexHandler = new DocumentIndexHandler();
        LexiconHandler lexiconHandler = new LexiconHandler();
        invertedIndexHandler = new InvertedIndexHandler();

        //--------------------DEFINE ARRAYS------------------------//
        numTermQuery = queryTerms.length;
        numBlockRead = new int[numTermQuery];

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs =  new int[numTermQuery];
        offsets = new int[numTermQuery];

        endOfPostingListBlockFlag = new boolean[numTermQuery];
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
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i],POSTING_LIST_BLOCK_LENGTH));

            numBlockRead[i]++;
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

        while ((minDocId = getMinDocId()) != collectionSize) {
            currentDocScore = 0;
            System.out.println("minDocId: " + minDocId);
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            int documentLength = documentIndexHandler.readDocumentLength(minDocId);
            for (int i =0; i<numTermQuery;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    currentTf = postingListBlocks[i].getCurrentTf();
                    currentDocScore += ScoreFunction.BM25(currentTf, documentLength, docFreqs[i]);
                    //increment the position in the posting list
                    if(postingListBlocks[i].next() == -1)  //increment position and if end of block reached then set the flag
                        endOfPostingListBlockFlag[i] = true;
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore , minDocId);
            updatePostingListBlocks();
        }

        //System.out.println("DEBUGGG --> " + heapScores.getDocId((float) 3.1066878)); //DEBUG
        return heapScores.getTopDocIdReversed();
    }

    //TODO cambiare la logica di questo metodo con la stessa che c'è in conjunctive DAAT in processQuery
    private void updatePostingListBlocks() throws IOException {
        for(int i=0; i<numTermQuery; i++){
            if (docFreqs[indexTerm] - readElement < blockSize) {
                postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                        offsets[indexTerm] + readElement,
                        docFreqs[indexTerm] - readElement
                );
            }
            else {
                postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                        offsets[indexTerm] + readElement,
                        blockSize
                );
            }
        }
    }

    protected void uploadPostingListBlock(int indexTerm, int readElement, int blockSize) throws IOException {
        //Upload the posting list block
        //if the element to read are less in size than "blockSize", read the remaining elements
        //otherwise read a posting list block of size "blockSize"

        if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        }
        else {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize
            );
        }
    }
}
