package it.unipi.mircv.evaluation;
import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Index.Index;
import it.unipi.mircv.Query.ConjunctiveDAAT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.TestMatteo.removeStopWords;

public class SystemEvaluator {
    public static ArrayList<Long> evaluateSystem(String tsvFile, boolean stopWordRemoval, boolean stemming) throws IOException {
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        ArrayList<Query> queries = new ArrayList<>();
        loadQueries(tsvFile, queries, stopWordRemoval, stemming);

        ArrayList<Long> resultsTimes = new ArrayList<>(queries.size());

        long startTime;
        for (Query query : queries) {
            startTime = System.currentTimeMillis();
            System.out.println(query.getQueryText());
            String[] queryTerms = removeStopWords(Index.tokenization(query.getQueryText()));

            ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
            documentIndexHandler.getDocNoREVERSE(conjunctiveDAAT.processQuery());

            resultsTimes.add(System.currentTimeMillis() - startTime);
        }

        System.out.println(resultsTimes);
        return resultsTimes;
    }

    public static void createQueryResult(String fileName){

    }

    //TODO aggiungere stemming e stopWord
    public static void loadQueries(String tsvFile, ArrayList<Query> queries, boolean stopWordRemoval, boolean stemming)  throws IOException{
        String line;
        BufferedReader br = new BufferedReader(new FileReader(tsvFile));
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\t");
            int queryId = Integer.parseInt(data[0]);
            String queryText = data[1];
            queries.add(new Query(queryId, queryText));
        }
    }
}
