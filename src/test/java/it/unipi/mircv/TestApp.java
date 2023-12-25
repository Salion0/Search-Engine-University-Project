package it.unipi.mircv;

import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.Index;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static it.unipi.mircv.Config.INDEX_PATH;
import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.DISJUNCTIVE_DAAT;
import static it.unipi.mircv.Parameters.QueryProcessor.DISJUNCTIVE_DAAT_C;
import static it.unipi.mircv.Parameters.Score.TFIDF;
import static it.unipi.mircv.Utils.printFilePaths;
import static it.unipi.mircv.Utils.setFilePaths;

public class TestApp {

    @Test
    public static void main(String[] args) throws IOException {
        // compute the index on which the score functions are going to be tested
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForQueryTest";
        Utils.loadStopWordList();
        setFilePaths();
        printFilePaths();

        Index index = new Index(INDEX_PATH + '/',"test_collection.tsv",false);

        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());


        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("Manhattan", DISJUNCTIVE_DAAT);

        buildCompressedIndex();
    }

    private static void buildCompressedIndex() throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForQueryTestCompressed";
        setFilePaths();
        printFilePaths();

        Index index = new Index(INDEX_PATH + '/',"test_collection.tsv",false);

        BlockMergerCompression blockMergerCompression = new BlockMergerCompression();
        blockMergerCompression.mergeBlocks(index.getNumberOfBlocks());


        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("Manhattan", DISJUNCTIVE_DAAT_C);
    }

    @Test
    void buildIndexForTest() throws IOException {
        // compute the index on which the score functions are going to be tested
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForScoreTest";
        setFilePaths();
        printFilePaths();

        Index index = new Index(INDEX_PATH + '/',"test_collection_for_query.tsv",false);

        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());


        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("river", DISJUNCTIVE_DAAT);
    }
}
