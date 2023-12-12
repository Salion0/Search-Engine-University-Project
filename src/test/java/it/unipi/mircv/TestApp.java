package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;

import java.io.IOException;

public class TestApp {

    public static void main(String[] args) throws IOException {

        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Config.collectionSize = documentIndexFileHandler.readCollectionSize();

        Config.flagStemming=false;
        Config.flagStopWordRemoval=true;
        Config.flagCompressedReading=false;

        Index index = new Index("data/","test_collection.tsv",false);

        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());
    }

}
