package it.unipi.mircv;

import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.query.DisjunctiveDAAT;
import it.unipi.mircv.query.MaxScoreDisjunctive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.Config.STARTING_PATH;
import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.TFIDF;
import static it.unipi.mircv.Parameters.docsLen;
import static it.unipi.mircv.Utils.removeStopWords;
import static it.unipi.mircv.Utils.setFilePaths;

public class TestQuerys {

    public static void main(String[] args) throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Parameters.collectionSize = documentIndexHandler.readCollectionSize();
        Parameters.avgDocLen = documentIndexHandler.readAvgDocLen();

    }

    @Test
    void testMaxScoreAndDisjunctive() throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        STARTING_PATH = "dataForQueryTest";
        setFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();


        String[] querys = new String[]{"10 100","railroad workers"};

        for (int i = 0; i < querys.length; i++)
        {
            String[] resultsDisjunctive = SystemEvaluator.queryResult(querys[i], DISJUNCTIVE_MAX_SCORE);
            String[] resultsMaxScore = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            Assertions.assertEquals(resultsMaxScore.length,resultsDisjunctive.length);
            for (int j = 0; j < resultsMaxScore.length; j++)
                Assertions.assertEquals(resultsDisjunctive[j],resultsMaxScore[j]);
        }

        System.out.println("\ntest on Disjunctive and MaxScore --> SUCCESSFUL");
    }

    public static ArrayList<Integer> testDisjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);


        DisjunctiveDAAT noPriorityQueueDisjunctive = new DisjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = noPriorityQueueDisjunctive.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
        return results;
    }

    public static ArrayList<Integer> testMaxScore(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        MaxScoreDisjunctive maxScore = new MaxScoreDisjunctive(queryTerms);
        ArrayList<Integer> results = maxScore.computeMaxScore();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("MAX-SCORE finished in " + (float)elapsedTime/1000 +"sec");
        return results;
    }
}
