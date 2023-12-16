package it.unipi.mircv.query;

import static it.unipi.mircv.Parameters.avgDocLen;
import static it.unipi.mircv.Parameters.collectionSize;

public class  ScoreFunction{

    public static Float BM25(int termFrequency, int documentLength, int documentFrequency) {
        return (float) ((float)termFrequency / ((float) termFrequency + 1.6 * (((float) documentLength/avgDocLen)*0.75 + 0.25))
                        * Math.log10((float)collectionSize/documentFrequency));
    }
    public Float computeIDF(int documentFrequency) {
        return (float) Math.log10((double) collectionSize/documentFrequency);
    }

    public static Float computeTFIDF(int termFrequency, int documentFrequency) {
        return (float) ((float)(Math.log10(termFrequency) + 1) * Math.log10((float) collectionSize/documentFrequency));
    }
}
