package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.DocumentIndex;
import it.unipi.mircv.index.Index;

import java.io.IOException;

import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.*;
import static it.unipi.mircv.Utils.loadStopWordList;
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
        scoreType = TFIDF;
        queryProcessType = DISJUNCTIVE_MAX_SCORE;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        lexiconFileHandler.findTermEntryCompression("");

        //QUERY TIME ----------------------
        startTime = currentTimeMillis();
        SystemEvaluator.queryResult("", queryProcessType);
        System.out.println("time: " + (currentTimeMillis() - startTime));

        SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", queryProcessType);
        //SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT_C);

        //CREATING FILE ----------------------
        /*
        SystemEvaluator.createFileQueryResults("queryResult/disjunctiveMaxStemming.txt",
                "query/msmarco-test2020-queries.tsv", DISJUNCTIVE_MAX_SCORE_C);
        SystemEvaluator.createFileQueryResults("queryResult/disjunctiveStemming.txt",
                "query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT);

         */
    }
}
