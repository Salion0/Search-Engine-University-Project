package it.unipi.mircv;
import it.unipi.mircv.indexing.Index;


public class App
{
    public static void main( String[] args )
    {
        String filePath = "test_collection.tsv";
        Index.createInvertedIndex(filePath);
        //prova.FindURLsExample("")
        //prova.tokenization();


    }
}
