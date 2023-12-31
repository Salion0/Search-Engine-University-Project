package it.unipi.mircv.query;

import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.file.SkipDescriptorFileHandler;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.SkipDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static it.unipi.mircv.utility.Parameters.docsLen;
import static it.unipi.mircv.utility.Config.*;
import static it.unipi.mircv.utility.Parameters.scoreType;

public class MaxScoreDisjunctive {
    /*
    * This class implements the MaxScoreDisjunctive algorithm.
    * This algorithm help to improve the efficiency of the disjunctive
    * query processing using dynamic pruning
    */
    private final int numTermQuery;
    private final int[] docFreqs;
    private final int[] offsets;
    private final int[] numElementsRead; // count the numbers of element read for each posting list
    private final float[] upperBoundScores;
    private final boolean[] endOfPostingListFlag;
    private final PostingListBlock[] postingListBlocks;
    private final SkipDescriptor[] skipDescriptors;
    private final InvertedIndexFileHandler invertedIndexHandler;
    private MinHeapScores heapScores;
    private boolean invalidConstruction = false;

    public MaxScoreDisjunctive(String[] queryTerms) throws IOException {
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        invertedIndexHandler = new InvertedIndexFileHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs = new int[numTermQuery];
        offsets = new int[numTermQuery];
        numElementsRead = new int[numTermQuery]; // count the numbers of element read for each posting list
        endOfPostingListFlag = new boolean[numTermQuery];
        upperBoundScores = new float[numTermQuery];
        skipDescriptors = new SkipDescriptor[numTermQuery];

        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            if(entryBuffer == null){ //if the ith term is not present in lexicon
                System.out.println(queryTerms[i] + " is not inside the index");
                invalidConstruction = true;
                break;
            }
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
            switch (scoreType){
                case BM25 ->
                        upperBoundScores[i] = lexiconHandler.getTermUpperBoundScoreBM25(entryBuffer);
                case TFIDF ->
                        upperBoundScores[i] = lexiconHandler.getTermUpperBoundScoreTFIDF(entryBuffer);
            }
            if (docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP))
            {
                skipDescriptors[i] = skipDescriptorFileHandler.readSkipDescriptor(
                        lexiconHandler.getOffsetSkipDesc(entryBuffer), (int) Math.ceil(Math.sqrt(docFreqs[i])));
                if (POSTING_LIST_BLOCK_LENGTH > docFreqs[i])
                    postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]);
                else
                    postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], POSTING_LIST_BLOCK_LENGTH);
            }
            else
            {
                skipDescriptors[i] = null;
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]);
            }
        }
        sortArraysByArray(upperBoundScores, docFreqs, offsets, skipDescriptors, postingListBlocks);
        lexiconHandler.close();
        skipDescriptorFileHandler.closeFileChannel();
    }

    private void uploadPostingListBlock(int indexTerm, int readElement, int blockSize) throws IOException {

        if ((docFreqs[indexTerm] - readElement) < blockSize) {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
            //numElementsRead[indexTerm] += (docFreqs[indexTerm] - readElement);
        }
        else {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize);
            //numElementsRead[indexTerm] += blockSize;
        }
    }

    // ************************  -- MAX SCORE --   ****************************************
    public ArrayList<Integer> computeMaxScore() throws IOException {
        if(invalidConstruction){
            invertedIndexHandler.close();
            return new ArrayList<>(0);
        }
        heapScores = new MinHeapScores();
        heapScores.setTopDocCount(MAX_NUM_DOC_RETRIEVED); // initialize the priority queue with 20 elements set to 0
        float minScoreInHeap = 0; // teta
        int pivot = 0;
        int minCurrentDocId; // current

        float[] documentUpperBounds = new float[postingListBlocks.length]; // ub
        documentUpperBounds[0] = upperBoundScores[0];
        for (int i = 1; i < postingListBlocks.length; i++)
            documentUpperBounds[i] = documentUpperBounds[i - 1] + upperBoundScores[i];

        float score;
        int next;
        int minDocIdDocumentLength; // optimization to avoid reading document length more than one time
        int countFinishedPostingLists = 0;
        minCurrentDocId = getMinCurrentDocId(); // get the min docId between the current element of all posting lists

        while (pivot < postingListBlocks.length && minCurrentDocId != Integer.MAX_VALUE) // DEBUG
        {
            score = 0;
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
                        case TFIDF ->
                                score += ScoreFunction.TFIDF(postingListBlocks[i].getCurrentTf(), docFreqs[i]);
                    }
                    numElementsRead[i]++;
                    if (postingListBlocks[i].next() == - 1)
                    {
                        if (numElementsRead[i] >= docFreqs[i]) // check if the entire posting list is finished
                            endOfPostingListFlag[i] = true;
                        else
                            uploadPostingListBlock(i, numElementsRead[i], POSTING_LIST_BLOCK_LENGTH); // load another block
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

                if (skipDescriptors[i] != null) {
                    int offsetNextGEQ = skipDescriptors[i].nextGEQ(minCurrentDocId); // get the nextGEQ of the current posting list
                    if (offsetNextGEQ != -1)
                    {
                        if (!(postingListBlocks[i].getMaxDocID() > minCurrentDocId
                                && postingListBlocks[i].getMinDocID() < minCurrentDocId))
                        {
                            uploadPostingListBlock(i, (offsetNextGEQ - offsets[i]), (int) Math.sqrt(docFreqs[i]));
                            numElementsRead[i] = offsetNextGEQ - offsets[i];
                        }
                    }
                }

                if (currentDocIdInPostingList(i, minCurrentDocId)){ //seek currentDocId in the posting list
                    switch (scoreType){
                        case BM25 ->
                                score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(), minDocIdDocumentLength, docFreqs[i]);
                        case TFIDF ->
                                score += ScoreFunction.TFIDF(postingListBlocks[i].getCurrentTf(), docFreqs[i]);
                    }
                }
            }

            // LIST PIVOT UPDATE
            heapScores.insertIntoPriorityQueueMAXSCORE(score, minCurrentDocId);
            minScoreInHeap = heapScores.getMinScore();
            while(pivot < postingListBlocks.length && documentUpperBounds[pivot] <= minScoreInHeap)
                pivot++;

            minCurrentDocId = next;
        }
        invertedIndexHandler.close();
        return heapScores.getTopDocIdReversed();
    }

    private boolean currentDocIdInPostingList(int indexTerm, int currentDocId) {
        do {
            if (postingListBlocks[indexTerm].getCurrentDocId() == currentDocId) return true;
            if (postingListBlocks[indexTerm].getCurrentDocId() > currentDocId) return false;
            numElementsRead[indexTerm]++;
        } while (postingListBlocks[indexTerm].next() != -1);
        return false;
    }

    public int getMinCurrentDocId() {
        int minCurrentDocId = Integer.MAX_VALUE;
        for (int i = 0; i < numTermQuery; i++)
            if (postingListBlocks[i].getCurrentDocId() < minCurrentDocId)
                minCurrentDocId = postingListBlocks[i].getCurrentDocId();
        return minCurrentDocId;
    }


    public static void sortArraysByArray(float[] arrayToSort, int[] otherArray, int[] otherOtherArray,
                                         SkipDescriptor[] otherOtherOtherArray, PostingListBlock[] otherOtherOtherOtherArray) {

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

                temp1 = otherOtherArray[i];
                otherOtherArray[i] = otherOtherArray[indexes[i]];
                otherOtherArray[indexes[i]] = temp1;

                SkipDescriptor tempSkipDescriptor = otherOtherOtherArray[i];
                otherOtherOtherArray[i] = otherOtherOtherArray[indexes[i]];
                otherOtherOtherArray[indexes[i]] = tempSkipDescriptor;

                PostingListBlock postingListBlock = otherOtherOtherOtherArray[i];
                otherOtherOtherOtherArray[i] = otherOtherOtherOtherArray[indexes[i]];
                otherOtherOtherOtherArray[indexes[i]] = postingListBlock;

                indexes[indexes[i]] = indexes[i];
            }
        }
    }

    public MinHeapScores getHeapScores() {
        return heapScores;
    }
}
