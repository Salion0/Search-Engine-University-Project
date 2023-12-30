package it.unipi.mircv.toDelete;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.LexiconFileHandler;

import java.io.IOException;

import static it.unipi.mircv.utility.Parameters.*;
import static it.unipi.mircv.utility.Parameters.QueryProcessor.*;
import static it.unipi.mircv.utility.Parameters.Score.*;
import static it.unipi.mircv.utility.Utils.loadStopWordList;
import static java.lang.System.currentTimeMillis;

public class TestMatteo {
    public static void main(String[] args) throws IOException {

        flagStemming = true;
        flagStopWordRemoval = true;
        flagCompressedReading = true;
        long startTime;
        /*
        startTime = currentTimeMillis();
        Index index = new Index("data/","collection.tar",false);
        BlockMergerCompression blockMerger = new BlockMergerCompression();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());
        System.out.println("time: " + (currentTimeMillis() - startTime));

         */

        //QUERY ------
        loadStopWordList();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = BM25;
        queryProcessType = CONJUNCTIVE_DAAT_C;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        lexiconFileHandler.findTermEntryCompression("");

        //QUERY TIME ----------------------
        startTime = currentTimeMillis();
        System.out.println("time: " + (currentTimeMillis() - startTime));

        SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", queryProcessType);
        SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", queryProcessType);
        SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", queryProcessType);

        //CREATING FILE ----------------------
        /*
        SystemEvaluator.createFileQueryResults("queryResult/disjunctiveMaxStemming.txt",
                "query/msmarco-test2020-queries.tsv", DISJUNCTIVE_MAX_SCORE_C);
        SystemEvaluator.createFileQueryResults("queryResult/disjunctiveStemming.txt",
                "query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT);

         */
    }
}
