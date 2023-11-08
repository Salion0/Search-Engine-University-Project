package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.*;

import javax.swing.text.Document;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import static it.unipi.mircv.Index.Config.*;

public class QueryProcessor {

    //------------------------------------------------------------------------//
    private int collectionSize;
    
    private int numTermQuery;

    //------------------------------------------------------------------------  Priority Queue of Doc Retrieved//
    private final HashMap<Float, ArrayList<Integer>> score2DocIdMap = new HashMap<>();  //list of docIDs retrieved sorted by ranking

    private final PriorityQueue<Float> topScores = new PriorityQueue<>();


    //------------------------------------------------------------------------//
    private int[] query_tf;  //list of term frequencies in the queries
    private boolean[] endOfPostingListBlockFlag;

    private int [] numBlockRead;  //counter for keeping info on how many blocks for each posting list
    private boolean[] endOfPostingList;
    private int numBlockChecked; //number of block completely checked
    private String[] queryTerms;//query terms
    private int[] docFreqs; //doc frequencies of query terms
    private int[] collectionFreqs; // collection frequencies of query terms
    private int[] offsets; // offsets of the posting list of query terms
    private ArrayList<PostingListBlock> postingListBlocks;

    //------------------------------------------------------------------------//
    public QueryProcessor(String query) throws IOException {

        //---------------INITIALIZE ARRAYS---------------------------
        this.queryTerms = query.split("\\s");
        this.numTermQuery = queryTerms.length;
        this.query_tf = new int[numTermQuery];
        this.numBlockRead = new int[numTermQuery];

        //TODO prendere la collection size da solo
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();


        this.docFreqs =  new int[numTermQuery];
        this.collectionFreqs = new int[numTermQuery];
        this.offsets = new int[numTermQuery];

        LexiconHandler lexiconHandler = new LexiconHandler();
        for (int i = 0; i < numTermQuery; i++) {
                ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
                docFreqs[i] = lexiconHandler.getDf(entryBuffer);
                collectionFreqs[i] = lexiconHandler.getCf(entryBuffer);
                offsets[i] = lexiconHandler.getOffset(entryBuffer);
        }
        initializePostingListBlocks();
        
    }

    private void initializePostingListBlocks() throws IOException {
        InvertedIndexHandler invertedIndexFiles = new InvertedIndexHandler();
        for(int i=0;i<numTermQuery; i++){
            if(POSTING_LIST_BLOCK_LENGTH>docFreqs[i]){ //if posting list length is less than the block size
                postingListBlocks.add((PostingListBlock) invertedIndexFiles.getPostingList(offsets[i],docFreqs[i]));
            }
            else{                                     //else posting list length is greather than block size
                postingListBlocks.add((PostingListBlock) invertedIndexFiles.getPostingList(offsets[i],POSTING_LIST_BLOCK_LENGTH));
            }
            numBlockRead[i]++;
        }
    }

    private int getMinDocId() {
        int minDocId = this.collectionSize;  //valore che indica che le posting list sono state raggiunte
        if(this.numBlockChecked == numTermQuery){
            numBlockChecked=0;
            return minDocId;
        }

        //find the current min doc id in the posting lists of the query terms
        for (PostingListBlock plb : postingListBlocks) {
            int currentDocId = plb.getCurrentDocId();
               if(currentDocId<minDocId){
                   minDocId = currentDocId;
               }
        }

        int count=0;

        return minDocId;
    }

    private int docPartialScore(int currentTf) {
        //TODO
        return currentTf;
    }
    private void insertDocIdInMap(float score,int docId){
        if (score2DocIdMap.containsKey(score)) {  //if score is present in hashmap
            score2DocIdMap.get(score).add(docId); //add element to the arrayList of docId
        }else{
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(docId);
            score2DocIdMap.put(score,arrayList);
        }
    }
    private void removeDocIdFromMap(float score){
        ArrayList<Integer> docIds = score2DocIdMap.get(score);
        if(docIds.size()>1){ //if there are more than 1 docIDs associated to the score then remove only one
            docIds.removeLast();
        }
        else{ //if there is only one element then remove the tuple from the hashmap
            score2DocIdMap.remove(score);
        }
    }
    private boolean allTrue(boolean[] array){
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private void updatePostingListBlocks(){
        for(int i=0;i<numTermQuery;i++){
            if(endOfPostingListBlockFlag[i]){ //if one block is completely processed then load the subsequent if exists

                //check if exist a subsequent block using the docFreqs which is equal to the length of the posting list
                if(docFreqs[i]>POSTING_LIST_BLOCK_LENGTH*numBlockRead[i]) {
                    //read the subsequent block

                }
            }
        }
    }
    public int[] DAAT() throws IOException {
        //Process the query using Document At A Time
        //TODO

        while(!allTrue(endOfPostingList)){

                updatePostingListBlocks();
            int topDocCount=0; //counter for keep track of how many document have been inserted in the min-heap
            int minDocId;
            float docScore = 0;
            while ((minDocId = getMinDocId()) != this.collectionSize) {

                int currentTf = 0;

                for (int i =0; i<numTermQuery;i++) {
                    PostingListBlock plb = postingListBlocks.get(i);
                    if (plb.getCurrentDocId() == minDocId) {

                        //compute the score
                        currentTf = plb.getCurrentTf();
                        docScore += docPartialScore(currentTf);

                        //increment the position in the posting list
                        if(plb.next()==-1){         //increment position and if end of block reached then set the flag
                            endOfPostingListBlockFlag[i] = true;
                            this.numBlockChecked ++;
                        }
                    }
                }



                if (topDocCount<MAX_NUM_DOC_RETRIEVED){  //There less than k documents in the priority queue
                    topDocCount++;
                    try {
                        topScores.add(docScore);
                        insertDocIdInMap(docScore,minDocId);

                    }catch(Exception e){
                        System.out.println("Errore Non posso inserire doc:"+minDocId);
                        e.printStackTrace();
                    }

                }else{      //there are more than k documents in the priority queue

                    if(docScore > topScores.peek()) { //need to check if minDocId should be inserted
                        topScores.remove(topScores.peek());
                        topScores.add(docScore);
                        removeDocIdFromMap(topScores.peek());
                        insertDocIdInMap(docScore,minDocId);
                    }

                }



                //sortDocIDRetrieved();
            }
        }

        return null;
    }

    public int[] TAAT(){
        //Process the query using Document At A Time
        //TODO


        return null;
    }

    public String[] getQuery() {
        return queryTerms;
    }

    public void setCollectionSize(int collectionSize) {
        this.collectionSize = collectionSize;
    }
}
