package it.unipi.mircv;

import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.utility.Config;
import it.unipi.mircv.utility.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static it.unipi.mircv.utility.Config.INDEX_PATH;
import static it.unipi.mircv.utility.Parameters.*;
import static it.unipi.mircv.utility.Parameters.QueryProcessor.*;
import static it.unipi.mircv.utility.Parameters.Score.BM25;
import static it.unipi.mircv.utility.Parameters.Score.TFIDF;
import static it.unipi.mircv.utility.Parameters.docsLen;
import static it.unipi.mircv.utility.Utils.*;

public class TestQuerysResults {

    @Test
    void testMaxScoreAndDisjunctive() throws IOException {
        // test if Disjunctive DAAT returns the same results as MaxScore
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Config.INDEX_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        // querys to be processed
        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                    "New York", "best lunch dishes"};

        // work with TFIDF to compute the score
        for (int i = 0; i < querys.length; i++)
        {
            String[] resultsDisjunctive = SystemEvaluator.queryResult(querys[i], DISJUNCTIVE_MAX_SCORE);
            String[] resultsMaxScore = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            Assertions.assertEquals(resultsMaxScore.length,resultsDisjunctive.length);
            for (int j = 0; j < resultsMaxScore.length; j++)
                Assertions.assertEquals(resultsDisjunctive[j],resultsMaxScore[j]);
        }

        scoreType = BM25; // work with BM25 to compute the score
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
        // test if Conjunctive DAAT returns the same results with and without Skipping with nextGEQ
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        INDEX_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        // querys to be processed
        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        // work with TFIDF to compute the scores
        for (int i = 0; i < querys.length; i++)
        {
            String[] resultsConjunctiveWithoutSkipping = SystemEvaluator.queryResult(querys[i], CONJUNCTIVE_DAAT_NO_SKIPPING);
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            Assertions.assertEquals(resultsConjunctiveWithoutSkipping.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithoutSkipping[j]);
        }

        scoreType = BM25; // work with BM25 to compute the scores
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
        // test if Disjunctive DAAT returns the same results with and without compression
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Utils.loadStopWordList();

        // querys to be processed
        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        scoreType = TFIDF; // work with TFIDF to compute the scores
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression(); // switch to uncompressed index
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            setParametersForCompression(); // switch to compressed index
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        scoreType = BM25; // work with BM25 to compute the scores
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression(); // switch to uncompressed index
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT);
            setParametersForCompression(); // switch to compressed index
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],CONJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Compression --> SUCCESSFUL");
    }

    @Test
    void testDisjunctiveWithAndWithoutCompression() throws IOException {
        // test if Disjunctive DAAT returns the same results with and without compression
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Utils.loadStopWordList();

        // querys to be processed
        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        scoreType = TFIDF;  // use TFIDF for computing the scores
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression(); // switch to uncompressed index
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            setParametersForCompression(); // switch to compressed index
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        scoreType = BM25;  // use BM25 for computing the scores
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression(); // switch to uncompressed index
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT);
            setParametersForCompression(); // switch to compressed index
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_DAAT_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Compression --> SUCCESSFUL");
    }

    @Test
    void testMaxScoreWithAndWithoutCompression() throws IOException {
        // test if MaxScore returns the same results with and without compression
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        Utils.loadStopWordList();

        // querys to be processed
        String[] querys = new String[]{"10 100","railroad workers","detection system", "project", "apple fruit",
                "New York", "best lunch dishes"};

        scoreType = TFIDF;  // use TFIDF for computing the scores
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression(); // switch to uncompressed index
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE);
            setParametersForCompression();  // switch to compressed index
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        scoreType = BM25; // use BM25 for computing the scores
        for (int i = 0; i < querys.length; i++)
        {
            setParametersForNoCompression(); // switch to uncompressed index
            String[] resultsConjunctive = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE);
            setParametersForCompression();  // switch to compressed index
            String[] resultsConjunctiveWithCompression = SystemEvaluator.queryResult(querys[i],DISJUNCTIVE_MAX_SCORE_C);
            Assertions.assertEquals(resultsConjunctiveWithCompression.length,resultsConjunctive.length);
            for (int j = 0; j < resultsConjunctive.length; j++)
                Assertions.assertEquals(resultsConjunctive[j],resultsConjunctiveWithCompression[j]);
        }

        System.out.println("\ntest on Conjunctive and Conjunctive Without Compression --> SUCCESSFUL");
    }

    private void setParametersForNoCompression() throws IOException {   // set the parameters to work with the
        INDEX_PATH = "dataForQueryTest";                             // uncompressed index
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
    }

    private void setParametersForCompression() throws IOException {     // set the parameters to work with the
        INDEX_PATH = "dataForQueryTestCompressed";                   // compressed index
        setFilePaths();
        printFilePaths();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
    }

}
