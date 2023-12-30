package it.unipi.mircv.evaluation;
import it.unipi.mircv.utility.Parameters.QueryProcessor;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.query.*;
import it.unipi.mircv.utility.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static it.unipi.mircv.utility.Config.MAX_NUM_DOC_RETRIEVED;
import static it.unipi.mircv.utility.Parameters.flagStemming;
import static it.unipi.mircv.utility.Parameters.flagStopWordRemoval;
import static it.unipi.mircv.utility.Utils.*;

public class SystemEvaluator {
    //even if it is not used do not delete it
    public static void evaluateSystemTime(String tsvFile, QueryProcessor queryProcessor) throws IOException {
    /*---------------------------------------------------------
        method that evalues the time of the query processing
    ---------------------------------------------------------*/

        //load queries from file
        ArrayList<Query> queries = new ArrayList<>();
        loadQueriesFromFile(tsvFile, queries);

        //evaluate the time of the query processing foreach query
        ArrayList<Long> resultsTimes = new ArrayList<>(queries.size());
        for (Query query : queries) {
            resultsTimes.add(testQueryTime(query.getQueryText(), queryProcessor));
        }
        //print the results
        System.out.println("results times: " + resultsTimes);
        //compute and print the mean
        System.out.println("mean: " + computeMean(resultsTimes));
    }

    //Note: even if it is not used do not delete it
    public static void createFileQueryResults(String fileName, String queryFile, QueryProcessor queryProcessor) throws IOException {
    /*---------------------------------------------------------
        method that creates a file
        with the results of the queries
    ---------------------------------------------------------*/

        StringBuilder stringToWrite;
        String fixed = "Q0";
        String runId = "0";
        float docScore = 0f;
        ArrayList<Query> queries = new ArrayList<>();
        loadQueriesFromFile(queryFile, queries);
        deleteFile(fileName);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        for (Query query : queries) {
            String[] queryResults = queryResult(query.getQueryText(), queryProcessor);
            for(int i = 0; i < queryResults.length; i++){
                //this line must be here, otherwise the buffer size could not be enough
                stringToWrite = new StringBuilder();
                stringToWrite.append(query.getQueryId()).append(" ").append(fixed).append(" ");    //queryId fixed
                stringToWrite.append(queryResults[i]).append(" ");                                 //docNo rank
                stringToWrite.append(i + 1).append(" ");                                           //rank
                stringToWrite.append(docScore).append(" ").append(runId).append("\n");             //score runId
                writer.write(stringToWrite.toString());
            }
            writer.flush();
        }
    }

    public static void loadQueriesFromFile(String tsvFile, ArrayList<Query> queries)  throws IOException{
    /*---------------------------------------------------------
        method that loads the queries from file
    ---------------------------------------------------------*/
        String line;
        BufferedReader br = new BufferedReader(new FileReader(tsvFile));

        //read the file line by line
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\t");
            int queryId = Integer.parseInt(data[0]);
            String queryText = data[1];
            queries.add(new Query(queryId, queryText));
        }
    }

    public static String[] queryResult(String query, QueryProcessor queryProcessor) throws IOException {
    /*---------------------------------------------------------
        method that returns the results of a query
    ---------------------------------------------------------*/
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        String[] queryTerms = Utils.tokenization(query);

        if (flagStopWordRemoval) queryTerms = removeStopWords(queryTerms);
        if (flagStemming) stemPhrase(queryTerms);

        System.out.println("final query: " + Arrays.toString(queryTerms)); //DEBUG

        ArrayList<Integer> results = new ArrayList<>(MAX_NUM_DOC_RETRIEVED);

        switch (queryProcessor) {
            case DISJUNCTIVE_DAAT -> {
                results = new DisjunctiveDAAT(queryTerms).processQuery();
            }
            case CONJUNCTIVE_DAAT -> {
                results = new ConjunctiveDAAT(queryTerms).processQuery();
            }
            case CONJUNCTIVE_DAAT_NO_SKIPPING -> {
                results = new ConjunctiveDAAT(queryTerms).processQueryWithoutSkipping();
            }
            case DISJUNCTIVE_MAX_SCORE -> {
                results = new MaxScoreDisjunctive(queryTerms).computeMaxScore();
            }
            case DISJUNCTIVE_DAAT_C -> {
                results = new DisjunctiveDAATCompression(queryTerms).processQuery();
            }
            case CONJUNCTIVE_DAAT_C -> {
                results = new ConjunctiveDAATCompression(queryTerms).processQuery();
            }
            case DISJUNCTIVE_MAX_SCORE_C -> {
                results = new MaxScoreDisjunctiveCompression(queryTerms).computeMaxScore();
            }
        }

        String[] docNos = documentIndexHandler.getDocNoREVERSE(results);
        documentIndexHandler.closeFileChannel();
        return docNos;
    }

    public static long testQueryTime(String query, QueryProcessor queryProcessor) throws IOException {
    /*---------------------------------------------------------
        method that returns the time of
        the query processing for a query
    ---------------------------------------------------------*/

        //load the document index
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        long startTime = System.currentTimeMillis();
        //tokenize the query
        String[] queryTerms = Utils.tokenization(query);
        if (flagStopWordRemoval) queryTerms = removeStopWords(queryTerms);
        if (flagStemming) stemPhrase(queryTerms);

        ArrayList<Integer> results = new ArrayList<>(MAX_NUM_DOC_RETRIEVED);

        //process the query based on the query processor chosen
        switch (queryProcessor) {
            case DISJUNCTIVE_DAAT -> {
                results = new DisjunctiveDAAT(queryTerms).processQuery();
            }
            case CONJUNCTIVE_DAAT -> {
                results = new ConjunctiveDAAT(queryTerms).processQuery();
            }
            case CONJUNCTIVE_DAAT_NO_SKIPPING -> {
                results = new ConjunctiveDAAT(queryTerms).processQueryWithoutSkipping();
            }
            case DISJUNCTIVE_MAX_SCORE -> {
                results = new MaxScoreDisjunctive(queryTerms).computeMaxScore();
            }
            case DISJUNCTIVE_DAAT_C -> {
                results = new DisjunctiveDAATCompression(queryTerms).processQuery();
            }
            case CONJUNCTIVE_DAAT_C -> {
                results = new ConjunctiveDAATCompression(queryTerms).processQuery();
            }
            case DISJUNCTIVE_MAX_SCORE_C -> {
                results = new MaxScoreDisjunctiveCompression(queryTerms).computeMaxScore();
            }
        }

        long endTime =  System.currentTimeMillis() - startTime;
        System.out.println(Arrays.toString(documentIndexHandler.getDocNoREVERSE(results)));
        return endTime;
    }

    public static HashMap<Float, ArrayList<Integer>> queryResultForTest(String query, QueryProcessor queryProcessor) throws IOException {
    /*---------------------------------------------------------
        method that returns the results of a query for the test
    ---------------------------------------------------------*/

        String[] queryTerms = Utils.tokenization(query);

        if (flagStopWordRemoval) queryTerms = removeStopWords(queryTerms);
        if (flagStemming) stemPhrase(queryTerms);

        System.out.println("final query: " + Arrays.toString(queryTerms)); //DEBUG

        MinHeapScores heapScores = new MinHeapScores();

        switch (queryProcessor) {
            case DISJUNCTIVE_DAAT -> {
                DisjunctiveDAAT disjunctiveDAAT = new DisjunctiveDAAT(queryTerms);
                disjunctiveDAAT.processQuery();
                heapScores = disjunctiveDAAT.getHeapScores();
            }
            case CONJUNCTIVE_DAAT -> {
                ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
                conjunctiveDAAT.processQuery();
                heapScores = conjunctiveDAAT.getHeapScores();
            }
            case CONJUNCTIVE_DAAT_NO_SKIPPING -> {
                ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
                conjunctiveDAAT.processQueryWithoutSkipping();
                heapScores = conjunctiveDAAT.getHeapScores();
            }
            case DISJUNCTIVE_MAX_SCORE -> {
                MaxScoreDisjunctive maxScoreDisjunctive = new MaxScoreDisjunctive(queryTerms);
                maxScoreDisjunctive.computeMaxScore();
                heapScores = maxScoreDisjunctive.getHeapScores();
            }
            case DISJUNCTIVE_DAAT_C -> {
                DisjunctiveDAATCompression disjunctiveDAATCompression = new DisjunctiveDAATCompression(queryTerms);
                disjunctiveDAATCompression.processQuery();
                heapScores = disjunctiveDAATCompression.getHeapScores();
            }
            case CONJUNCTIVE_DAAT_C -> {
                ConjunctiveDAATCompression conjunctiveDAATCompression = new ConjunctiveDAATCompression(queryTerms);
                conjunctiveDAATCompression.processQuery();
                heapScores = conjunctiveDAATCompression.getHeapScores();
            }
            case DISJUNCTIVE_MAX_SCORE_C -> {
                MaxScoreDisjunctiveCompression maxScoreDisjunctiveCompression =
                        new MaxScoreDisjunctiveCompression(queryTerms);
                maxScoreDisjunctiveCompression.computeMaxScore();
                heapScores = maxScoreDisjunctiveCompression.getHeapScores();
            }
        }
        return heapScores.getScore2DocIdMap();
    }

    public static double computeMean(ArrayList<Long> list) {
    /*---------------------------------------------------------
        method that computes the mean of a list of longs
    ---------------------------------------------------------*/

        long sum = 0L;
        for (long num : list) {
            sum += num;
        }
        return (double) sum / list.size();
    }
}
