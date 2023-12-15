package it.unipi.mircv;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;

import static it.unipi.mircv.Config.*;

public class App
{
    public static void main( String[] args )  {
        try{
            //DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
            //Config.collectionSize = documentIndexFileHandler.readCollectionSize();

            STARTING_PATH = "dataForQueryTest";
            flagStemming=false;
            flagStopWordRemoval=true;
            flagCompressedReading=false;

            Index index = new Index(STARTING_PATH + '/',"test_collection.tsv",false);

            BlockMerger blockMerger = new BlockMerger();
            blockMerger.mergeBlocks(index.getNumberOfBlocks());


            } catch(Exception e){
                e.printStackTrace();
            }


    }
}


