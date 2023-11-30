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

import static it.unipi.mircv.Config.MIN_NUM_POSTING_TO_SKIP;
import static it.unipi.mircv.Config.POSTING_LIST_BLOCK_LENGTH;

public class ConjunctiveDAAT {
    protected final int numTermQuery;
    protected final int[] docFreqs;
    protected final int[] offsets;
    protected final PostingListBlock[] postingListBlocks;
    protected final SkipDescriptor[] skipDescriptors;
    protected final DocumentIndexHandler documentIndexHandler;
    protected final InvertedIndexHandler invertedIndexHandler;
    protected float currentDocScore;
    protected Integer currentDocLen;

    public ConjunctiveDAAT(String[] queryTerms) throws IOException {
        LexiconHandler lexiconHandler = new LexiconHandler();
        documentIndexHandler = new DocumentIndexHandler();
        invertedIndexHandler = new InvertedIndexHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();

        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs = new int[numTermQuery];
        offsets = new int[numTermQuery];
        skipDescriptors = new SkipDescriptor[numTermQuery];

        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);

            if(docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
                skipDescriptors[i] = skipDescriptorFileHandler.readSkipDescriptor(
                        lexiconHandler.getOffsetSkipDesc(entryBuffer), (int) Math.ceil(Math.sqrt(docFreqs[i])));
                postingListBlocks[i] = new PostingListBlock();
                postingListBlocks[i].setDummyFields();
            }
            else{
                skipDescriptors[i] = null;
                postingListBlocks[i] = invertedIndexHandler.getPostingList(offsets[i], docFreqs[i]);
            }
        }

        sortArraysByArray(docFreqs, offsets, skipDescriptors, postingListBlocks);
    }

    public ArrayList<Integer> processQuery() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        int postingCount = 0;
        int currentDocId;
        int offsetNextGEQ;
        boolean docIdNotInAllPostingLists;

        if(skipDescriptors[0] != null)
            uploadPostingListBlock(0, postingCount, POSTING_LIST_BLOCK_LENGTH); //load the first posting list block

        while(postingCount < docFreqs[0]){
            currentDocId = postingListBlocks[0].getCurrentDocId();
            postingCount++;
            currentDocScore = 0;
            currentDocLen = 0; // it will be updated only if
            docIdNotInAllPostingLists = false;

            //calculate the partial score for the other posting list if they contain the currentDocId
            for (int i = 1; i < numTermQuery; i++) {
                //if skipDescriptors[i] is not null load a posting list block by block
                if(skipDescriptors[i] != null){
                    offsetNextGEQ = skipDescriptors[i].nextGEQ(currentDocId); // get the nextGEQ of the current posting list
                    if(offsetNextGEQ == -1)
                    {
                        return heapScores.getTopDocIdReversed();
                        //docIdNotInAllPostingLists = true;
                        //break;
                    }
                    else
                    {
                        int postingListSkipBlockSize = (int) Math.sqrt(docFreqs[i]); //compute the skip size (square root of the posting list length)
                        if (!(postingListBlocks[i].getMaxDocID() > currentDocId                  // controllo il range del currentDocId per vedere
                                && postingListBlocks[i].getMinDocID() < currentDocId)) // se siamo nello stesso blocco di prima
                            uploadPostingListBlock(i, (offsetNextGEQ - offsets[i]), postingListSkipBlockSize);
                    }
                }

                if(currentDocIdInPostingList(i, currentDocId))
                    updateCurrentDocScore(i);
                else
                {
                    docIdNotInAllPostingLists = true;
                    break;
                }
            }

            if(docIdNotInAllPostingLists == false) {
                updateCurrentDocScore(0);
                heapScores.insertIntoPriorityQueue(currentDocScore, currentDocId);
            }

            if (postingListBlocks[0].next() == -1)
                uploadPostingListBlock(0, postingCount, POSTING_LIST_BLOCK_LENGTH);
        }
        return heapScores.getTopDocIdReversed();
    }

    protected boolean currentDocIdInPostingList(int indexTerm, int currentDocId){
        do{
            if(postingListBlocks[indexTerm].getCurrentDocId() == currentDocId) return true;
            if(postingListBlocks[indexTerm].getCurrentDocId() > currentDocId) break;

        } while (postingListBlocks[indexTerm].next() != -1);
        return false;
    }

    protected void updateCurrentDocScore(int index) throws IOException {
        if (index == 1) { // prima era index == 1
            currentDocLen = documentIndexHandler.readDocumentLength(postingListBlocks[index].getCurrentDocId());
        }
        currentDocScore += ScoreFunction.BM25(postingListBlocks[index].getCurrentTf(), currentDocLen, docFreqs[index]);
    }

    protected void uploadPostingListBlock(int indexTerm, int readElement, int blockSize) throws IOException {
        //Upload the posting list block
        //if the element to read are less in size than "blockSize", read the remaining elements
        //otherwise read a posting list block of size "blockSize"
        /*if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[0] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        }
        else {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize);
        }*/
        if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        }
        else {
            postingListBlocks[indexTerm] = invertedIndexHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize
            );
        }
    }
    protected static void sortArraysByArray(int[] arrayToSort, int[] otherArray, SkipDescriptor[] otherOtherArray, PostingListBlock[] otherOtherOtherArray){
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

                //Change the elements in the othersArray to sort
                temp = otherArray[i];
                otherArray[i] = otherArray[indexes[i]];
                otherArray[indexes[i]] = temp;

                SkipDescriptor tempSkipDescriptor = otherOtherArray[i];
                otherOtherArray[i] = otherOtherArray[indexes[i]];
                otherOtherArray[indexes[i]] = tempSkipDescriptor;

                PostingListBlock postingListBlock = otherOtherOtherArray[i];
                otherOtherOtherArray[i] = otherOtherOtherArray[indexes[i]];
                otherOtherOtherArray[indexes[i]] = postingListBlock;

                // Update the indexes array
                indexes[indexes[i]] = indexes[i];
            }
        }
    }

}
