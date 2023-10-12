package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Index {

    private void createInvertedIndex(){
        //TODO
        InvertedIndex invertedIndex = new InvertedIndex(0);
    }

    public void writeIndexToFile(){
        //TODO
    }

    public void SPIMI() {
        InvertedIndex invertedIndex = new InvertedIndex(0);
        String filePath = "C:\\Users\\HP\\Downloads\\test_collection.tsv"; // Replace with your file's path

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                String[] tokens = tokenization(values[1]);
                invertedIndex.processDocument(Integer.parseInt(values[0]), tokens);
                count += 1;
                if (count == 5) break;
            }

            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
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

    public String findURLsExample(String inputString) {

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

