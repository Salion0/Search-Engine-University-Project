package it.unipi.mircv;


import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.query.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Utils.removeStopWords;
import static java.util.Collections.binarySearch;

public class TestLorenzo {
    public static void main(String[] args) throws IOException {
        flagCompressedReading = false;
        flagStemming = false;
        flagStopWordRemoval = false;

        //testCompressedReading();
        String forLexiconTest = "";
        //checkLexiconEntry(forLexiconTest);

        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        //TODO da fare più veloce perchè così ci vuole una vita e poi da mettere in Documenet Index
        Config.docsLen = new int[Config.collectionSize];
        for (int i = 0; i < Config.collectionSize; i++){
            Config.docsLen[i] = documentIndexHandler.readDocumentLength(i);
        }
        //String test = "\0\0\0\0\0pfdvefvegr";
        //if (test.startsWith("\0\0\0\0"))
          //  System.out.println("deh");

        System.out.println("-----------------------------------------------------------");

        LRUCache<Integer, Integer> docLenCache = new LRUCache<>(CACHE_SIZE);
        //docLenCache.put(1,50);
        //System.out.println(docLenCache.get(1));

        String forConjunctiveTest = "", forDisjunctiveTest = "";

        //testNewDisjunctive("");
        //testOldDisjunctive("");
        //testNoPriorityQueueDisjunctive("");
        //testMaxScoreDisjunctive("");
        //testConjunctive("diet detox");
        //testMaxScore("diet detox");
        for (int i = 0; i < 2; i++) {
            //testConjunctive("100 10");
            //testConjunctiveCache("100 10 diet", docLenCache);
        }
        System.out.println("Test Conjuctive");
        testNewConjunctive("manhattan project");
        checkLexiconEntry("manhattan");
        checkLexiconEntry("project");
        //testMaxScore("diet 100");
        //testOldDisjunctive("diet 100");
        //System.out.println(SystemEvaluator.testQueryTime("diet 100", CONJUNCTIVE, BM25,true, false ));
        //testConjunctiveCache("diet 100",docLenCache);

        System.out.println("***************************************************************************************************");

        //testMaxScore("diet 100");
    }

    public static void testNewConjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        ConjunctiveDAAT newConjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = newConjunctiveDAAT.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("NEW-CONJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testConjunctiveCache(String string,LRUCache lruCache) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        ConjunctiveDAATCache conjunctiveDAATCache = new ConjunctiveDAATCache(queryTerms,lruCache);
        ArrayList<Integer> results = conjunctiveDAATCache.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("CONJUNCTIVE-CACHE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testConjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = conjunctiveDAAT.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("CONJUNCTIVE-WITHOUT-CACHE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testMaxScore(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        MaxScore maxScore2 = new MaxScore(queryTerms);
        ArrayList<Integer> results2 = maxScore2.computeMaxScore();
        System.out.println(results2);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("MAX-SCORE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testMaxScoreDisjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        MaxScoreDisjunctive maxScore = new MaxScoreDisjunctive(queryTerms);
        ArrayList<Integer> results = maxScore.computeMaxScore();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("MAX-SCORE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testOldDisjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        DisjunctiveDAAT oldDisjunctive = new DisjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = oldDisjunctive.processQuery();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println(results);
        System.out.println("OLD-DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testNewDisjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        //PriorityQueueDisjunctiveDAAT disjunctiveDAAT = new PriorityQueueDisjunctiveDAAT(queryTerms);
        //ArrayList<Integer> results = disjunctiveDAAT.processQuery();
        //System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("NEW-DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testNoPriorityQueueDisjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        DisjunctiveDAAT noPriorityQueueDisjunctive = new DisjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = noPriorityQueueDisjunctive.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("NO-PRIORITY-QUEUE-DISJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
    }

    public static void testCompressedReading() throws IOException {
        Index index = new Index("","",false);
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger();
        blockMerger.mergeBlocks(numberOfBlocks);
    }

    public static void checkLexiconEntry(String string) throws IOException {
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        InvertedIndexFileHandler invertedIndexHandler = new InvertedIndexFileHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        float termUpperBoundScore = lexiconHandler.getTermUpperBoundScore(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        System.out.println("term = " + term);
        System.out.println("postingList = " + postingListBlock);
        System.out.println("offset = " + offset);
        System.out.println("documentFrequency = " + documentFrequency);
        System.out.println("termUpperBoundScore = " + termUpperBoundScore);
    }
}


