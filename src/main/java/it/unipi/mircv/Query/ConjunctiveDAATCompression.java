package it.unipi.mircv.Query;

import it.unipi.mircv.File.*;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Index.SkipDescriptorCompression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static it.unipi.mircv.Config.*;

public class ConjunctiveDAATCompression {
    protected final int numTermQuery;
    protected final int[] docFreqs;
    protected final long[] offsetsDocId;
    protected final long[] offsetsTermFreq;
    protected final PostingListBlock[] postingListBlocks;
    protected final SkipDescriptorCompression[] skipDescriptorsCompression;
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

        skipDescriptorsCompression = new SkipDescriptorCompression[numTermQuery];

        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconFileHandler.findTermEntryCompression(queryTerms[i]);
            docFreqs[i] = lexiconFileHandler.getDfCompression(entryBuffer);
            offsetsDocId[i] = lexiconFileHandler.getOffsetDocIdCompression(entryBuffer);
            offsetsTermFreq[i] = lexiconFileHandler.getOffsetTermFreqCompression(entryBuffer);

            if(docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
                skipDescriptorsCompression[i] = skipDescriptorFileHandler.readSkipDescriptorCompression(
                        lexiconFileHandler.getOffsetSkipDescCompression(entryBuffer), (int) Math.ceil(Math.sqrt(docFreqs[i])));
                postingListBlocks[i] = new PostingListBlock();
                postingListBlocks[i].setDummyFields();
            }
            else{
                skipDescriptorsCompression[i] = null;

                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(docFreqs[i],
                        offsetsDocId[i], lexiconFileHandler.getNumByteDocId(entryBuffer),
                        offsetsTermFreq[i], lexiconFileHandler.getNumByteTermFreq(entryBuffer));
            }
        }
        //TODO anzichè ordinare 300 array basterebbe ordinare gli entryBuffer facendo una prima get della sola docfreq
        sortArraysByArray(docFreqs, offsetsDocId, offsetsTermFreq, skipDescriptorsCompression, postingListBlocks);
    }

    public ArrayList<Integer> processQuery() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        int postingCount = 0;
        int currentDocId;
        long[] offsetNextGEQ;
        boolean docIdInAllPostingLists;

        if(skipDescriptorsCompression[0] != null)
            uploadPostingListBlockCompression(0, postingCount, POSTING_LIST_BLOCK_LENGTH); //load the first posting list block
        //else -> it was already loaded

        while(postingCount < docFreqs[0]){
            currentDocId = postingListBlocks[0].getCurrentDocId();
            postingCount++;
            currentDocScore = 0;
            currentDocLen = 0;
            docIdInAllPostingLists = true;

            //calculate the partial score for the other posting list if they contain the currentDocId
            for (int i = 1; i < numTermQuery; i++) {
                //if skipDescriptors[i] is not null load a posting list block by block
                if(skipDescriptorsCompression[i] != null){
                    offsetNextGEQ = skipDescriptorsCompression[i].nextGEQ(currentDocId); // get the nextGEQ of the current posting list
                    if(offsetNextGEQ[0] == -1)
                    {
                        return heapScores.getTopDocIdReversed();
                        //docIdNotInAllPostingLists = true;
                        //break;
                    }
                    else
                    {
                        //TODO questo si può portare fuori da qui e mettere un array globale così ogni volta sipuò evitare di calcolarlo
                        int postingListSkipBlockSize = (int) Math.sqrt(docFreqs[i]); //compute the skip size (square root of the posting list length)
                        if (!(postingListBlocks[i].getMaxDocID() > currentDocId                  // controllo il range del currentDocId per vedere
                                && postingListBlocks[i].getMinDocID() < currentDocId)) // se siamo nello stesso blocco di prima
                            uploadPostingListBlockCompression(i, (offsetNextGEQ - offsets[i]), postingListSkipBlockSize);
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

            if (postingListBlocks[0].next() == -1)
                uploadPostingListBlockCompression(0, postingCount, POSTING_LIST_BLOCK_LENGTH);
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
        currentDocScore += ScoreFunction.BM25(postingListBlocks[index].getCurrentTf(), currentDocLen, docFreqs[index]);
    }
    protected void uploadPostingListBlockCompression(int indexTerm, int readElement, int blockSize) throws IOException {
        postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingList(
                docFreqs[indexTerm],
                offsetsDocId, offsetsTermFreq
                offsets[indexTerm] + readElement,
                docFreqs[indexTerm] - readElement
        );



        if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        }
        else {
            postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize
            );
        }
    }
    /*
    protected void uploadPostingListBlock(int indexTerm, int readElement, int blockSize) throws IOException {
        if (docFreqs[indexTerm] - readElement < blockSize) {
            postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    docFreqs[indexTerm] - readElement
            );
        }
        else {
            postingListBlocks[indexTerm] = invertedIndexFileHandler.getPostingList(
                    offsets[indexTerm] + readElement,
                    blockSize
            );
        }
    }*/
    protected static void sortArraysByArray(int[] arrayToSort, long[] array1, long[] array2, SkipDescriptorCompression[] array3, PostingListBlock[] array4){
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
