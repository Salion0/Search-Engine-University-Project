package it.unipi.mircv;
import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.Query.QueryProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CommandLineInterface {

    public static void main( String[] args ) throws IOException {

        String commandList = "command list: \n" +
                "help --> print this command list\n" +
                "index (c) --> perform indexing with compression or not\n" +
                "query --> (hopefully) return most N relevant docIds\n" +
                "quit";
        System.out.println(commandList);

        Scanner scanner = new Scanner(System.in);

        boolean quit = false;
        while(!quit){

            System.out.print("enter a command: ");
            String[] command = scanner.nextLine().split("\\s+");

            switch (command[0]) {
                case "index":
                {
                    if (command.length>1){
                        if(command[1].charAt(0) == 'c'){
                            System.out.println("indexing with compression...");
                            long startTime = System.currentTimeMillis();

                            //TODO add indexing with compression

                            long endTime = System.currentTimeMillis();
                            long elapsedTime = endTime - startTime;
                            System.out.println("indexing with compression finished in " + (float)elapsedTime/1000 +"sec");
                            break;
                        }

                    } else {
                        System.out.println("indexing...");
                        long startTime = System.currentTimeMillis();

                        //TODO add indexing

                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;
                        System.out.println("indexing finished in " + (float)elapsedTime/1000 +"sec");
                        break;
                    }
                }

                case "query":
                {
                    //query terms are in command[1:length-1]
                    System.out.println("number of terms: " + (command.length - 1));
                    long startTime = System.currentTimeMillis();

                    //TODO add query processing

                    // ---------------------TEST DAAT-----------------------------
                    String query = "solis";
                    QueryProcessor queryProcessor = new QueryProcessor(query);
                    ArrayList<Integer> docId = queryProcessor.DAAT();
                    System.out.println("Doc Id retrieved: ");
                    System.out.println(docId);

                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    System.out.println("query processed in " + (float)elapsedTime/1000 +"sec");
                    break;
                }

                case "help":
                    System.out.println(commandList);
                    break;

                case "quit":
                    quit = true;
                    break;

                default:
                    System.out.println("unknown command");
                    break;
            }
        }
        scanner.close();
    }
}