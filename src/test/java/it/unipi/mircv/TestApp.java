package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;

import java.io.IOException;

import static it.unipi.mircv.Config.*;

public class TestApp {

    public static void main(String[] args) throws IOException {

        STARTING_PATH = "dataForQueryTest";
        Config.flagStemming=false;
        Config.flagStopWordRemoval=true;
        Config.flagCompressedReading=false;

        Index index = new Index(STARTING_PATH + '/',"test_collection.tsv",false);
        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());

    }

}
