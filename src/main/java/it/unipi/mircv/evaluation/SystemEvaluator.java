package it.unipi.mircv.evaluation;
import it.unipi.mircv.Config.Score;
import it.unipi.mircv.Config.QueryProcessor;
import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Query.*;
import it.unipi.mircv.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static it.unipi.mircv.Config.MAX_NUM_DOC_RETRIEVED;
import static it.unipi.mircv.Utils.removeStopWords;
import static it.unipi.mircv.Utils.stemPhrase;

public class SystemEvaluator {

    public static ArrayList<Long> evaluateSystemTime(String tsvFile, QueryProcessor queryProcessor, Score score, boolean stopWordRemoval, boolean stemming) throws IOException {

        ArrayList<Query> queries = new ArrayList<>();
        loadQueriesFromFile(tsvFile, queries);

        ArrayList<Long> resultsTimes = new ArrayList<>(queries.size());

        for (Query query : queries) {
            resultsTimes.add(testQueryTime(query.getQueryText(), queryProcessor, score, stopWordRemoval, stemming));
        }

        System.out.println("results times: " + resultsTimes);
        System.out.println("mean: " + computeMean(resultsTimes));
        return resultsTimes;
    }

    public static void createQueryResultsFile(String fileName){

    }

    public static void loadQueriesFromFile(String tsvFile, ArrayList<Query> queries)  throws IOException{
        String line;
        BufferedReader br = new BufferedReader(new FileReader(tsvFile));
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\t");
            int queryId = Integer.parseInt(data[0]);
            String queryText = data[1];
            queries.add(new Query(queryId, queryText));
        }
    }

    public static long testQueryTime(String query, QueryProcessor queryProcessor, Score score, boolean stopWordRemoval, boolean stemming) throws IOException {
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        long startTime = System.currentTimeMillis();
        String[] queryTerms = Utils.tokenization(query);

        if (stopWordRemoval) queryTerms = removeStopWords(queryTerms);
        if (stemming) stemPhrase(queryTerms);

        System.out.println("final query:" + Arrays.toString(queryTerms)); //DEBUG

        ArrayList<Integer> results = new ArrayList<>(MAX_NUM_DOC_RETRIEVED);

        switch (queryProcessor) {
            case DISJUNCTIVE -> {
                results = new DisjunctiveDAAT(queryTerms).processQuery();
            }
            case CONJUNCTIVE -> {
                results = new ConjunctiveDAAT(queryTerms).processQuery();
            }
            case DISJUNCTIVE_MAX_SCORE -> {
                results = new MaxScoreDisjunctive(queryTerms).computeMaxScore();
            }
            case CONJUNCTIVE_MAX_SCORE -> {
                results = new MaxScore(queryTerms).computeMaxScore();
            }
        }

        System.out.println(Arrays.toString(documentIndexHandler.getDocNoREVERSE(results)));
        return System.currentTimeMillis() - startTime;
    }

    public static double computeMean(ArrayList<Long> list) {
        long sum = 0L;
        for (long num : list) {
            sum += num;
        }
        return (double) sum / list.size();
    }
}
