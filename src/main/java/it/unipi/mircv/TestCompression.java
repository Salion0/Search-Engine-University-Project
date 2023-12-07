package it.unipi.mircv;

import it.unipi.mircv.Index.BlockMerger;
import it.unipi.mircv.Index.Index;

import java.io.IOException;

import static it.unipi.mircv.Config.*;

public class TestCompression {
    public static void main(String[] args) throws IOException {
        flagStemming=false;
        flagStopWordRemoval=true;
        flagCompressedReading=false;
        Index index = new Index("test_collection.tsv");
        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());
    }
}
