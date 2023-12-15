package it.unipi.mircv.query;

import it.unipi.mircv.file.*;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.SkipDescriptorCompression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Parameters.docsLen;
import static it.unipi.mircv.Parameters.scoreType;

public class ConjunctiveDAATCompression {
    protected final int numTermQuery;
    protected final int[] docFreqs;
    protected final long[] offsetsDocId;
    protected final long[] offsetsTermFreq;
    protected final PostingListBlock[] postingListBlocks;
    protected final SkipDescriptorCompression[] skipDescriptorsCompression;
    protected final int[] numPostingPerBlock;
    protected final DocumentIndexFileHandler documentIndexFileHandler;
    protected final InvertedIndexFileHandler invertedIndexFileHandler;
    protected float currentDocScore;
    protected Integer currentDocLen;

    public ConjunctiveDAATCompression(String[] queryTerms) throws IOException {
        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        documentIndexFileHandler = new DocumentIndexFileHandler();
        invertedIndexFileHandler = new InvertedIndexFileHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();

        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs = new int[numTermQuery];
        offsetsDocId = new long[numTermQuery];
        offsetsTermFreq = new long[numTermQuery];
        numPostingPerBlock = new int[numTermQuery];

        skipDescriptorsCompression = new SkipDescriptorCompression[numTermQuery];

        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconFileHandler.findTermEntryCompression(queryTerms[i]);
            docFreqs[i] = lexiconFileHandler.getDfCompression(entryBuffer);
            offsetsDocId[i] = lexiconFileHandler.getOffsetDocIdCompression(entryBuffer);
            offsetsTermFreq[i] = lexiconFileHandler.getOffsetTermFreqCompression(entryBuffer);

            if(docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
                skipDescriptorsCompression[i] = skipDescriptorFileHandler.readSkipDescriptorCompression(
                        lexiconFileHandler.getOffsetSkipDescCompression(entryBuffer), (int) Math.ceil((float) docFreqs[i] / (int) Math.sqrt(docFreqs[i])));
                postingListBlocks[i] = new PostingListBlock();
                postingListBlocks[i].setDummyFields();
            }
            else{
                skipDescriptorsCompression[i] = null;

                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        docFreqs[i],
                        offsetsDocId[i], lexiconFileHandler.getNumByteDocId(entryBuffer),
                        offsetsTermFreq[i], lexiconFileHandler.getNumByteTermFreq(entryBuffer));
            }
        }
        //TODO anzichè ordinare 300 array basterebbe ordinare gli entryBuffer facendo una prima get della sola docfreq come ho fatto per numPostingPerBlock
        sortArraysByArray(docFreqs, offsetsDocId, offsetsTermFreq,
                skipDescriptorsCompression, postingListBlocks);

        for(int i = 0; i< numTermQuery; i++){
            numPostingPerBlock[i] = (int) Math.sqrt(docFreqs[i]);
        }
    }

    public ArrayList<Integer> processQuery() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        int postingCount = 0;
        int numBlockProcessed = 0;
        int currentDocId;
        long[] returnNextGEQ;
        boolean docIdInAllPostingLists;
        int elemToRead;

        if(skipDescriptorsCompression[0] != null){ //load the first posting list block
            loadPostingListBlockCompression(0,
                    numPostingPerBlock[0],
                    offsetsDocId[0], offsetsTermFreq[0],
                    skipDescriptorsCompression[0].getNumByteMaxDocIds().get(0),
                    skipDescriptorsCompression[0].getNumByteTermFreqs().get(0));
        }
        //else -> it was already loaded

        while(postingCount < docFreqs[0]){
            postingCount++;
            currentDocId = postingListBlocks[0].getCurrentDocId();
            currentDocScore = 0;
            currentDocLen = 0;
            docIdInAllPostingLists = true;

            //calculate the partial score for the other posting list if they contain the currentDocId
            for (int i = 1; i < numTermQuery; i++) {
                //if skipDescriptors[i] is not null load a posting list block by block
                if(skipDescriptorsCompression[i] != null){
                    returnNextGEQ = skipDescriptorsCompression[i].nextGEQ(currentDocId); // get the nextGEQ of the current posting list
                    if(returnNextGEQ[0] == -1)
                    {
                        return heapScores.getTopDocIdReversed();
                    }
                    else
                    {   //int postingListSkipBlockSize = (int) Math.sqrt(docFreqs[i]); //compute the skip size (square root of the posting list length)
                        //controllo il range del currentDocId per vedere se siamo nello stesso blocco di prima, se sì non ha senso ricaricarlo
                        if (!(postingListBlocks[i].getMaxDocID() > currentDocId
                                && postingListBlocks[i].getMinDocID() < currentDocId)){

                            elemToRead = numPostingPerBlock[i];
                            //check if the block is the last one, and if it has less than (sqrt()) elements.
                            // This if statement must be performed after elemToRead = numPostingPerBlock[i];
                            if(returnNextGEQ[4] == 1 && docFreqs[i]%numPostingPerBlock[i] != 0){
                                elemToRead = docFreqs[i]%numPostingPerBlock[i];
                            }
                            loadPostingListBlockCompression(i,
                                    elemToRead,
                                    returnNextGEQ[0],
                                    returnNextGEQ[1],
                                    (int) returnNextGEQ[2],
                                    (int) returnNextGEQ[3]
                            );
                        }
                    }
                }

                if(currentDocIdIsInPostingList(i, currentDocId))
                    updateCurrentDocScore(i);
                else
                {
                    docIdInAllPostingLists = false;
                    break;
                }
            }
            if(docIdInAllPostingLists) {
                updateCurrentDocScore(0);
                heapScores.insertIntoPriorityQueue(currentDocScore, currentDocId);
            }
            if (postingListBlocks[0].next() == -1 && skipDescriptorsCompression[0] != null) {
                numBlockProcessed ++;
                //numBlockProcessed is equal to the index that we have to use to get the next block
                if (numBlockProcessed < skipDescriptorsCompression[0].size()-1){
                    //there is another block to load
                    elemToRead = numPostingPerBlock[0];
                }
                else if(numBlockProcessed == skipDescriptorsCompression[0].size()-1 && docFreqs[0] % numPostingPerBlock[0] != 0){
                    //there is another INCOMPLETE block to load
                    elemToRead = docFreqs[0] % (numPostingPerBlock[0]);
                }else{
                    //there are no more blocks to load
                    continue;
                }
                loadPostingListBlockCompression(0,
                        elemToRead,
                        skipDescriptorsCompression[0].getOffsetMaxDocIds().get(numBlockProcessed),
                        skipDescriptorsCompression[0].getOffsetTermFreqs().get(numBlockProcessed),
                        skipDescriptorsCompression[0].getNumByteMaxDocIds().get(numBlockProcessed),
                        skipDescriptorsCompression[0].getNumByteTermFreqs().get(numBlockProcessed)
                );
            }
        }
        return heapScores.getTopDocIdReversed();
    }

    protected boolean currentDocIdIsInPostingList(int indexTerm, int currentDocId){
        do{
            if(postingListBlocks[indexTerm].getCurrentDocId() == currentDocId) return true;
            if(postingListBlocks[indexTerm].getCurrentDocId() > currentDocId) return false;
        } while (postingListBlocks[indexTerm].next() != -1);
        return false;
    }

    protected void updateCurrentDocScore(int index) throws IOException {
        if (index == 1) {
            //AIUDOO
            //currentDocLen = documentIndexHandler.readDocumentLength(postingListBlocks[index].getCurrentDocId());
            currentDocLen = docsLen[postingListBlocks[index].getCurrentDocId()];
        }
        switch (scoreType){
            case BM25 ->
                    currentDocScore += ScoreFunction.BM25(postingListBlocks[index].getCurrentTf(), currentDocLen, docFreqs[index]);
            case TFIDF ->
                    currentDocScore += ScoreFunction.computeTFIDF(postingListBlocks[index].getCurrentTf(), docFreqs[index]);
        }
    }
    protected void loadPostingListBlockCompression(int indexTerm, int numPosting, long offsetMaxDocId, long offsetTermFreq,
                                                   int numByteDocId, int numByteTermFreq) throws IOException {

        postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingListCompressed(
                numPosting, offsetMaxDocId, numByteDocId, offsetTermFreq, numByteTermFreq);
    }
    protected static void sortArraysByArray(int[] arrayToSort,long[] array1, long[] array2,
                                            SkipDescriptorCompression[] array3, PostingListBlock[] array4) {
        // Sort all the input arrays according to the elements of the first array
        // Initialize an array of indexes to keep track of the original positions of the elements
        Integer[] indexes = new Integer[arrayToSort.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        // Sort the indexes array according to the elements of the array to sort
        Arrays.sort(indexes, Comparator.comparingInt(i -> arrayToSort[i]));
        // Apply the same permutation to the other arrays
        for (int i = 0; i < arrayToSort.length; i++) {
            if (indexes[i] != i) {
                //Change the elements in the array to sort
                int temp = arrayToSort[i];
                arrayToSort[i] = arrayToSort[indexes[i]];
                arrayToSort[indexes[i]] = temp;

                //Change the elements in the othersArrays to sort

                long tempOffsetDocId = array1[i];
                array1[i] = array1[indexes[i]];
                array1[indexes[i]] = tempOffsetDocId;

                long tempOffsetTermFreq = array2[i];
                array2[i] = array2[indexes[i]];
                array2[indexes[i]] = tempOffsetTermFreq;

                SkipDescriptorCompression tempSkipDescriptor = array3[i];
                array3[i] = array3[indexes[i]];
                array3[indexes[i]] = tempSkipDescriptor;

                PostingListBlock postingListBlock = array4[i];
                array4[i] = array4[indexes[i]];
                array4[indexes[i]] = postingListBlock;

                // Update the indexes array
                indexes[indexes[i]] = indexes[i];
            }
        }
    }

}
