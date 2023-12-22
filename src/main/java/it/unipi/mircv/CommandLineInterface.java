package it.unipi.mircv;
import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.Index;
import it.unipi.mircv.query.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

import static it.unipi.mircv.Config.MAX_NUM_DOC_RETRIEVED;
import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.BM25;
import static it.unipi.mircv.Parameters.Score.TFIDF;
import static it.unipi.mircv.Utils.*;

public class CommandLineInterface {

    public static void main( String[] args ) throws IOException {
        String title = """
                  ____                      _       _____             _           \s
                 / ___|  ___  __ _ _ __ ___| |__   | ____|_ __   __ _(_)_ __   ___\s
                 \\___ \\ / _ \\/ _` | '__/ __| '_ \\  |  _| | '_ \\ / _` | | '_ \\ / _ \\
                  ___) |  __/ (_| | | | (__| | | | | |___| | | | (_| | | | | |  __/
                 |____/ \\___|\\__,_|_|  \\___|_| |_| |_____|_| |_|\\__, |_|_| |_|\\___|
                                                                |___/              \
                """;

        String commandList = """
                COMMAND LIST:\s
                exit
                help
                index [file_name]   --> perform indexing |
                query               --> (hopefully) return most 10 relevant docNo ordered
                settings (-c)       --> show setting, with (-c) change settings
                ----------------------------------------------------------------
                """;

        System.out.println(title);
        System.out.println("WARNING: Every time the program start the default settings are set");
        System.out.println("WARNING: Pay attention to run query only with the same setting of the index otherwise" +
                "the program will not work properly and it could crush. The option that change index structure are: stop words removal, temming, \n");
        System.out.println("DEFAULT SETTINGS:");
        printSettings();
        System.out.println("\n" + commandList);
        loadStopWordList();
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;
        while(!exit){
            String[] command = scanner.nextLine().split("\\s+");

            switch (command[0]) {
                case "index" -> {
                    long startTime = System.currentTimeMillis();
                    Index index = new Index("data/", "collection.tar.gz", false);
                    if (flagCompression) {
                        BlockMergerCompression blockMerger = new BlockMergerCompression();
                        blockMerger.mergeBlocks(index.getNumberOfBlocks());
                    } else {
                        BlockMerger.mergeBlocks(index.getNumberOfBlocks());
                    }
                    System.out.println("indexed finished in " + (int) (System.currentTimeMillis() - startTime) / 1000 / 60 + "min");
                }
                case "query" -> {
                    String query = scanner.nextLine();
                    long startTime = System.currentTimeMillis();
                    SystemEvaluator.queryResult(query, queryProcessType);
                    System.out.println("query processed in " + (System.currentTimeMillis() - startTime) + "ms");
                }
                case "settings" -> {
                    if (command.length > 1 && command[1].equals("-c")) changeSettings();
                    else printSettings();
                }
                case "help" -> System.out.println(commandList);
                case "exit" -> exit = true;
                default -> System.out.print("unknown command\n");
            }
        }
        scanner.close();
    }
    private static void changeSettings() throws InputMismatchException {
        Scanner scanner = new Scanner(System.in);
        int intParsed;
        int queryType;
        System.out.println("enter the number corresponding to the desired option");

        //compressed reading
        System.out.print("compressed reading 1)true 2)false : ");
        intParsed = scanner.nextInt();
        if (intParsed == 1) flagCompressedReading = true;
        else if (intParsed == 2) flagCompressedReading = false;

        //stop word removal
        System.out.print("stop words removal 1)true 2)false : ");
        intParsed = scanner.nextInt();
        if (intParsed == 1) flagStopWordRemoval = true;
        else if (intParsed == 2) flagStopWordRemoval = false;

        //stemming
        System.out.print("stemming 1)true 2)false : ");
        intParsed = scanner.nextInt();
        if (intParsed == 1) flagStemming = true;
        else if (intParsed == 2) flagStemming = false;

        //compression
        System.out.print("compression 1)true 2)false : ");
        intParsed = scanner.nextInt();
        if (intParsed == 1) flagCompression = true;
        else if (intParsed == 2) flagCompression = false;

        //score
        System.out.print("score type 1)BM25 2)TFIDF : ");
        intParsed = scanner.nextInt();
        if (intParsed == 1) scoreType = BM25;
        else if (intParsed == 2) scoreType = TFIDF;

        //queryProcessor
        System.out.print("query type 1)disjunctive 2)conjunctive : ");
        intParsed = scanner.nextInt();
            //Disjunctive
        if (intParsed == 1) {
            System.out.print("process type 1)MaxScore 2)Daat : ");
            intParsed = scanner.nextInt();
            if(intParsed == 1){
                if (flagCompression) queryProcessType = DISJUNCTIVE_MAX_SCORE_C;
                else queryProcessType = DISJUNCTIVE_MAX_SCORE;
            }
            else if (intParsed == 2){
                if (flagCompression) queryProcessType = DISJUNCTIVE_DAAT_C;
                else queryProcessType = DISJUNCTIVE_DAAT;
            }
        }
            //Conjunctive
        else if (intParsed == 2) {
            if (flagCompression) queryProcessType = CONJUNCTIVE_DAAT_C;
            else queryProcessType = CONJUNCTIVE_DAAT;
        }
    }
    private static void printSettings(){
        System.out.println("compressed reading: " + flagCompressedReading);
        System.out.println("stop words removal: " + flagStopWordRemoval);
        System.out.println("stemming: " + flagStemming);
        System.out.println("compression: " + flagCompression);
        System.out.println("score type: " + scoreType);
        switch (queryProcessType) {
            case DISJUNCTIVE_DAAT -> {
                System.out.println("query type: disjunctive");
                System.out.println("process type: DAAT");
            }
            case CONJUNCTIVE_DAAT -> {
                System.out.println("query type: conjunctive");
                System.out.println("process type: DAAT");
            }
            case DISJUNCTIVE_MAX_SCORE -> {
                System.out.println("query type: disjunctive");
                System.out.println("process type: MaxScore");
            }
            case DISJUNCTIVE_DAAT_C -> {
                System.out.println("query type: disjunctive");
                System.out.println("process type: DAAT");
            }
            case CONJUNCTIVE_DAAT_C -> {
                System.out.println("query type: conjunctive");
                System.out.println("process type: DAAT");
            }
            case DISJUNCTIVE_MAX_SCORE_C -> {
                System.out.println("query type: disjunctive");
                System.out.println("process type: MaxScore");
            }
        }
    }
}