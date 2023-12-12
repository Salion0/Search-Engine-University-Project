package it.unipi.mircv;
import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.Index;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.*;

public class App
{
    public static void main( String[] args )  {
        try{
            flagStemming = false;
            flagStopWordRemoval = true;
            flagCompressedReading = true;

            Index index = new Index("data/","test_collection.tsv",false);

            BlockMergerCompression blockMerger = new BlockMergerCompression();
            blockMerger.mergeBlocks(index.getNumberOfBlocks());

            //QUERY ------
            DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
            collectionSize = documentIndexFileHandler.readCollectionSize();
            avgDocLen = documentIndexFileHandler.readAvgDocLen();
            scoreType = TFIDF;
            docsLen = documentIndexFileHandler.loadAllDocumentLengths();

            SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_MAX_SCORE_C);

            } catch(Exception e){
                e.printStackTrace();
            }
    }
}


