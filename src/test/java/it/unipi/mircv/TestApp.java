package it.unipi.mircv;

import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.Index;
import it.unipi.mircv.query.DisjunctiveDAAT;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.TFIDF;
import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Utils.*;

public class TestApp {

    public static void main(String[] args) throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        STARTING_PATH = "dataForQueryTest";  // set the name of the folder where the index is going to be computed
        setFilePaths(); // set the paths of the components of the index accordingly
        Utils.loadStopWordList();
        printFilePaths();

        //Index index = new Index(STARTING_PATH + '/',"test_collection.tsv",false);

        //BlockMergerCompression blockMerger = new BlockMergerCompression();
        //blockMerger.mergeBlocks(index.getNumberOfBlocks());

        Index index = new Index(STARTING_PATH + '/',"test_collection.tsv",false);

        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());


        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_MAX_SCORE);

        buildCompressedIndex();
    }

    private static void buildCompressedIndex() throws IOException {     // build the index with compression
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        STARTING_PATH = "dataForQueryTestCompressed";
        setFilePaths();
        printFilePaths();

        Index index = new Index(STARTING_PATH + '/',"test_collection.tsv",false);

        BlockMergerCompression blockMerger = new BlockMergerCompression();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());


        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_MAX_SCORE_C);
    }

}
