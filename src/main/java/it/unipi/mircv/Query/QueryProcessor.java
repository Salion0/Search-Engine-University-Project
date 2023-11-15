package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;


import static it.unipi.mircv.Config.*;

public class QueryProcessor {

    //------------------FILE HANDLER-------------------------------------------//
    private InvertedIndexHandler invertedIndexHandler;
    private LexiconHandler lexiconHandler;
    private DocumentIndexHandler documentIndexHandler;

    //-------------------STATS----------------------------------------------//

    private String[] queryTerms;//query terms
    private int collectionSize;
    private float avgDocLen;
    private int numTermQuery;
    private int[] docFreqs; //doc frequencies of query terms
    private int[] collectionFreqs; // collection frequencies of query terms
    private int[] offsets; // offsets of the posting list of query terms

    //------------------------------------------------------------------------//
    private boolean[] endOfPostingListBlockFlag;
    private int[] numBlockRead;  //counter for keeping info on how many blocks for each posting list
    private boolean[] endOfPostingListFlag;
    private ArrayList<PostingListBlock> postingListBlocks;

    private int minPostingList;

    //-------------------CONSTRUCTOR--------------------------------//

    public QueryProcessor(String query) throws IOException {

        //---------------INITIALIZE ARRAYS---------------------------
        this.queryTerms = query.split(" ");
        this.numTermQuery = queryTerms.length;
        this.numBlockRead = new int[numTermQuery];
        this.docFreqs = new int[numTermQuery];
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

        //------------SORT POSTING LIST BY DOC FREQ (SIZE)-------------

        for (int i = 0; i < this.numTermQuery; i++) {
            for (int k = i + 1; k < this.numTermQuery; k++) {
                if (docFreqs[k] < docFreqs[i]) {
                    this.minPostingList = k;
                }
            }

            initializePostingListBlocks();
        }
    }
        private void swap ( int a, int b, int[] array){
            int temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }
        private void initializePostingListBlocks () throws IOException {
            this.postingListBlocks = new ArrayList<>(numTermQuery);
            for (int i = 0; i < numTermQuery; i++) {
                if (POSTING_LIST_BLOCK_LENGTH > docFreqs[i]) { //if posting list length is less than the block size
                    postingListBlocks.add(i, this.invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]));
                } else {                                     //else posting list length is greather than block size
                    postingListBlocks.add(i, this.invertedIndexHandler.getPostingList(offsets[i], POSTING_LIST_BLOCK_LENGTH));
                }
                numBlockRead[i]++;
            }
        }

        //--------------------DAAT FUNCTION--------------------------------//

        private int getMinDocId () {
            int minDocId = this.collectionSize;  //valore che indica che le posting list sono state raggiunte

            //find the current min doc id in the posting lists of the query terms
            for (int i = 0; i < numTermQuery; i++) {
                if (endOfPostingListFlag[i]) continue;
                int currentDocId = postingListBlocks.get(i).getCurrentDocId();
                if (currentDocId < minDocId) {
                    minDocId = currentDocId;
                }
            }

            return minDocId;
        }
        private void updatePostingListBlocks () throws IOException {
            for (int i = 0; i < numTermQuery; i++) {
                if (endOfPostingListBlockFlag[i]) { //if one block is completely processed then load the subsequent if exists
                    // read the subsequent block
                    int elementToRead = docFreqs[i] - POSTING_LIST_BLOCK_LENGTH * numBlockRead[i];
                    //System.out.println(elementToRead); //DEBUG

                    //check if exist a subsequent block using the docFreqs which is equal to the length of the posting list
                    if (elementToRead > 0) {
                        if (elementToRead > POSTING_LIST_BLOCK_LENGTH) {
                            elementToRead = POSTING_LIST_BLOCK_LENGTH;
                        }
                        postingListBlocks.set(i, // ho messo i perché sennò facevamo sempre append e non replace
                                this.invertedIndexHandler.getPostingList(offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]),
                                        elementToRead));
                        //System.out.println(this.invertedIndexHandler.getPostingList(offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]), elementToRead)); //DEBUG
                        endOfPostingListBlockFlag[i] = false; // resetto il campo perché ho caricato un altro blocco
                        numBlockRead[i]++; // ho letto un altro blocco quindi aumento il campo
                    } else {
                        endOfPostingListFlag[i] = true;
                    }
                }
            }
        }

        //-------------------SCORING FUNCTION-------------------------------------//

        public float computeBM25 ( int termFrequency, int documentLength, int documentFrequency){
            return (float) ((termFrequency / (termFrequency + 1.5 * ((1 - 0.75) + 0.75 * (documentLength / avgDocLen))))
                    * (float) Math.log10(collectionSize / documentFrequency));

        }
        public float computeIDF ( int documentFrequency){
            return (float) Math.log10(documentFrequency / collectionSize);
        }
        public float computeTFIDF ( int termFrequency, int documentFrequency){
            return (float) ((1 + Math.log10(termFrequency)) * Math.log10(documentFrequency / collectionSize));
        }

        //------------------------------------------------------------------------//
        public ArrayList<Integer> DAAT () throws IOException {
            DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
            MinHeapScores heapScores = new MinHeapScores();
            float docScore;
            int minDocId;

            int count = 0;//DEBUG
            while ((minDocId = getMinDocId()) != this.collectionSize) {
                docScore = 0;
                System.out.println("minDocId: " + minDocId);
                //-----------------------COMPUTE THE SCORE-------------------------------------------------------
                int currentTf;
                int documentLength = documentIndexHandler.readDocumentLength(minDocId);
                for (int i = 0; i < numTermQuery; i++) {
                    PostingListBlock postingListBlock = postingListBlocks.get(i);
                    if (postingListBlock.getCurrentDocId() == minDocId) {

                        currentTf = postingListBlock.getCurrentTf();
                        //docScore += docPartialScore(currentTf); //questa era la versione originale dove usavamo le frequency per lo score
                        docScore += computeBM25(currentTf, documentLength, docFreqs[i]);

                        //increment the position in the posting list
                        if (postingListBlock.next() == -1) {         //increment position and if end of block reached then set the flag
                            endOfPostingListBlockFlag[i] = true;
                        }
                    }
                }
                heapScores.insertIntoPriorityQueue(docScore, minDocId);
                updatePostingListBlocks();


                if (count == 0) { //DEBUG
                    for (PostingListBlock plb : postingListBlocks) {
                        System.out.print("Print postingListBlocks");
                        System.out.println(plb);
                    }
                }
                count++; //DEBUG
            }

            return heapScores.getTopDocIdReversed();
        }
        public ArrayList<Integer> conjunctiveDAAT () throws IOException {
            DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
            MinHeapScores heapScores = new MinHeapScores();
        /*
        float docScore;
        int maxDocId;
        boolean maxDocIdInAllPostingLists = true;

        while ((maxDocId = getMaxDocId()) != this.collectionSize) {
            docScore = 0;
            System.out.println("maxDocId: " + maxDocId);
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            int documentLength = documentIndexHandler.readDocumentLength(maxDocId);

            for (int i =0; i<numTermQuery;i++)
            {
                PostingListBlock postingListBlock = postingListBlocks.get(i);
                if (postingListBlock.getCurrentDocId() != maxDocId)
                    maxDocIdInAllPostingLists = false;
                    if (postingListBlock.next() == -1)         //increment position and if end of block reached then set the flag
                        endOfPostingListBlockFlag[i] = true;
                else
                {
                    if (maxDocIdInAllPostingLists == true)
                    {
                        currentTf = postingListBlock.getCurrentTf();
                        docScore += computeBM25(currentTf, documentLength, docFreqs[i]);
                    }
                    //increment the position in the posting list

                }
            }
            if (maxDocIdInAllPostingLists == true)
                heapScores.insertIntoPriorityQueue(docScore, maxDocId);
            updatePostingListBlocks();
            maxDocIdInAllPostingLists = true;
        }
*/
            return heapScores.getTopDocIdReversed();
        }

    }

