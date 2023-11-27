package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.BlockMerger;
import it.unipi.mircv.Index.Index;
import it.unipi.mircv.Index.SkipDescriptor;
import it.unipi.mircv.Query.ConjunctiveDAAT;
import it.unipi.mircv.Query.MaxScore;
import it.unipi.mircv.Query.QueryProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Config.LEXICON_ENTRY_LENGTH;
import static java.util.Collections.binarySearch;

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
        Index index = new Index("collection.tsv");
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger(numberOfBlocks);
        blockMerger.mergeBlocks();
        */

        // Testing DAAT
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        Config.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        System.out.println("-----------------------------------------------------------");

        long startTime = System.currentTimeMillis();
        String[] queryTerms = "railroad workers 10".split(" ");
        queryTerms = removeStopWords(queryTerms);
        MaxScore maxScore = new MaxScore(queryTerms);
        ArrayList<Integer> results = maxScore.computeMaxScore();
        //ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        //ArrayList<Integer> results = conjunctiveDAAT.processQuery();

        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("indexing finished in " + (float)elapsedTime/1000 +"sec");
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

    public static String[] removeStopWords(String[] queryTerms) throws IOException {
        ArrayList<String> filteredTerms = new ArrayList<>();
        for (String term : queryTerms)
            if (!seekInStopwords(term)) //if (binarySearch(stopWords,term) == -1) //if (!stopWords.contains(term))
                filteredTerms.add(term);

        return filteredTerms.toArray(new String[0]);
    }

    public static boolean seekInStopwords(String term) throws IOException {

        int l = 0, r = stopWords.size() - 1;

        while (l <= r)
        {
            int m = l + (r - l) / 2;
            int res = term.compareTo(stopWords.get(m));
            if (res == 0)
                return true;
            if (res > 0)
                l = m + 1;
            else
                r = m - 1;
        }

        return false;
    }

}
