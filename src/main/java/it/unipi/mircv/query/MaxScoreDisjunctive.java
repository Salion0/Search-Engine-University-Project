package it.unipi.mircv.query;

import it.unipi.mircv.file.DocumentIndexFileHandler;
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

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Parameters.scoreType;

public class MaxScoreDisjunctive {
    private int numTermQuery;
    private final int[] docFreqs;
    private final int[] offsets;
    private final int[] numElementsRead; // count the numbers of element read for each posting list
    private final float[] upperBoundScores;
    private final boolean[] endOfPostingListFlag;
    private final PostingListBlock[] postingListBlocks;
    private final SkipDescriptor[] skipDescriptors;
    private final DocumentIndexFileHandler documentIndexHandler;
    private final InvertedIndexFileHandler invertedIndexHandler;

    public MaxScoreDisjunctive(String[] queryTerms) throws IOException {
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        documentIndexHandler = new DocumentIndexFileHandler();
        invertedIndexHandler = new InvertedIndexFileHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs = new int[numTermQuery];
        offsets = new int[numTermQuery];
        numElementsRead = new int[postingListBlocks.length]; // count the numbers of element read for each posting list
        endOfPostingListFlag = new boolean[postingListBlocks.length];
        upperBoundScores = new float[numTermQuery];
        skipDescriptors = new SkipDescriptor[numTermQuery];

        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
            upperBoundScores[i] = lexiconHandler.getTermUpperBoundScoreTFIDF(entryBuffer);

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

        while (pivot < postingListBlocks.length && minCurrentDocId != Integer.MAX_VALUE) // DEBUG
        {
            score = 0;
            //AIUDOOO
            minDocIdDocumentLength = documentIndexHandler.readDocumentLength(minCurrentDocId);
            //minDocIdDocumentLength = docsLen[minCurrentDocId];
            next = Integer.MAX_VALUE;

            // ESSENTIAL LISTS
            for (int i = pivot; i < postingListBlocks.length; i++)
            {
                if (endOfPostingListFlag[i])
                    continue;

                if (postingListBlocks[i].getCurrentDocId() == minCurrentDocId)
                {
                    score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(), minDocIdDocumentLength, docFreqs[i]);
                    //score += ScoreFunction.computeTFIDF(postingListBlocks[i].getCurrentTf(), docFreqs[i]);
                    numElementsRead[i]++;
                    if (postingListBlocks[i].next() == - 1)
                    {
                        //uploadPostingListBlock(i, numElementsRead[i], POSTING_LIST_BLOCK_LENGTH); // load another block
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
                    //score += ScoreFunction.computeTFIDF(postingListBlocks[i].getCurrentTf(), docFreqs[i]);
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
            //System.out.println("minCurrentDocId = " + minCurrentDocId);
        }

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
        for (int i = 0; i < postingListBlocks.length; i++)
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
}
