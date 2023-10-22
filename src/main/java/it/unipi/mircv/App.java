package it.unipi.mircv;
import it.unipi.mircv.indexing.BlockReadingHandler;
import it.unipi.mircv.indexing.Index;
import it.unipi.mircv.indexing.PostingElement;
import it.unipi.mircv.indexing.PostingList;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class App
{

    public static void main( String[] args ) throws IOException {
        //Index index = new Index("test_collection.tsv");
        testBlock(3);
    }

    public static void testBlock(int numberOfBlocks) throws IOException {

        String directoryPath = "data./"; // Replace with your file path
        String docIdPath = "docIds";
        String lexiconPath = "lexicon";
        String termFreqPath = "termFreq";

        ArrayList<BlockReadingHandler> blocks = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>();
        ArrayList<PostingList> postingLists = new ArrayList<>();
        ArrayList<String> currentTermsOfBlocks = new ArrayList<>();
        ArrayList<Boolean> minTermFoundInBlock = new ArrayList<>();

        for (int i = 0; i < numberOfBlocks; i++) {
            BlockReadingHandler block = new BlockReadingHandler(directoryPath,lexiconPath,docIdPath,termFreqPath,i);
            minTermFoundInBlock.add(true);
            blocks.add(i,block);
            currentTermsOfBlocks.add(""); // inizializzo l'ArrayList
        }

        //DEBUG
        int iterations = 0;

        String termRead = null;
        int count = 0;
        String minTerm; // come valore iniziale prendo questo che controllerò con un if

        while(true) {

            minTerm = null;
            for (int i = 0; i < numberOfBlocks; i++) {      // cerco il term minore dal punto di vista lessicografico
                if (minTermFoundInBlock.get(i) == true)
                    currentTermsOfBlocks.set(i, blocks.get(i).readLexiconFile()); //TODO rinominare in nextTermLexiconFile

                if (currentTermsOfBlocks.get(i) == null) { //ho finito il blocco e incremento contatore dei blocchi letti
                    count++;
                }
                else {
                    if (minTerm == null)
                        minTerm = currentTermsOfBlocks.get(i);  // serve all' inizio per settare minTerm al primo term trovato
                    int compare = currentTermsOfBlocks.get(i).compareTo(minTerm);
                    if (compare < 0)
                        minTerm = currentTermsOfBlocks.get(i);
                }
            }

            if (count == numberOfBlocks) break; // condizione di terminazione del while, i.e ho letto tutti i lexicon
            count = 0;

            //TODO adesso scriviamo su una postingList """mergiata""" in memoria e poi la stampiamo per debugging -> dobbiamo scrivere su file
            PostingList postingList = new PostingList();
            for (int i = 0; i < numberOfBlocks; i++) {  // scorro i blocchi e se ne trovo uno con term che matcha lo aggiungo alla Posting List del term
                if (currentTermsOfBlocks.get(i) == null) continue;
                int compare = minTerm.compareTo(currentTermsOfBlocks.get(i));
                if (compare == 0) {
                    blocks.get(i).readPostingListFiles(postingList);
                    minTermFoundInBlock.set(i,true);
                }
                else
                    minTermFoundInBlock.set(i, false);

            }

            terms.add(minTerm);  //  salvo term e Posting List associata
            postingLists.add(postingList);
            //TODO fine
            //iterations ++;
        }

        //DEBUG printing the whole lexicon
        for (int i = 0; i < terms.size(); i++) {   // printo tutti termini di tutti i merged blocks con Posting List associata
            System.out.println(terms.get(i) + " --->> " + postingLists.get(i));
            System.out.println();
        }
    }

    /*
    public void testWord() throws FileNotFoundException {

        int[] buffer = new int[64];

        String striga = "Hello World!";
        ByteBuffer stringaBuffer = ByteBuffer.allocate(64);
        stringaBuffer.put(striga.getBytes());
        FileOutputStream fos = new FileOutputStream("test.dat");
        fos.write(stringaBuffer.array());

        FileInputStream test = new FileInputStream("test.dat");
        BufferedInputStream testBuff = new BufferedInputStream(test);
        bytesRead = testBuff.read(buffer, offsetIncrement, offsetIncrement + termByteLength); //leggo il primo int

        String termTest = new String(buffer,StandardCharsets.UTF_8);
        System.out.println(termTest.charAt(10));

    }*/
}


