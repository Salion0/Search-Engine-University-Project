package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.PostingListBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

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
    private final boolean[] endOfPostingListFlag;
    private final PriorityQueue<Integer> priorityQueue; // Create a priority queue of integers
    private final HashSet<Integer> uniqueSet; // Create a set to keep track of unique elements

    public DisjunctiveDAAT(String[] queryTerms) throws IOException {
        documentIndexHandler = new DocumentIndexHandler();
        LexiconHandler lexiconHandler = new LexiconHandler();
        invertedIndexHandler = new InvertedIndexHandler();

        priorityQueue = new PriorityQueue<>();
        uniqueSet = new HashSet<>();

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

            numBlockRead[i]++;
            insertElement(postingListBlocks[i].getCurrentDocId()); // for the PriorityQueue version
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
        Integer minDocId;
        int count = 0;//DEBUG

        //while ((minDocId = getMinDocId()) != collectionSize)
        while ((minDocId = priorityQueue.poll()) != null)
        {
            uniqueSet.remove(minDocId);
            currentDocScore = 0;
            //System.out.println("minDocId: " + minDocId); //DEBUG
            //System.out.println("Priority Queue: " + priorityQueue);
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            int documentLength = documentIndexHandler.readDocumentLength(minDocId);
            for (int i =0; i<numTermQuery;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    currentTf = postingListBlocks[i].getCurrentTf();
                    currentDocScore += ScoreFunction.BM25(currentTf, documentLength, docFreqs[i]);
                    if(endOfPostingListFlag[i] == false) // version with PriorityQueue
                    {
                        if (postingListBlocks[i].next() == -1)
                            updatePostingListBlock(i);
                        insertElement(postingListBlocks[i].getCurrentDocId());
                    } // end of version with PriorityQueue
                    //if(endOfPostingListFlag[i] == false && postingListBlocks[i].next() == -1)  //increment position and if end of block reached then set the flag
                    //    updatePostingListBlock(i);
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore , minDocId);
        }

        //System.out.println("DEBUGGG --> " + heapScores.getDocId((float) 3.1066878)); //DEBUG
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

    private void insertElement(int element) {
        if (!uniqueSet.contains(element)) {
            priorityQueue.add(element);
            uniqueSet.add(element);
        }
    }
}
