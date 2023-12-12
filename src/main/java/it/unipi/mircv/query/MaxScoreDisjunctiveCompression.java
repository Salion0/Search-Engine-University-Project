package it.unipi.mircv.query;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.file.SkipDescriptorFileHandler;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.SkipDescriptor;
import it.unipi.mircv.index.SkipDescriptorCompression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Parameters.docsLen;
import static it.unipi.mircv.Parameters.scoreType;

public class MaxScoreDisjunctiveCompression {
    private int numTermQuery;
    private final int[] docFreqs;
    private final long[] offsetsDocId;
    private final long[] offsetsTermFreq;
    private final int[] numBlockRead;
    private final int[] numPostingPerBlock;
    private final float[] upperBoundScores;
    private final boolean[] endOfPostingListFlag;
    private final PostingListBlock[] postingListBlocks;
    private final SkipDescriptorCompression[] skipDescriptorsCompression;
    private final InvertedIndexFileHandler invertedIndexFileHandler;

    public MaxScoreDisjunctiveCompression(String[] queryTerms) throws IOException {
        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        invertedIndexFileHandler = new InvertedIndexFileHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs = new int[numTermQuery];
        offsetsDocId = new long[numTermQuery];
        offsetsTermFreq = new long[numTermQuery];
        numBlockRead = new int[numTermQuery];
        endOfPostingListFlag = new boolean[postingListBlocks.length];
        upperBoundScores = new float[numTermQuery];
        skipDescriptorsCompression = new SkipDescriptorCompression[numTermQuery];
        numPostingPerBlock = new int[numTermQuery];

        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconFileHandler.findTermEntryCompression(queryTerms[i]);
            docFreqs[i] = lexiconFileHandler.getDfCompression(entryBuffer);
            offsetsDocId[i] = lexiconFileHandler.getOffsetDocIdCompression(entryBuffer);
            offsetsTermFreq[i] = lexiconFileHandler.getOffsetTermFreqCompression(entryBuffer);
            switch (scoreType){
                case BM25 ->
                        upperBoundScores[i] = lexiconFileHandler.getTermUpperBoundScoreBM25Compression(entryBuffer);
                case FTIDF ->
                        upperBoundScores[i] = lexiconFileHandler.getTermUpperBoundScoreTFIDFCompression(entryBuffer);
            }
            if(docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
                skipDescriptorsCompression[i] = skipDescriptorFileHandler.readSkipDescriptorCompression(
                        lexiconFileHandler.getOffsetSkipDescCompression(entryBuffer), (int) Math.ceil((float) docFreqs[i] / (int) Math.sqrt(docFreqs[i])));
                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        (int) Math.sqrt(docFreqs[i]),
                        offsetsDocId[i], skipDescriptorsCompression[i].getNumByteMaxDocIds().get(0),
                        offsetsTermFreq[i], skipDescriptorsCompression[i].getNumByteTermFreqs().get(0));
            }
            else{
                skipDescriptorsCompression[i] = null;

                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        docFreqs[i],
                        offsetsDocId[i], lexiconFileHandler.getNumByteDocId(entryBuffer),
                        offsetsTermFreq[i], lexiconFileHandler.getNumByteTermFreq(entryBuffer));
            }

            numBlockRead[i] = 1;
        }

        sortArraysByArray(upperBoundScores, docFreqs, offsetsDocId, offsetsTermFreq, skipDescriptorsCompression, postingListBlocks);

        for(int i = 0; i < numTermQuery; i++){
            numPostingPerBlock[i] = (int) Math.sqrt(docFreqs[i]);
        }
    }

    protected void loadPostingListBlockCompression(int indexTerm, int numPosting, long offsetMaxDocId, long offsetTermFreq,
                                                   int numByteDocId, int numByteTermFreq) throws IOException {

        postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingListCompressed(
                numPosting, offsetMaxDocId, numByteDocId, offsetTermFreq, numByteTermFreq);
    }

    // ************************  -- MAX SCORE --   ****************************************
    public ArrayList<Integer> computeMaxScore() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        heapScores.setTopDocCount(MAX_NUM_DOC_RETRIEVED); // initialize the priority queue with 20 elements set to 0
        float[] documentUpperBounds = new float[postingListBlocks.length]; // ub
        float minScoreInHeap = 0; // teta
        int pivot = 0;
        int minCurrentDocId; // current

        documentUpperBounds[0] = upperBoundScores[0];
        for (int i = 1; i < postingListBlocks.length; i++)
            documentUpperBounds[i] = documentUpperBounds[i - 1] + upperBoundScores[i];

        float score;
        int next;
        int minDocIdDocumentLength; // optimization to avoid reading document length more than one time
        int countFinishedPostingLists = 0;
        minCurrentDocId = getMinCurrentDocId(); // get the mi docId between the current element of all posting lists
        long[] returnNextGEQ;
        int elemToRead;


        while (pivot < postingListBlocks.length && minCurrentDocId != Integer.MAX_VALUE) // DEBUG
        {
            score = 0;
            //AIUDOOO
            //minDocIdDocumentLength = documentIndexHandler.readDocumentLength(minCurrentDocId);
            minDocIdDocumentLength = docsLen[minCurrentDocId];
            next = Integer.MAX_VALUE;

            // ESSENTIAL LISTS
            for (int i = pivot; i < postingListBlocks.length; i++)
            {
                if (endOfPostingListFlag[i])
                    continue;

                if (postingListBlocks[i].getCurrentDocId() == minCurrentDocId)
                {
                    switch (scoreType){
                        case BM25 ->
                                score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(), minDocIdDocumentLength, docFreqs[i]);
                        case FTIDF ->
                                score += ScoreFunction.computeTFIDF(postingListBlocks[i].getCurrentTf(), docFreqs[i]);
                    }
                    //prima
                    //score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(), minDocIdDocumentLength, docFreqs[i]);
                    if (postingListBlocks[i].next() == - 1)
                    {
                        if (skipDescriptorsCompression[i] == null){
                            endOfPostingListFlag[i] = true;
                        }else{
                            //numBlockProcessed is equal to the index that we have to use to get the next block

                            if (numBlockRead[i] < skipDescriptorsCompression[i].size()-1){//there is another block to load
                                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                                        numPostingPerBlock[i],
                                        skipDescriptorsCompression[i].getOffsetMaxDocIds().get(numBlockRead[i]),
                                        skipDescriptorsCompression[i].getNumByteMaxDocIds().get(numBlockRead[i]),
                                        skipDescriptorsCompression[i].getOffsetTermFreqs().get(numBlockRead[i]),
                                        skipDescriptorsCompression[i].getNumByteTermFreqs().get(numBlockRead[i])
                                );
                                numBlockRead[i]++;
                            }

                            else if(numBlockRead[i] == skipDescriptorsCompression[i].size()-1 && docFreqs[i] % numPostingPerBlock[i] != 0){
                                //there is another INCOMPLETE block to load
                                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                                        docFreqs[i] % numPostingPerBlock[i],
                                        skipDescriptorsCompression[i].getOffsetMaxDocIds().get(numBlockRead[i]),
                                        skipDescriptorsCompression[i].getNumByteMaxDocIds().get(numBlockRead[i]),
                                        skipDescriptorsCompression[i].getOffsetTermFreqs().get(numBlockRead[i]),
                                        skipDescriptorsCompression[i].getNumByteTermFreqs().get(numBlockRead[i])
                                );
                                numBlockRead[i]++;
                            }else{
                                //there are no more blocks to load
                                endOfPostingListFlag[i] = true;
                            }
                        }
                    }
                }
                if (postingListBlocks[i].getCurrentDocId() < next)
                    next = postingListBlocks[i].getCurrentDocId();
            }

            // NON-ESSENTIAL LISTS
            for (int i = pivot - 1; i >= 0; i--)
            {
                if (score + documentUpperBounds[i] <= minScoreInHeap)
                    break;

                if(skipDescriptorsCompression[i] != null){
                    returnNextGEQ = skipDescriptorsCompression[i].nextGEQ(minCurrentDocId); // get the nextGEQ of the current posting list
                    if(returnNextGEQ[0] == -1)
                    {
                        return heapScores.getTopDocIdReversed();
                    }
                    else
                    {   //int postingListSkipBlockSize = (int) Math.sqrt(docFreqs[i]); //compute the skip size (square root of the posting list length)
                        //controllo il range del currentDocId per vedere se siamo nello stesso blocco di prima, se sì non ha senso ricaricarlo
                        if (!(postingListBlocks[i].getMaxDocID() > minCurrentDocId
                                && postingListBlocks[i].getMinDocID() < minCurrentDocId)){

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

                if (currentDocIdInPostingList(i, minCurrentDocId)){ //seek currentDocId in the posting list
                    switch (scoreType){
                        case BM25 ->
                                score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(), minDocIdDocumentLength, docFreqs[i]);
                        case FTIDF ->
                                score += ScoreFunction.computeTFIDF(postingListBlocks[i].getCurrentTf(), docFreqs[i]);
                    }
                    //prima dello switch
                    //score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(), minDocIdDocumentLength, docFreqs[i]);
                }
            }

            // LIST PIVOT UPDATE
            heapScores.insertIntoPriorityQueueMAXSCORE(score, minCurrentDocId);
            minScoreInHeap = heapScores.getMinScore();
            while(pivot < postingListBlocks.length && documentUpperBounds[pivot] <= minScoreInHeap)
                pivot++;

            minCurrentDocId = next;
        }

        return heapScores.getTopDocIdReversed();
    }

    private boolean currentDocIdInPostingList(int indexTerm, int currentDocId) {
        do {
            if (postingListBlocks[indexTerm].getCurrentDocId() == currentDocId) return true;
            if (postingListBlocks[indexTerm].getCurrentDocId() > currentDocId) return false;
        } while (postingListBlocks[indexTerm].next() != -1);
        return false;
    }

    public int getMinCurrentDocId() {
        int minCurrentDocId = Integer.MAX_VALUE;
        for (int i = 0; i < postingListBlocks.length; i++)
            if (postingListBlocks[i].getCurrentDocId() < minCurrentDocId)
                minCurrentDocId = postingListBlocks[i].getCurrentDocId();

        return minCurrentDocId;
    }


    public static void sortArraysByArray(float[] arrayToSort, int[] otherArray, long[] offsetsDocId, long[] offsetsTermFreq,
                                         SkipDescriptorCompression[] otherOtherOtherArray, PostingListBlock[] otherOtherOtherOtherArray) {

        Integer[] indexes = new Integer[arrayToSort.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        Arrays.sort(indexes, Comparator.comparingDouble(i -> arrayToSort[i]));
        for (int i = 0; i < arrayToSort.length; i++) {
            if (indexes[i] != i) {
                float temp = arrayToSort[i];
                arrayToSort[i] = arrayToSort[indexes[i]];
                arrayToSort[indexes[i]] = temp;

                int temp1 = otherArray[i];
                otherArray[i] = otherArray[indexes[i]];
                otherArray[indexes[i]] = temp1;

                long offset = offsetsDocId[i];
                offsetsDocId[i] = offsetsDocId[indexes[i]];
                offsetsDocId[indexes[i]] = offset;

                offset = offsetsTermFreq[i];
                offsetsTermFreq[i] = offsetsTermFreq[indexes[i]];
                offsetsTermFreq[indexes[i]] = offset;

                SkipDescriptorCompression tempSkipDescriptor = otherOtherOtherArray[i];
                otherOtherOtherArray[i] = otherOtherOtherArray[indexes[i]];
                otherOtherOtherArray[indexes[i]] = tempSkipDescriptor;

                PostingListBlock postingListBlock = otherOtherOtherOtherArray[i];
                otherOtherOtherOtherArray[i] = otherOtherOtherOtherArray[indexes[i]];
                otherOtherOtherOtherArray[indexes[i]] = postingListBlock;

                indexes[indexes[i]] = indexes[i];
            }
        }
    }
}
