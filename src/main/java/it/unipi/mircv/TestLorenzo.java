package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.BlockMerger;
import it.unipi.mircv.Index.Index;
import it.unipi.mircv.Index.SkipDescriptor;
import it.unipi.mircv.Query.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Config.LEXICON_ENTRY_LENGTH;
import static java.util.Collections.binarySearch;

public class TestLorenzo {
    public static void main(String[] args) throws IOException {
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

        //testNewDisjunctive();
        //testOldDisjunctive();
        //testNoPriorityQueueDisjunctive();

        System.out.println("***************************************************************************************************");
    }

    public void testConjunctive() throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = "10 100".split(" ");
        queryTerms = removeStopWords(queryTerms);
        ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = conjunctiveDAAT.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("CONJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public void testMaxScore() throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = "10 100".split(" ");
        queryTerms = removeStopWords(queryTerms);
        MaxScore maxScore2 = new MaxScore(queryTerms);
        ArrayList<Integer> results2 = maxScore2.computeMaxScore();
        System.out.println(results2);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("MAX-SCORE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testOldDisjunctive() throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = "diet madonna".split(" ");
        queryTerms = removeStopWords(queryTerms);
        oldDisjunctive oldDisjunctive = new oldDisjunctive(queryTerms);
        ArrayList<Integer> results = oldDisjunctive.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("OLD-DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testNewDisjunctive() throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = "diet madonna".split(" ");
        queryTerms = removeStopWords(queryTerms);
        DisjunctiveDAAT disjunctiveDAAT = new DisjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = disjunctiveDAAT.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("NEW-DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testNoPriorityQueueDisjunctive() throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = "diet madonna".split(" ");
        queryTerms = removeStopWords(queryTerms);
        noPriorityQueueDisjunctive noPriorityQueueDisjunctive = new noPriorityQueueDisjunctive(queryTerms);
        ArrayList<Integer> results = noPriorityQueueDisjunctive.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("NO-PRIORITY-QUEUE-DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
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

    public void testCompressedReading() {
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

    }
}


