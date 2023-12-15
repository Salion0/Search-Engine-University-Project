package it.unipi.mircv;

import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Parameters.QueryProcessor.DISJUNCTIVE_MAX_SCORE;
import static it.unipi.mircv.Parameters.QueryProcessor.DISJUNCTIVE_MAX_SCORE_C;
import static it.unipi.mircv.Parameters.Score.TFIDF;
import static it.unipi.mircv.Utils.cleanFolder;
import static it.unipi.mircv.Utils.setFilePaths;
import static it.unipi.mircv.Parameters.*;

public class TestApp {

    public static void main(String[] args) throws IOException {
        flagCompressedReading = false;
        flagStopWordRemoval = true;
        flagStemming = false;
        STARTING_PATH = "dataForQueryTest";
        setFilePaths();
        Index index = new Index(STARTING_PATH + '/',"test_collection.tsv",false);
        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());

        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
        for (int i = 0; i < docsLen[i]; i++)
            System.out.println(docsLen[i]);

        //SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_MAX_SCORE);

    }

    @Test
    void test() throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        Utils.loadStopWordList();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();
        SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_MAX_SCORE_C);
        SystemEvaluator.queryResult("10 100", DISJUNCTIVE_MAX_SCORE_C);
    }

}
