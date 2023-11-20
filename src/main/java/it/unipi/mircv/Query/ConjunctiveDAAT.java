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
    private int numTermQuery;
    private final int[] docFreqs;
    private final int[] offsets;
    private final PostingListBlock[] postingListBlocks;
    private final SkipDescriptor[] skipDescriptors;
    private final DocumentIndexHandler documentIndexHandler;
    private final InvertedIndexHandler invertedIndexHandler;

    public ConjunctiveDAAT(String[] queryTerms) throws IOException {
        LexiconHandler lexiconHandler = new LexiconHandler();
        documentIndexHandler = new DocumentIndexHandler();
        invertedIndexHandler = new InvertedIndexHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();

        numTermQuery = queryTerms.length;

        docFreqs =  new int[numTermQuery];
        offsets = new int[numTermQuery];
        skipDescriptors = new SkipDescriptor[numTermQuery];
        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);

            if(docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
                skipDescriptors[i] = skipDescriptorFileHandler.readSkipDescriptor(
                        lexiconHandler.getOffsetSkipDesc(entryBuffer), (int) Math.ceil(Math.sqrt(docFreqs[i])));
            }else skipDescriptors[i] = null;

        }
        sortArraysByArray(docFreqs, offsets, skipDescriptors);

        postingListBlocks = new PostingListBlock[numTermQuery];

        for (int i = 1; i < numTermQuery; i++){
            // break se non trovo il currentDocId in una delle altre posting list
            System.out.println("skipDescriptors" + i + skipDescriptors[i]);
        }
    }

    public ArrayList<Integer> processQuery() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        float currentDocScore;

        int countPosting = 0;
        int currentDocId;
        int offsetNextGEQ;
        while(countPosting < docFreqs[0]){
            //Upload the posting list block
            if (docFreqs[0] - countPosting < POSTING_LIST_BLOCK_LENGTH) {
                postingListBlocks[0] = invertedIndexHandler.getPostingList(offsets[0] + countPosting,
                        docFreqs[0] - countPosting);
            }
            else {
                postingListBlocks[0] = invertedIndexHandler.getPostingList(offsets[0] + countPosting, POSTING_LIST_BLOCK_LENGTH);
            }
            System.out.println(postingListBlocks[0]);

            do{
                currentDocId = postingListBlocks[0].getCurrentDocId();
                countPosting ++;
                System.out.println("currentDocID: " + currentDocId);
                for (int i = 1; i < numTermQuery; i++){
                    // break se non trovo il currentDocId in una delle altre posting list

                    offsetNextGEQ = skipDescriptors[i].nextGEQ(currentDocId);
                    System.out.println("offsetNextGEQ: " + offsetNextGEQ);
                    if(offsetNextGEQ == -1){
                        System.out.println("break");
                        break;
                    }else {
                        int postingListSkipBlockSize = (int) Math.sqrt(docFreqs[i]);
                        if (docFreqs[i] - (offsetNextGEQ - offsets[i]) < postingListSkipBlockSize){
                            postingListBlocks[i] = invertedIndexHandler.getPostingList(offsetNextGEQ, docFreqs[i] - (offsetNextGEQ - offsets[i]));
                        }else{
                            postingListBlocks[i] = invertedIndexHandler.getPostingList(offsetNextGEQ, postingListSkipBlockSize);
                        }

                        System.out.println(postingListBlocks[i]);
                        //calcolo lo score
                    }
                }
            }while(postingListBlocks[0].next() != -1);


        }

        return heapScores.getTopDocIdReversed();
    }

    public static void sortArraysByArray(int[] arrayToSort, int[] otherArray, SkipDescriptor[] otherOtherArray) {
        Integer[] indexes = new Integer[arrayToSort.length];
        // Genera un array di indici per tenere traccia della posizione originale degli elementi
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        // Ordina gli indici in base ai valori dell'array principale
        Arrays.sort(indexes, Comparator.comparingInt(i -> arrayToSort[i]));
        // Applica lo stesso ordinamento all'altro array
        for (int i = 0; i < arrayToSort.length; i++) {
            if (indexes[i] != i) {
                // Scambia gli elementi nell'array da ordinare
                int temp = arrayToSort[i];
                arrayToSort[i] = arrayToSort[indexes[i]];
                arrayToSort[indexes[i]] = temp;

                // Scambia gli elementi nell'altro array
                temp = otherArray[i];
                otherArray[i] = otherArray[indexes[i]];
                otherArray[indexes[i]] = temp;

                // Scambia gli elementi nell'altroAltro array
                SkipDescriptor tempSkipDescriptor = otherOtherArray[i];
                otherOtherArray[i] = otherOtherArray[indexes[i]];
                otherOtherArray[indexes[i]] = tempSkipDescriptor;

                // Aggiorna gli indici se necessario
                indexes[indexes[i]] = indexes[i];
            }
        }
    }
}
