package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Query.QueryProcessor;

import java.io.IOException;
import java.util.ArrayList;

public class TestLorenzo {
    public static void main(String[] args) throws IOException {
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();

        // ---------------------TEST DAAT-----------------------------
        String query = "impressive achievement";
        QueryProcessor queryProcessor = new QueryProcessor(query);
        System.out.println("**************** DAAT ******************");
        ArrayList<Integer> docId = queryProcessor.DAAT();
        System.out.println("Doc Id retrieved: ");
        System.out.println(docId);
        System.out.println("**************** TAAT ******************");
        queryProcessor = new QueryProcessor(query);
        //queryProcessor.TAAT();
    }

    /*
        //Test per leggere senza unzippare
        String tarFilePath = "collection.tar.gz";

        try {
            FileInputStream fis = new FileInputStream(tarFilePath);
            GZIPInputStream gzis = new GZIPInputStream(fis);
            InputStreamReader reader = new InputStreamReader(gzis);
            BufferedReader br = new BufferedReader(reader);

            String line;
            br.readLine(); // la prima riga contiene metadati quindi la salto
            int count = 0;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                count++;
                if (count == 5) break; //DEBUG
            }

            br.close();
            reader.close();
            gzis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

     */
}
