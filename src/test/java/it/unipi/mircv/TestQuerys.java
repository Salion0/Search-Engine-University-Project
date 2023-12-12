package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.query.DisjunctiveDAAT;
import it.unipi.mircv.query.MaxScoreDisjunctive;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.Utils.removeStopWords;

public class TestQuerys {

    public static void main(String[] args) throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Parameters.collectionSize = documentIndexHandler.readCollectionSize();
        Parameters.avgDocLen = documentIndexHandler.readAvgDocLen();

        testMaxScoreAndDisjunctive();
    }

    public static void testMaxScoreAndDisjunctive() throws IOException {
        String[] querys = new String[]{"10 100","railroad workers","caries detection system"};

        for (int i = 0; i < 3; i++)
        {
            String string = querys[i];
            ArrayList<Integer> resultsDisjunctive = testDisjunctive(string);
            ArrayList<Integer> resultsMaxScore = testMaxScore(string);
            Assertions.assertEquals(resultsMaxScore,resultsDisjunctive);
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
