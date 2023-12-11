package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.file.SkipDescriptorFileHandler;
import it.unipi.mircv.index.*;
import it.unipi.mircv.query.*;
import it.unipi.mircv.evaluation.SystemEvaluator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Config.QueryProcessor.*;
import static it.unipi.mircv.Config.Score.BM25;

public class TestCompression {
    public static void main(String[] args) throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;

        /*
        long startTime = System.currentTimeMillis();
        Index index = new Index("collection.tsv");
        BlockMergerCompression blockMergerCompression = new BlockMergerCompression();
        blockMergerCompression.mergeBlocks(index.getNumberOfBlocks());
        System.out.println(System.currentTimeMillis() - startTime);
        */



        //--------------------CARICO LE DOC LEN--------------------------------------------------------
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        //TestLorenzo.checkLexiconEntry("diet");

        //TODO da fare più veloce perchè così ci vuole una vita e poi da mettere in Documenet Index
        Config.docsLen = new int[Config.collectionSize];
        for (int i = 0; i < Config.collectionSize; i++){
            Config.docsLen[i] = documentIndexHandler.readDocumentLength(i);
        }
        //----------------------------------------------------------------------------------------------
        System.out.println("------------query------------------------");
        String[] query = new String[]{"railroad", "workers"};
        MaxScoreDisjunctiveCompression queryProcessor = new MaxScoreDisjunctiveCompression(query);
        ArrayList<Integer> result = queryProcessor.computeMaxScore();

        for (String s: documentIndexHandler.getDocNoREVERSE(result)) {
            System.out.println(s);
        }

        /*
        SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT_C, BM25, true, false);
        SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT_C, BM25, true, false);

        SystemEvaluator.createFileQueryResults("queryResult/disjunctive_c.txt","query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT_C, BM25,true, false);
        SystemEvaluator.createFileQueryResults("queryResult/conjunctive_c.txt","query/msmarco-test2020-queries.tsv", CONJUNCTIVE_DAAT_C, BM25,true, false);


         */
    }
}
