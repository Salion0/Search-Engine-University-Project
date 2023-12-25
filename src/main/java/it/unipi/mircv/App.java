package it.unipi.mircv;
import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;

import java.io.IOException;

import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.*;
import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Utils.printFilePaths;
import static it.unipi.mircv.Utils.setFilePaths;

public class App
{
    public static void main( String[] args ) throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();

        Index index = new Index(INDEX_PATH + '/',"test_collection.tsv",false);

        //BlockMergerCompression blockMerger = new BlockMergerCompression();
        //blockMerger.mergeBlocks(index.getNumberOfBlocks());
        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());


        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_DAAT);
    }
}


