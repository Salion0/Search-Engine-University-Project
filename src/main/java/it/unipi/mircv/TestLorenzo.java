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

import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Utils.removeStopWords;
import static java.util.Collections.binarySearch;

public class TestLorenzo {
    public static void main(String[] args) throws IOException {
        flagCompressedReading = false;
        flagStemming = false;
        flagStopWordRemoval = true;

        //testCompressedReading();
        String forLexiconTest = "";
        //checkLexiconEntry(forLexiconTest);

        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        collectionSize = documentIndexHandler.readCollectionSize();
        avgDocLen = documentIndexHandler.readAvgDocLen();

        //String test = "\0\0\0\0\0pfdvefvegr";
        //if (test.startsWith("\0\0\0\0"))
          //  System.out.println("deh");

        System.out.println("-----------------------------------------------------------");

        //docLenCache.put(1,50);
        //System.out.println(docLenCache.get(1));


        //testNewDisjunctive("10 100");
        //testOldDisjunctive("");
        //testNoPriorityQueueDisjunctive("10 100");
        testMaxScoreDisjunctive("10 100");
        //testConjunctive("diet detox");
        //testMaxScore("diet detox");


        //testNewConjunctive("railroad workers");
        //testNewConjunctive("10 100 1000");
        //testNoPriorityQueueDisjunctive("diet 100");

        //InvertedIndexHandler invertedIndexHandler = new InvertedIndexHandler();
        //System.out.println(invertedIndexHandler.getPostingList(0,20));
        //checkLexiconEntry("railroad");
        //checkLexiconEntry("workers");
        //checkLexiconEntry("dziena");

        //testNoPriorityQueueDisjunctive("what is the distance between flat rock michigan and detroit");

        System.out.println("***************************************************************************************************");

        //testMaxScoreDisjunctive("what is the distance between flat rock michigan and detroit");
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

    public static void checkLexiconEntry(String string) throws IOException {
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        InvertedIndexFileHandler invertedIndexHandler = new InvertedIndexFileHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        float termUpperBoundScoreBM25 = lexiconHandler.getTermUpperBoundScoreBM25(entryBuffer);
        float termUpperBoundScoreTFIDF = lexiconHandler.getTermUpperBoundScoreTFIDF(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        System.out.println("term = " + term);
        System.out.println("postingList = " + postingListBlock);
        System.out.println("offset = " + offset);
        System.out.println("documentFrequency = " + documentFrequency);
        System.out.println("termUpperBoundScore = " + termUpperBoundScoreBM25);
    }
}


