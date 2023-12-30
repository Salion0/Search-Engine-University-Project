package it.unipi.mircv;


import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.utility.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.unipi.mircv.utility.Config.INDEX_PATH;
import static it.unipi.mircv.utility.Parameters.*;
import static it.unipi.mircv.utility.Parameters.QueryProcessor.*;
import static it.unipi.mircv.utility.Parameters.Score.BM25;
import static it.unipi.mircv.utility.Parameters.Score.TFIDF;
import static it.unipi.mircv.utility.Utils.printFilePaths;
import static it.unipi.mircv.utility.Utils.setFilePaths;

public class TestScores {

    @Test
    void testTFIDF() throws IOException {
        // compute the actual scores w.r.t TFIDF for the docIds in the collection and compare them with the ones computed with
        // our method to process the querys (in Disjunctive DAAT mode)
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForScoreTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
        printCollectionStatistics();
        Utils.loadStopWordList();
        HashMap<Float, ArrayList<Integer>> score2DocIdMap;

        // evaluate the query
        String[] querys = new String[]{"Manhattan project"};
        score2DocIdMap = SystemEvaluator.queryResultForTest(querys[0],DISJUNCTIVE_DAAT);

        // To compute the scores, WolframAplha was used
        Assertions.assertEquals(new ArrayList<>(List.of(2)),score2DocIdMap.get(0.39794f)); // computed score for docId = 2
        Assertions.assertEquals(new ArrayList<>(List.of(3)),score2DocIdMap.get(1.0406106f)); // computed score for docId = 3
        Assertions.assertEquals(new ArrayList<>(List.of(0,8)),score2DocIdMap.get(0.9208187f)); // computed score for docId = 2 and docId = 8

        // evaluate the query
        querys = new String[]{"science secret"};
        score2DocIdMap = SystemEvaluator.queryResultForTest(querys[0],DISJUNCTIVE_DAAT);

        // To compute the scores, WolframAplha was used
        Assertions.assertEquals(new ArrayList<>(List.of(1,2)),score2DocIdMap.get(0.69897f)); // computed score for docId = 1 and docId = 2
        Assertions.assertEquals(new ArrayList<>(List.of(8)),score2DocIdMap.get(1.0f)); // computed score for docId = 8

        // evaluate the query
        querys = new String[]{"into the ocean"};
        score2DocIdMap = SystemEvaluator.queryResultForTest(querys[0],DISJUNCTIVE_DAAT);

        // To compute the scores, WolframAplha was used
        Assertions.assertEquals(new ArrayList<>(List.of(9)),score2DocIdMap.get(1.0f)); // computed score for docId = 9

        System.out.println("test on the method TFIDF --> SUCCESSFUL");
    }

    @Test
    void testBM25() throws IOException {
        // compute the actual scores w.r.t BM25 for the docIds in the collection and compare them with the ones computed with
        // our method to process the querys (in Disjunctive DAAT mode)
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForScoreTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = BM25;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
        printCollectionStatistics();
        Utils.loadStopWordList();
        HashMap<Float, ArrayList<Integer>> score2DocIdMap;

        // evaluate the query
        String[] querys = new String[]{"Manhattan project"};
        score2DocIdMap = SystemEvaluator.queryResultForTest(querys[0],DISJUNCTIVE_DAAT);

        // To compute the scores, WolframAplha was used
        Assertions.assertEquals(new ArrayList<>(List.of(0)),score2DocIdMap.get(0.40475547f)); // computed score for docId = 0
        Assertions.assertEquals(new ArrayList<>(List.of(2)),score2DocIdMap.get(0.16409898f)); // computed score for docId = 2
        Assertions.assertEquals(new ArrayList<>(List.of(3)),score2DocIdMap.get(0.37058663f)); // computed score for docId = 3
        Assertions.assertEquals(new ArrayList<>(List.of(8)),score2DocIdMap.get(0.37971908f)); // computed score for docId = 8

        // evaluate the query
        querys = new String[]{"science secret"};
        score2DocIdMap = SystemEvaluator.queryResultForTest(querys[0],DISJUNCTIVE_DAAT);

        // To compute the scores, WolframAplha was used
        Assertions.assertEquals(new ArrayList<>(List.of(1)),score2DocIdMap.get(0.25650275f)); // computed score for docId = 1
        Assertions.assertEquals(new ArrayList<>(List.of(2)),score2DocIdMap.get(0.28823504f)); // computed score for docId = 2
        Assertions.assertEquals(new ArrayList<>(List.of(8)),score2DocIdMap.get(0.41237113f)); // computed score for docId = 8

        // evaluate the query
        querys = new String[]{"into the ocean"};
        score2DocIdMap = SystemEvaluator.queryResultForTest(querys[0],DISJUNCTIVE_DAAT);

        // To compute the scores, WolframAplha was used
        Assertions.assertEquals(new ArrayList<>(List.of(9)),score2DocIdMap.get(0.3478261f)); // computed score for docId = 9

        System.out.println("test on the method BM25 --> SUCCESSFUL");
    }

    private static void printCollectionStatistics() {   // print AverageDocumentLength, CollectionSize and the
                                                        // document lengths of the collection
        System.out.println("avgDocLen = " + avgDocLen);
        System.out.println("collectionSize = " + collectionSize);
        for (int i = 0; i < 10; i++)
            System.out.println("docLen[" + i + "] = " + docsLen[i]);
    }
}
