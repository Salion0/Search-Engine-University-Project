package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


import static it.unipi.mircv.Config.*;

public class QueryProcessor {

    //------------------FILE HANDLER-------------------------------------------//
    private final InvertedIndexHandler invertedIndexHandler;
    private final LexiconHandler lexiconHandler;
    private final DocumentIndexHandler documentIndexHandler;
    //------------------------------------------------------------------------//
    private int collectionSize;
    private float avgDocLen;
    private int  numTermQuery;
    //------------------------------------------------------------------------//
    private boolean[] endOfPostingListBlockFlag;
    private int [] numBlockRead;  //counter for keeping info on how many blocks for each posting list
    private boolean[] endOfPostingListFlag;
    private String[] queryTerms;//query terms
    private int[] docFreqs; //doc frequencies of query terms
    private int[] collectionFreqs; // collection frequencies of query terms
    private int[] offsets; // offsets of the posting list of query terms
    private ArrayList<PostingListBlock> postingListBlocks;

    //------------------------------------------------------------------------//

    public QueryProcessor(String query) throws IOException {

        //---------------INITIALIZE ARRAYS---------------------------
        this.queryTerms = query.split(" ");
        this.numTermQuery = queryTerms.length;
        this.numBlockRead = new int[numTermQuery];
        this.docFreqs =  new int[numTermQuery];
        this.collectionFreqs = new int[numTermQuery];
        this.offsets = new int[numTermQuery];
        this.endOfPostingListBlockFlag = new boolean[numTermQuery];
        this.endOfPostingListFlag = new boolean[numTermQuery];
        //-----------------------------------------------------------

        //-------------INITIALIZE FILE HANDLER ----------------------
        this.lexiconHandler = new LexiconHandler();
        this.documentIndexHandler = new DocumentIndexHandler();
        this.invertedIndexHandler = new InvertedIndexHandler();
        //-----------------------------------------------------------

        //-------------INITIALIZE COLLECTION STATISTICS -------------
        this.collectionSize = documentIndexHandler.collectionSize();
        this.avgDocLen = documentIndexHandler.readAvgDocLen();
        //-----------------------------------------------------------

        //-------------INITIALIZE TERM STATISTICS---------------------
        for (int i = 0; i < numTermQuery; i++) {
                ByteBuffer entryBuffer = this.lexiconHandler.findTermEntry(queryTerms[i]);
                docFreqs[i] = this.lexiconHandler.getDf(entryBuffer);
                collectionFreqs[i] = this.lexiconHandler.getCf(entryBuffer);
                offsets[i] = this.lexiconHandler.getOffset(entryBuffer);
        }
        initializePostingListBlocks();

    }
    private void initializePostingListBlocks() throws IOException {
        this.postingListBlocks = new ArrayList<>(numTermQuery);
        for(int i=0; i<numTermQuery; i++){
            if(POSTING_LIST_BLOCK_LENGTH > docFreqs[i]){ //if posting list length is less than the block size
                postingListBlocks.add(i,this.invertedIndexHandler.getPostingList(offsets[i],docFreqs[i]));
            }
            else{                                     //else posting list length is greather than block size
                postingListBlocks.add(i,this.invertedIndexHandler.getPostingList(offsets[i],POSTING_LIST_BLOCK_LENGTH));
            }
            numBlockRead[i]++;
        }
    }

    //------------------------------------------------------------------------//

    private int getMinDocId() {
        int minDocId = this.collectionSize;  //valore che indica che le posting list sono state raggiunte

        //find the current min doc id in the posting lists of the query terms
        for (int i = 0; i < numTermQuery; i++){
            if (endOfPostingListFlag[i]) continue;
            int currentDocId = postingListBlocks.get(i).getCurrentDocId();
            if(currentDocId<minDocId){
                minDocId = currentDocId;
            }
        }

        return minDocId;
    }
    private int docPartialScore(int currentTf) {
        //TODO
        return currentTf;
    }
    private void updatePostingListBlocks() throws IOException {
        for(int i=0; i<numTermQuery; i++){
            if(endOfPostingListBlockFlag[i]){ //if one block is completely processed then load the subsequent if exists
                // read the subsequent block
                int elementToRead = docFreqs[i] - POSTING_LIST_BLOCK_LENGTH*numBlockRead[i];

                //check if exist a subsequent block using the docFreqs which is equal to the length of the posting list
                if (elementToRead > 0) {
                    if(elementToRead > POSTING_LIST_BLOCK_LENGTH ) {
                        elementToRead = POSTING_LIST_BLOCK_LENGTH;
                    }
                    postingListBlocks.add(
                            this.invertedIndexHandler.getPostingList(offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]),
                                    elementToRead));
                }
                else{
                    endOfPostingListFlag[i]=true;
                }
            }
        }
    }

    //------------------------------------------------------------------------//

    public ArrayList<Integer> DAAT() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        float docScore;
        int minDocId;

        int count = 0;//DEBUG
        while ((minDocId = getMinDocId()) != this.collectionSize) {
            docScore = 0;
            System.out.println("minDocId: " + minDocId);
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            for (int i =0; i<numTermQuery;i++) {
                PostingListBlock postingListBlock = postingListBlocks.get(i);
                if (postingListBlock.getCurrentDocId() == minDocId) {

                    currentTf = postingListBlock.getCurrentTf();
                    docScore += docPartialScore(currentTf);

                    //increment the position in the posting list
                    if(postingListBlock.next() == -1){         //increment position and if end of block reached then set the flag
                        endOfPostingListBlockFlag[i] = true;
                    }
                }
            }
            heapScores.insertIntoPriorityQueue(docScore, minDocId);
            updatePostingListBlocks();

            System.out.println("Print postingListBlocks");

            if(count == 0) { //DEBUG
                for(PostingListBlock plb: postingListBlocks){
                    System.out.println(plb);
                }
            }
            count++; //DEBUG
        }
            
        return heapScores.getTopDocIdReversed();
    }
}
