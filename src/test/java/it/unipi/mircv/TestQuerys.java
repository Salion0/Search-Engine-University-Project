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
import static it.unipi.mircv.Parameters.Score.BM25;
import static it.unipi.mircv.Parameters.Score.TFIDF;
import static it.unipi.mircv.Parameters.docsLen;
import static it.unipi.mircv.Utils.*;

public class TestQuerys {

    @Test
    void testMaxScoreAndDisjunctive() throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Config.STARTING_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                    "New York", "best lunch dishes"};

        for (int i = 0; i < querys.length; i++)
        {
            String[] resultsDisjunctive = SystemEvaluator.queryResult(querys[i], DISJUNCTIVE_MAX_SCORE);
            String[] resultsMaxScore = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            Assertions.assertEquals(resultsMaxScore.length,resultsDisjunctive.length);
            for (int j = 0; j < resultsMaxScore.length; j++)
                Assertions.assertEquals(resultsDisjunctive[j],resultsMaxScore[j]);
        }

        scoreType = BM25;
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

    @Test
    void testConjuctiveWithAndWithoutSkipping() throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        STARTING_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        for (int i = 0; i < querys.length; i++)
        {
            String[] resultsConjunctiveWithoutSkipping = SystemEvaluator.queryResult(querys[i], CONJUNCTIVE_DAAT_NO_SKIPPING);
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            Assertions.assertEquals(resultsConjunctiveWithoutSkipping.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithoutSkipping[j]);
        }

        scoreType = BM25;
        for (int i = 0; i < querys.length; i++)
        {
            String[] resultsConjunctiveWithoutSkipping = SystemEvaluator.queryResult(querys[i], CONJUNCTIVE_DAAT_NO_SKIPPING);
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            Assertions.assertEquals(resultsConjunctiveWithoutSkipping.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithoutSkipping[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Skipping --> SUCCESSFUL");
    }

    @Test
    void testConjuctiveWithAndWithoutCompression() throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Utils.loadStopWordList();
        //setParametersForNoCompression();

        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression();
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            setParametersForCompression();
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        scoreType = BM25;
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression();
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            setParametersForCompression();
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Compression --> SUCCESSFUL");
    }

    @Test
    void testDisjunctiveWithAndWithoutCompression() throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Utils.loadStopWordList();
        //setParametersForNoCompression();

        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression();
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            setParametersForCompression();
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        scoreType = BM25;
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression();
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            setParametersForCompression();
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Compression --> SUCCESSFUL");
    }

    @Test
    void testMaxScoreWithAndWithoutCompression() throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Utils.loadStopWordList();
        //setParametersForNoCompression();

        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression();
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE);
            setParametersForCompression();
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        scoreType = BM25;
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression();
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE);
            setParametersForCompression();
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Compression --> SUCCESSFUL");
    }

    private void setParametersForNoCompression() throws IOException {
        STARTING_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
    }

    private void setParametersForCompression() throws IOException {
        STARTING_PATH = "dataForQueryTestCompressed";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
    }

}
