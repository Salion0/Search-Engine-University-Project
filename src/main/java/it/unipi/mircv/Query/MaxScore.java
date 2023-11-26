package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Index.SkipDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import static it.unipi.mircv.Config.MIN_NUM_POSTING_TO_SKIP;
import static it.unipi.mircv.Config.POSTING_LIST_BLOCK_LENGTH;

public class MaxScore {
    private int numTermQuery;
    private final int[] docFreqs;
    private final int[] offsets;
    private int[] numElementsRead; // count the numbers of element read for each posting list
    private final float[] upperBoundScores;
    private final PostingListBlock[] postingListBlocks;
    private final SkipDescriptor[] skipDescriptors;
    private final DocumentIndexHandler documentIndexHandler;
    private final InvertedIndexHandler invertedIndexHandler;

    public MaxScore(String[] queryTerms) throws IOException {
        //initialize file handlers
        LexiconHandler lexiconHandler = new LexiconHandler();
        documentIndexHandler = new DocumentIndexHandler();
        invertedIndexHandler = new InvertedIndexHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();

        numTermQuery = queryTerms.length;

        //initialize arrays
        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs = new int[numTermQuery];
        offsets = new int[numTermQuery];
        numElementsRead =  new int[postingListBlocks.length]; // count the numbers of element read for each posting list
        upperBoundScores = new float[numTermQuery];
        skipDescriptors = new SkipDescriptor[numTermQuery];

        //search for the query terms in the lexicon
        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
            upperBoundScores[i] = lexiconHandler.getTermUpperBoundScore(entryBuffer);

            if (docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP))
            {
                System.out.println("offsetToSkipSrittoNel Lexicon: " + lexiconHandler.getOffsetSkipDesc(entryBuffer));
                skipDescriptors[i] = skipDescriptorFileHandler.readSkipDescriptor(
                        lexiconHandler.getOffsetSkipDesc(entryBuffer), (int) Math.ceil(Math.sqrt(docFreqs[i])));
                if (POSTING_LIST_BLOCK_LENGTH > docFreqs[i])
                    //we just load the posting list if it is smaller than the chosen treshold for block length
                    postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]);
                else
                    postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], POSTING_LIST_BLOCK_LENGTH);
            }
            else
            {
                skipDescriptors[i] = null;
                //load in main memory the posting list for which there is no skipDescriptor cause they are too small
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]);
            }
        }

        //for (int i = 0; i < numTermQuery; i++)
        //    System.out.println("queryTerm: " + queryTerms[i] + " docFreq: " + docFreqs[i]); // for visual debug

        //sort arrays for upper bound scores
        sortArraysByArray(upperBoundScores, docFreqs, offsets, skipDescriptors, postingListBlocks);

        //for (int i = 0; i < numTermQuery; i++) // for visual debug
        //    System.out.println("docFreq: " + docFreqs[i] + " offset: " + offsets[i] + " postList: " + i + postingListBlocks[i]);
    }

    private void uploadPostingListBlock(int indexTerm, int readElement, int blockSize) throws IOException {
        //Upload the posting list block
        //if the element to read are less in size than "blockSize", read the remaining elements
        //otherwise read a posting list block of size "blockSize"

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
        heapScores.setTopDocCount(20); // initialize the priority queue with 20 elements set to 0
        float[] documentUpperBounds = new float[postingListBlocks.length]; // ub
        float minScoreInHeap = 0; // teta
        int pivot = 0;
        int minCurrentDocId; // current

        documentUpperBounds[0] = upperBoundScores[0];
        for (int i = 1; i < postingListBlocks.length; i++)
            documentUpperBounds[i] = documentUpperBounds[i - 1] + upperBoundScores[i];

        float score;
        int next;
        int countCurrentDocIdInPostingLists; // for checking if the docId is in every posting list
        minCurrentDocId = getMinCurrentDocId(); // get the mi docId between the current element of all posting lists
        while (pivot < postingListBlocks.length && minCurrentDocId != Integer.MAX_VALUE) // DEBUG
        {
            score = 0;
            countCurrentDocIdInPostingLists = 0;
            next = Integer.MAX_VALUE;

            // ESSENTIAL LISTS
            for (int i = pivot; i < postingListBlocks.length; i++)
            {
                //System.out.println("Entrato nel ESSENTIAL LISTS");
                if (postingListBlocks[i].getCurrentDocId() == minCurrentDocId)
                {
                    score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(),
                            documentIndexHandler.readDocumentLength(postingListBlocks[i].getCurrentDocId()), docFreqs[i]);

                    countCurrentDocIdInPostingLists++;
                    numElementsRead[i]++;
                    if (postingListBlocks[i].next() == - 1)
                    {
                        if (numElementsRead[i] >= docFreqs[i]) // check if the entire posting list is finished
                            return heapScores.getTopDocIdReversed(); // terminate processing because one of the posting list is finished
                        uploadPostingListBlock(i, numElementsRead[i], POSTING_LIST_BLOCK_LENGTH); // load another block
                    }
                }
                if (postingListBlocks[i].getCurrentDocId() < next)
                    next = postingListBlocks[i].getCurrentDocId();
            }

            // NON-ESSENTIAL LISTS
            for (int i = pivot - 1; i >= 0; i--)
            {
                //System.out.println("Entrato nel NON-ESSENTIAL LISTS");
                if (score + documentUpperBounds[i] <= minScoreInHeap)
                    break;

                if (skipDescriptors[i] != null) {
                    int offsetNextGEQ = skipDescriptors[i].nextGEQ(minCurrentDocId); // get the nextGEQ of the current posting list
                    if (offsetNextGEQ != -1) {
                        if (!(postingListBlocks[i].getMaxDocID() > minCurrentDocId
                                && postingListBlocks[i].getMinDocID() < minCurrentDocId))
                        {
                            uploadPostingListBlock(i, (offsetNextGEQ - offsets[i]), (int) Math.sqrt(docFreqs[i]));
                            numElementsRead[i] = offsetNextGEQ - offsets[i];
                        }
                    }
                }

                if (currentDocIdInPostingList(i, minCurrentDocId)) //seek currentDocId in the posting list
                {
                    countCurrentDocIdInPostingLists++;
                    score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(),
                            documentIndexHandler.readDocumentLength(postingListBlocks[i].getCurrentDocId()), docFreqs[i]);
                }
            }

            // LIST PIVOT UPDATE
            if (countCurrentDocIdInPostingLists == postingListBlocks.length) // check if the docId is in all the posting lists
            {
                //System.out.println("Entrato nel LIST PIVOT UPDATE con id = " + minCurrentDocId);
                heapScores.insertIntoPriorityQueueMAXSCORE(score, minCurrentDocId);
                minScoreInHeap = heapScores.getMinScore();
                while(pivot < postingListBlocks.length && documentUpperBounds[pivot] <= minScoreInHeap)
                    pivot++;
            }
            minCurrentDocId = next;
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
        for (int i = 0; i < postingListBlocks.length; i++) {
            //System.out.println(" i --> " + postingListBlocks[i].getPostingList());
            if (postingListBlocks[i].getCurrentDocId() < minCurrentDocId)
                minCurrentDocId = postingListBlocks[i].getCurrentDocId();
        }

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
