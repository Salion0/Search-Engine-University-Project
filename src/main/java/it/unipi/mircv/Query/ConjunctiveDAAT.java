package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ConjunctiveDAAT {
    private int numTermQuery;
    private final int[] docFreqs;
    private final int[] offsets;
    private final DocumentIndexHandler documentIndexHandler;
    private final InvertedIndexHandler invertedIndexHandler;
    public ConjunctiveDAAT(String[] queryTerms) throws IOException {
        LexiconHandler lexiconHandler = new LexiconHandler();
        documentIndexHandler = new DocumentIndexHandler();
        invertedIndexHandler = new InvertedIndexHandler();

        numTermQuery = queryTerms.length;

        docFreqs =  new int[numTermQuery];
        offsets = new int[numTermQuery];
        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
        }
        sortArraysByArray(docFreqs, offsets);
    }

    public ArrayList<Integer> processQuery() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        System.out.println();
        float currentDocScore;

        return heapScores.getTopDocIdReversed();
    }

    public static void sortArraysByArray(int[] arrayToSort, int[] otherArray) {
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

                // Aggiorna gli indici se necessario
                indexes[indexes[i]] = indexes[i];
            }
        }
    }
}
