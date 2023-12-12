package it.unipi.mircv;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.Index;
import static it.unipi.mircv.Parameters.*;

public class App
{
    public static void main( String[] args )  {
        try{
            DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
            collectionSize = documentIndexFileHandler.readCollectionSize();


            flagStemming=false;
            flagStopWordRemoval=true;
            flagCompressedReading=false;

            Index index = new Index("data/","test_collection.tsv",false);

            BlockMerger blockMerger = new BlockMerger();
            blockMerger.mergeBlocks(index.getNumberOfBlocks());


            } catch(Exception e){
                e.printStackTrace();
            }


    }
}


