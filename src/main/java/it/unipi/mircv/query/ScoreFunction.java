package it.unipi.mircv.query;

import static it.unipi.mircv.Parameters.avgDocLen;
import static it.unipi.mircv.Parameters.collectionSize;

public class  ScoreFunction{

    public static Float BM25(int termFrequency, int documentLength, int documentFrequency) {
        return (float) ((( termFrequency / (termFrequency + 1.5 * ((1 - 0.75) + 0.75*(documentLength / avgDocLen))))
                * Math.log10((double)collectionSize/documentFrequency)));
    }
    public Float computeIDF(int documentFrequency) {
        return (float) Math.log10((double) collectionSize/documentFrequency);
    }

    public static Float computeTFIDF(int termFrequency, int documentFrequency) {
        return (float) (((1 + Math.log10(termFrequency)) * Math.log10((double) collectionSize/documentFrequency)));
    }
}
