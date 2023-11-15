package it.unipi.mircv;
import it.unipi.mircv.Index.BlockMerger;
import it.unipi.mircv.Index.Index;

import java.io.*;


public class App
{

    public static void main( String[] args ) throws IOException {
        Index index = new Index("test_collection.tsv");
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger(numberOfBlocks);
        blockMerger.mergeBlocks();
        // TODO compute term upper bounds scores
    }

}


