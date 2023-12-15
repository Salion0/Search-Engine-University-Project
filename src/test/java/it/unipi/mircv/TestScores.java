package it.unipi.mircv;


import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.query.ScoreFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Config.flagCompressedReading;
import static it.unipi.mircv.Utils.cleanFolder;
import static it.unipi.mircv.Utils.setFilePaths;

public class TestScores {

    @Test
    void buildTestIndex() throws IOException {
        flagStemming=false;
        flagStopWordRemoval=true;
        flagCompressedReading=false;
        STARTING_PATH = "dataForScoreTest";
        setFilePaths();
        Index index1 = new Index(STARTING_PATH + '/',"test_collection_for_query.tsv",false);
        BlockMerger blockMerger1 = new BlockMerger();
        blockMerger1.mergeBlocks(index1.getNumberOfBlocks());
        System.out.println(POSTING_LIST_DESC_FILE);
        System.out.println(DOC_ID_FILE);
        System.out.println(DOCUMENT_INDEX_FILE);
    }

    @Test
    void testTFIDF() throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        int[] tf = new int[]{3,4,5,2};
        int[] docFreq = new int[]{2,4,6,7};
        int N = 10000;
        System.out.println(Config.collectionSize);
        // compute TFIDF with wolframAplha w.r.t the formula in the slides
        float[] actualResults = new float[]{5.46382f,5.44370f,5.47382f,4.10462f};
        float[] predictedResult = new float[4];
        for (int i = 0; i < 4; i++) {
            predictedResult[i] = ScoreFunction.computeTFIDF(tf[i],docFreq[i]);
        }

        for (int i = 0; i < 4; i++)
            Assertions.assertEquals((int) (actualResults[i]*1000),(int) (predictedResult[i]*1000));

        System.out.println("test on the method TFIDF --> SUCCESSFUL");
    }

    @Test
    void testBM25() throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        System.out.println(collectionSize);
        System.out.println(avgDocLen);
        int[] tf = new int[]{3,4,5,2};
        int[] docLen = new int[]{27,27,43,35};
        int[] docFreq = new int[]{2,4,6,7};
        float avgDocLen = 26.709f;
        int N = 10000;
        // compute BM25 with wolframAplha w.r.t the formula in the slides
        float[] actualResults = new float[]{2.45928f,2.46573f,2.24170f,1.63924f};
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

    @Test
    void testTFIDFOnQuery() throws IOException {
        STARTING_PATH = "dataForScoreTest";
        setFilePaths();
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        PostingListBlock[] postingListBlocks = new PostingListBlock[3];
        postingListBlocks[0] = getPostingListFromLexiconEntry("project");
        postingListBlocks[1] = getPostingListFromLexiconEntry("legacy");
        postingListBlocks[2] = getPostingListFromLexiconEntry("science");

        ArrayList<Integer> arraysOfResults0 = new ArrayList<>(List.of(699, 701, 702, 703, 704, 708));
        ArrayList<Integer> arraysOfResults1 = new ArrayList<>(List.of(719, 721, 722, 726));
        ArrayList<Integer> arraysOfResults2 = new ArrayList<>(List.of(730, 6678));


        System.out.println(documentIndexHandler.readDocumentLength(0));
        System.out.println(documentIndexHandler.readDocumentLength(1));
        System.out.println(documentIndexHandler.readDocumentLength(2));
    }

    public static PostingListBlock getPostingListFromLexiconEntry(String string) throws IOException {
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        InvertedIndexFileHandler invertedIndexHandler = new InvertedIndexFileHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        System.out.println(postingListBlock);
        return postingListBlock;
    }
}
