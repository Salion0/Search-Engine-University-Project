package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Query.QueryProcessor;

import java.io.IOException;
import java.util.ArrayList;

public class TestLorenzo {
    public static void main(String[] args) throws IOException {
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        System.out.println(documentIndexHandler.readAvgDocLen());


        // ---------------------TEST DAAT-----------------------------
        String query = "holy spirit";
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<Integer> docId = queryProcessor.conjunctiveDAAT();
        System.out.println("Doc Id retrieved: ");
        System.out.println(docId);
    }
}
