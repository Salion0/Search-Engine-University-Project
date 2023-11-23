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
        upperBoundScores = new float[numTermQuery];
        skipDescriptors = new SkipDescriptor[numTermQuery];
        //TODO per MaxScore sorta in base agli upper bounds dei terms

        //search for the query terms in the lexicon
        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
            upperBoundScores[i] = lexiconHandler.getTermUpperBoundScore(entryBuffer);
            System.out.println("upperBoundScores[i] = " + upperBoundScores[i]);

            if (docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)) {
                System.out.println("offsetToSkipSrittoNel Lexicon: " + lexiconHandler.getOffsetSkipDesc(entryBuffer));
                skipDescriptors[i] = skipDescriptorFileHandler.readSkipDescriptor(
                        lexiconHandler.getOffsetSkipDesc(entryBuffer), (int) Math.ceil(Math.sqrt(docFreqs[i])));
                postingListBlocks[i] = new PostingListBlock();
                postingListBlocks[i].setDummyFields(); // this to avoid using a boolean for the already read block optimization
            } else {
                skipDescriptors[i] = null;
                //load in main memory the posting list for which there is no skipDescriptor cause they are too small
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]);
            }
        }

        for (int i = 0; i < numTermQuery; i++) {
            // break se non trovo il currentDocId in una delle altre posting list
            System.out.println("queryTerm: " + queryTerms[i] + " docFreq: " + docFreqs[i]);
        }
        //sort arrays for posting list Length
        //sortArraysByArray(docFreqs, offsets, skipDescriptors, postingListBlocks);
        sortArraysByArray(upperBoundScores, docFreqs, offsets, skipDescriptors, postingListBlocks);

        for (int i = 0; i < numTermQuery; i++) {
            // break se non trovo il currentDocId in una delle altre posting list
            System.out.println("docFreq: " + docFreqs[i] + " offset: " + offsets[i] + " postList: " + i + postingListBlocks[i]);
        }
    }

    private void uploadPostingListBlock(int indexTerm, int readElement, int blockSize) throws IOException {
        //Upload the posting list block
        //if the element to read are less in size than "blockSize", read the remaining elements
        //otherwise read a posting list block of size "blockSize"
        if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[0] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        } else {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize);
        }
        if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        } else {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize
            );
        }
    }

    public static void sortArraysByArray(float[] arrayToSort, int[] otherArray, int[] otherOtherArray,
                                         SkipDescriptor[] otherOtherOtherArray, PostingListBlock[] otherOtherOtherOtherArray) {
        // Sort all the input arrays according to the elements of the first array
        // Initialize an array of indexes to keep track of the original positions of the elements
        Integer[] indexes = new Integer[arrayToSort.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        // Sort the indexes array according to the elements of the array to sort
        Arrays.sort(indexes, Comparator.comparingDouble(i -> arrayToSort[i]));
        // Apply the same permutation to the other arrays
        for (int i = 0; i < arrayToSort.length; i++) {
            if (indexes[i] != i) {
                //Change the elements in the array to sort
                //int temp = arrayToSort[i];
                float temp = arrayToSort[i];
                arrayToSort[i] = arrayToSort[indexes[i]];
                arrayToSort[indexes[i]] = temp;

                //Change the elements in the othersArray to sort
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

                // Update the indexes array
                indexes[indexes[i]] = indexes[i];
            }
        }
    }

    // ************************  -- MAX SCORE --   ****************************************
    public ArrayList<Integer> computeMaxScore() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        float[] documentUpperBounds = new float[postingListBlocks.length]; // ub
        int[] numElementsRead = new int[postingListBlocks.length];
        float minScoreInHeap = 0;
        int pivot = 0;
        int minCurrentDocId;

        documentUpperBounds[0] = upperBoundScores[0];
        for (int i = 1; i < postingListBlocks.length; i++)
            documentUpperBounds[i] = documentUpperBounds[i - 1] + upperBoundScores[i];

        for (int i = 0; i < postingListBlocks.length; i++) {
            numElementsRead[i] += POSTING_LIST_BLOCK_LENGTH;
            uploadPostingListBlock(i, 0, POSTING_LIST_BLOCK_LENGTH);
        }

        minCurrentDocId = getMinCurrentDocId();

        while ((pivot < postingListBlocks.length && minCurrentDocId != Integer.MAX_VALUE) || minCurrentDocId == 9982 || minCurrentDocId == 9065) // DEBUG
        {
            float score = 0;
            int next = Integer.MAX_VALUE;

            // ESSENTIAL LISTS
            for (int i = pivot; i < postingListBlocks.length; i++)
            {
                //System.out.println(postingListBlocks[i]);
                if (postingListBlocks[i].getCurrentDocId() == minCurrentDocId)
                {
                    score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(),
                            documentIndexHandler.readDocumentLength(postingListBlocks[i].getCurrentDocId()), docFreqs[i]);

                    if (postingListBlocks[i].next() == - 1)
                        uploadPostingListBlock(i, numElementsRead[i], (int) Math.sqrt(docFreqs[i]));
                    else
                        numElementsRead[i]++;
                }

                if (postingListBlocks[i].getCurrentDocId() < next)
                    next = postingListBlocks[i].getCurrentDocId();
            }

            // NON-ESSENTIAL LISTS
            for (int i = pivot - 1; i >= 0; i--)
            {
                if (score + documentUpperBounds[i] <= minScoreInHeap)
                    break;

                int offsetNextGEQ = skipDescriptors[i].nextGEQ(minCurrentDocId); // get the nextGEQ of the current posting list
                if (offsetNextGEQ != -1) {
                    numElementsRead[i] = offsetNextGEQ - offsets[i];
                    uploadPostingListBlock(i, (offsetNextGEQ - offsets[i]), (int) Math.sqrt(docFreqs[i]));
                }

                System.out.println("currentDocId = " + postingListBlocks[i].getCurrentDocId());
                if (postingListBlocks[i].getCurrentDocId() == minCurrentDocId)
                {
                    score += ScoreFunction.BM25(postingListBlocks[i].getCurrentTf(),
                            documentIndexHandler.readDocumentLength(postingListBlocks[i].getCurrentDocId()), docFreqs[i]);
                }
            }

            // LIST PIVOT UPDATE
            if (heapScores.insertIntoPriorityQueue(score, minCurrentDocId) == true)
            {
                minScoreInHeap = heapScores.getMinScore();
                System.out.println("documentUpperBounds[pivot] = " + documentUpperBounds[pivot]);
                while(pivot < postingListBlocks.length && documentUpperBounds[pivot] <= minScoreInHeap)
                    pivot++;
            }

            minCurrentDocId = next;
            System.out.print("next = " + next + ", minCurrentDocId = " + minCurrentDocId + "\n");
        }

        return heapScores.getTopDocIdReversed();
    }

    public int getMinCurrentDocId() {
        int minCurrentDocId = Integer.MAX_VALUE;
        for (int i = 0; i < postingListBlocks.length; i++)
            if (postingListBlocks[i].getCurrentDocId() < minCurrentDocId)
                minCurrentDocId = postingListBlocks[i].getCurrentDocId();

        return minCurrentDocId;
    }

}
