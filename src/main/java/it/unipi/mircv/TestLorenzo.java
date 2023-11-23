package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.SkipDescriptor;
import it.unipi.mircv.Query.ConjunctiveDAAT;
import it.unipi.mircv.Query.MaxScore;
import it.unipi.mircv.Query.QueryProcessor;

import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.TestMatteo.removeStopWords;

public class TestLorenzo {
    public static void main(String[] args) throws IOException {
        /*DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();

        // ---------------------TEST DAAT-----------------------------
        String query = "railroad workers";
        QueryProcessor queryProcessor = new QueryProcessor(query);
        System.out.println("**************** DAAT ******************");
        ArrayList<Integer> docId = queryProcessor.conjunctiveDAAT();
        System.out.println("Doc Id retrieved: ");
        System.out.println(docId);
        System.out.println("**************** TAAT ******************");
        queryProcessor = new QueryProcessor(query);
        //queryProcessor.TAAT();
    }
    */

    /*
        Index index = new Index("test_collection.tsv");
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger(numberOfBlocks);
        blockMerger.mergeBlocks();


        LexiconHandler lexiconHandler = new LexiconHandler();
        */
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        SkipDescriptor skipDescriptor = skipDescriptorFileHandler.readSkipDescriptor(57, 1);
        System.out.println(skipDescriptor);

        // Testing DAAT
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        Config.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        //String[] queryTerms= TokenProcessing.doStopWordRemovalAndStemming(stemmer, "holy spirit".split(" "));
        System.out.println("-----------------------------------------------------------");

        long startTime = System.currentTimeMillis();
        String[] queryTerms = "10 100".split(" ");
        queryTerms = removeStopWords(queryTerms);
        System.out.println(queryTerms.length);
        ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        MaxScore maxScore = new MaxScore(queryTerms);
        ArrayList<Integer> results = maxScore.computeMaxScore();
        System.out.println(results);

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
}
