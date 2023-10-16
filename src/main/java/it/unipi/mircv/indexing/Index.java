package it.unipi.mircv.indexing;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Index {
     // Replace with your file's path

     public static void createInvertedIndex(String filePath){
         int blockID = 0;
         try {
             BufferedReader reader = new BufferedReader(new FileReader(filePath)); //read
              while(!singlePassInMemoryIndexing(blockID,reader)){   //If it is possible to singlePassInMemoryIndexing then do it and increment blockID otherwise file end so exit
                  blockID++;
              }
             reader.close();
         } catch (IOException e) {
             System.err.println("Error reading the file: " + e.getMessage());
         }

         //TODO implement merge
     }

     //TODO
     public static void writeInvertedIndexToFile(InvertedIndex invertedIndex, int blockID) throws IOException{

         String fileName = blockID +".dat";
         FileOutputStream fileOutputStream = new FileOutputStream(fileName);
         FileChannel fileChannel = fileOutputStream.getChannel();
         fileChannel.position(0);


         //TODO Fare for each che per ogni termine alloca un buffer in memoria e scrive su file termine e posting lists
         for(String term:invertedIndex.getInvertedIndex().keySet()) {

             //TODO per ogni termine calcolare in byte il suo valore e quello della Posting List associata
             ByteBuffer termBuffer = ByteBuffer.allocate(term.getBytes().length); //byte of a term
             ByteBuffer bufferPostingList = ByteBuffer.allocate(invertedIndex.getInvertedIndex().get(term).getPostingList().size());
             dataTerm =
             buffer.put(dataInvertedIndex); //fill the buffer
             buffer.flip(); //this is necessary because after put() the buffer has switched to read mode

             int bytesWritten = fileChannel.write(buffer);
         */
         }
             //DEBUG
         if (bytesWritten != dataInvertedIndex.length) System.out.println("Errore nella scrittura dio madonna");



         fileChannel.close();
         fileOutputStream.close();
    }

     public static double freeMemory(){
        long totalMemory = Runtime.getRuntime().totalMemory();
        System.out.println("totalMemory "+totalMemory);
        long freeMemory = Runtime.getRuntime().freeMemory(); //memory available
        System.out.println("Free memory: "+freeMemory);
        return (double) freeMemory / totalMemory * 100;
    }

    public static boolean singlePassInMemoryIndexing(int blockID,BufferedReader reader) throws IOException {
        InvertedIndex invertedIndex = new InvertedIndex();
        int count = 0; //DEBUG
        while (Index.freeMemory() <80) {
            String line = reader.readLine();
            if(line == null){
                return true;
            }
            System.out.println("Free memory percentage: "+ Index.freeMemory());
            String[] values = line.split("\t"); //split document text and docID
            String[] tokens = Index.tokenization(values[1]);  //take tokens from the text
            invertedIndex.processDocument(Integer.parseInt(values[0]), tokens);
            count += 1; //DEBUG
            if (count == 10000) break; //DEBUG
        }
        writeInvertedIndexToFile(invertedIndex, blockID);
        return false;
    }

    public static boolean isValid(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    public static String findURLsExample(String inputString) {

            String pattern = "\\b(?:https?|ftp)://\\S+\\b"; // Matches URLs starting with http://, https://, or ftp://

            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(inputString);
            ArrayList<String> tokens = new ArrayList<String>();
            while (m.find()) {
                String url = m.group();

                System.out.println("Match found: " + url);
                if (isValid(url))
                    System.out.println("Yes");
                else
                    System.out.println("No");
            }

            return "";
        }


        public static String[] tokenization(String doc) {

            //System.out.println(doc);
            //html tags removal
            doc = doc.replaceAll("<[^>]*>", "");

            //punctuation and whitespace
            String result = doc.replaceAll("\\p{Punct}","").toLowerCase();
            String[] tokens = result.split("\\s+");

            //System.out.println(result);
            return tokens;
        }



}

