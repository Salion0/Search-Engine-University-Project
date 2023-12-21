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
import java.util.Scanner;

import static it.unipi.mircv.Config.MAX_NUM_DOC_RETRIEVED;
import static it.unipi.mircv.Parameters.*;
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
                query               --> (hopefully) return most N relevant docNo
                settings (-c)       --> show setting, with (-c) change settings
                ----------------------------------------------------------------
                """;

        System.out.println(title);
        System.out.println("DEFAULT SETTINGS:");
        printSettings();
        System.out.println("\n" + commandList);
        loadStopWordList();
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;
        while(!exit){
            String[] command = scanner.nextLine().split("\\s+");

            switch (command[0]) {
                case "index":
                {
                    long startTime = System.currentTimeMillis();
                    Index index = new Index("data/","collection.tar.gz", false);
                    if(flagCompression){
                        BlockMergerCompression blockMerger = new BlockMergerCompression();
                        blockMerger.mergeBlocks(index.getNumberOfBlocks());
                    }else{
                        BlockMerger.mergeBlocks(index.getNumberOfBlocks());
                    }
                    System.out.println("indexed finished in " + (int)(System.currentTimeMillis() - startTime)/1000/60 +"min");
                    break;
                }
                case "query":
                {
                    String query = scanner.nextLine();
                    long startTime = System.currentTimeMillis();
                    SystemEvaluator.queryResult(query, queryProcessType);
                    System.out.println("query processed in " + (System.currentTimeMillis() - startTime) +"ms");
                    break;
                }

                case "settings":
                    if(command.length>1 && command[1].equals("-c")) changeSettings();
                    else printSettings();
                    break;

                case "help":
                    System.out.println(commandList);
                    break;

                case "exit":
                    exit = true;
                    break;

                default:
                    System.out.println("unknown command");
                    break;
            }
        }
        scanner.close();
    }
    private static void changeSettings(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("compressed reading (t/f): ");
        scanner.nextLine();

    }
    private static void printSettings(){
        System.out.println("compressed reading: " + flagCompressedReading);
        System.out.println("stop word removal: " + flagStopWordRemoval);
        System.out.println("stemming: " + flagStemming);
        System.out.println("score type: " + scoreType);
        System.out.println("compression: " + flagCompression);
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