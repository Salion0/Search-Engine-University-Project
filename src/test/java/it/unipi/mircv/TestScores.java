package it.unipi.mircv;


import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.query.ScoreFunction;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.ByteBuffer;

import static it.unipi.mircv.Config.STARTING_PATH;
import static it.unipi.mircv.Parameters.collectionSize;
import static it.unipi.mircv.Utils.setFilePaths;

public class TestScores {

    public static void main(String[] args) throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Parameters.collectionSize = documentIndexHandler.readCollectionSize();
        Parameters.avgDocLen = documentIndexHandler.readAvgDocLen();

    }

    public static void testTFIDF() throws IOException {

        int[] tf = new int[]{1,2,3,4};
        int[] docFreq = new int[]{4,6,14,2};
        int N = 2147483647;
        System.out.println(collectionSize);
        // compute TFIDF with wolframAplha w.r.t the formula in the slides
        float[] actualResults = new float[]{8.729f,11.1287f,12.0914f,14.4680f};
        float[] predictedResult = new float[4];
        for (int i = 0; i < 4; i++) {
            predictedResult[i] = ScoreFunction.computeTFIDF(tf[i],docFreq[i]);
        }

        for (int i = 0; i < 4; i++)
            Assertions.assertEquals((int) (actualResults[i]*1000),(int) (predictedResult[i]*1000));

        System.out.println("test on the method TFIDF --> SUCCESSFUL");
    }

    public static void testBM25() throws IOException {

        checkLexiconEntry("dziena",2841806);
        int[] tf = new int[]{1,1,1,2};
        int[] docLen = new int[]{27,27,43,35};
        int[] docFreq = new int[]{4,4,4,4};
        float avgDocLen = 28.248545f;
        int N = 2147483647;
        System.out.println(collectionSize);
        // compute BM25 with wolframAplha w.r.t the formula in the slides
        float[] actualResults = new float[]{3.56281f,3.56281f,2.82751f,4.63261f};
        float[] predictedResult = new float[4];
        for (int i = 0; i < 4; i++)
            predictedResult[i] = ScoreFunction.BM25(tf[i],docLen[i],docFreq[i]);

        for (int i = 0; i < 4; i++)
            Assertions.assertEquals((int) (actualResults[i]*1000),(int) (predictedResult[i]*1000));

        System.out.println("test on the method BM25 --> SUCCESSFUL");
    }

    public static void checkLexiconEntry(String string, int docId) throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        InvertedIndexFileHandler invertedIndexHandler = new InvertedIndexFileHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        float termUpperBoundScore = lexiconHandler.getTermUpperBoundScoreTFIDF(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        System.out.println("term = " + string);
        System.out.println("postingList = " + postingListBlock);
        System.out.println("documentLength = " + documentIndexHandler.readDocumentLength(docId));
        System.out.println("documentFrequency = " + documentFrequency);
    }
}
