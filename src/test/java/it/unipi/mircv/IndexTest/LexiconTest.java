package it.unipi.mircv.IndexTest;

import it.unipi.mircv.Config;
import it.unipi.mircv.Utils;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.Lexicon;
import it.unipi.mircv.index.LexiconEntry;
import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingList;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestMethodOrder;

import javax.management.openmbean.CompositeType;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LexiconTest {

    static LexiconFileHandler lexiconFileHandler;
    static InvertedIndexFileHandler invertedIndexFileHandler;
    static String path = "dataTest/lexiconTest/";
    static String lexiconFile = "lexiconTest.dat";
    static String docIdsFile = "docIdsTest.dat";
    static String termFreqsFile = "termFreqTest.dat";
    static ArrayList<PostingList> postingLists = new ArrayList<>();
    static Lexicon lexicon = new Lexicon();
    static int numTerms = 10;

    static void init() {
        //clean the folder
        try{
            Utils.cleanFolder(path);
        }catch(IOException io){
            System.out.println("Exception in cleaning the folder");
            io.printStackTrace();
        }

        //init postingLists
        for(int plCount =0; plCount<numTerms;plCount++){
            PostingList pl = new PostingList();
            for(int i = 0; i< numTerms; i++){
                PostingElement pe = new PostingElement(i,i);
                pl.addPostingElement(pe);
            }
            postingLists.add(pl);
        }

        //init the lexicon
        ArrayList<LexiconEntry> lexiconEntries = new ArrayList<>(numTerms);
        for (int i = 0; i < numTerms; i++) {
            //compute cf
            int cf = 0;
            PostingList pl = postingLists.get(i);
            for(PostingElement pe : pl.getList())
                cf+= pe.getTermFreq();

            String term = "term"+i;
            String filledTerm = term;
            for(int j = term.length(); j< Config.TERM_BYTES_LENGTH; j++){//ADD BLANKSPACE TO THE STRING
                filledTerm = filledTerm.concat("\0");
            }

            LexiconEntry le = new LexiconEntry(filledTerm,
                    cf,
                    postingLists.size(),
                    postingLists.get(i));
            lexicon.addLexiconEntry(le);
        }

    }

    @Test
    @Order(1)
    void toBlockTest() {
        init();
        //try write the lexicon to disk
        try {
            lexicon.toBlock(path, lexiconFile, docIdsFile, termFreqsFile);
        } catch (IOException io) {
            System.out.println("Exception in writing lexicon to disk");
            io.printStackTrace();
        }

        //try read the lexicon from disk
        Lexicon lexiconRead = new Lexicon();
        try {
            lexiconFileHandler = new LexiconFileHandler(path + lexiconFile, true);
            invertedIndexFileHandler = new InvertedIndexFileHandler(path + docIdsFile, path + termFreqsFile);
            LexiconEntry le = null;
            System.out.println("Starting reading Lexicon Entry...");
            while((le = lexiconFileHandler.nextBlockEntryLexiconFile())!=null){
                lexiconRead.addLexiconEntry(le);
                System.out.println("Lexicon Entry: "+le.getTerm()+"\n");
                le.setPostingList(invertedIndexFileHandler.getPostingList(le.getOffset(),le.getDf()));
            }
        }catch(IOException io){
            System.out.println("Exception in reading from the lexicon file");
            io.printStackTrace();
        }

        //print lexiconRead
        System.out.println(lexiconRead.toString());

        //print lexicon
        System.out.println(lexicon.toString());

        //compare the two lexicon
        assertEquals(lexicon.toString(),lexiconRead.toString());
        System.out.println("Lexicon are equals");
    }

    @Test
    @Order(2)
    void addPostingElementTest(){
        //add a posting element to the first term
        PostingElement pe = new PostingElement(100,100);
        String term = "term0";
        String filledTerm = term;
        for(int j = term.length(); j< Config.TERM_BYTES_LENGTH; j++){//ADD BLANKSPACE TO THE STRING
            filledTerm = filledTerm.concat("\0");
        }
        lexicon.addPostingElement(filledTerm,pe.getDocId(),pe.getTermFreq());

        //compute actual cf
        int actualCf = 0;
        for(PostingElement postingElement : lexicon.getPostingList(filledTerm).getList())
            actualCf += postingElement.getTermFreq();

        //add a posting element to a non existing term
        String nonExistingTerm = "nonExistingTerm";
        String filledNonExistingTerm = nonExistingTerm;
        for(int j = nonExistingTerm.length(); j< Config.TERM_BYTES_LENGTH; j++){//ADD BLANKSPACE TO THE STRING
            filledNonExistingTerm = filledNonExistingTerm.concat("\0");
        }
        lexicon.addPostingElement(filledNonExistingTerm,pe.getDocId(),pe.getTermFreq());


        /*-------------------ASSERT-------------------*/
        System.out.println("Testing adding PostingElement to existing term...");
        assertEquals(lexicon.getPostingList(filledTerm).getList().getLast().getDocId(),100);
        assertEquals(lexicon.getPostingList(filledTerm).getList().get(
                lexicon.getPostingList(filledTerm).getList().size()-1).getDocId(),100);
        System.out.println("Assert doc id: TRUE");
        assertEquals(lexicon.getPostingList(filledTerm).getList().getLast().getTermFreq(),100);
        assertEquals(lexicon.getPostingList(filledTerm).getList().get(
                lexicon.getPostingList(filledTerm).getList().size()-1).getTermFreq(),100);
        System.out.println("Assert TermFreq: TRUE");
        assertEquals(lexicon.getDf(filledTerm),numTerms+1);
        System.out.println("Assert DF: TRUE");
        assertEquals(lexicon.getCf(filledTerm),actualCf);
        System.out.println("Assert CF: TRUE");

        System.out.println("Test adding PostingElement to non existing term");
        assertEquals(lexicon.getPostingList(filledNonExistingTerm).getList().getLast().getDocId(),100);
        assertEquals(lexicon.getPostingList(filledNonExistingTerm).getList().get(
                lexicon.getPostingList(filledNonExistingTerm).getList().size()-1).getDocId(),100);
        System.out.println("Assert doc id: TRUE");
        assertEquals(lexicon.getPostingList(filledNonExistingTerm).getList().getLast().getTermFreq(),100);
        assertEquals(lexicon.getPostingList(filledNonExistingTerm).getList().get(
                lexicon.getPostingList(filledNonExistingTerm).getList().size()-1).getTermFreq(),100);
        System.out.println("Assert TermFreq: TRUE");
        assertEquals(lexicon.getDf(filledNonExistingTerm),1);
        System.out.println("Assert DF: TRUE");
        assertEquals(lexicon.getCf(filledNonExistingTerm),100);
        System.out.println("Assert CF: TRUE");
    }

    @Test
    @Order(3)
    void addLexiconEntryTest(){


        //adding an lexicon entry corresponding to an existing term
        PostingList pl  = new PostingList(new PostingElement(102,102));
        String existingTerm = "term0";
        String existingFilledTerm = existingTerm;
        for(int j = existingTerm.length(); j< Config.TERM_BYTES_LENGTH; j++){//ADD BLANKSPACE TO THE STRING
            existingFilledTerm = existingFilledTerm.concat("\0");
        }
        LexiconEntry existingLe = new LexiconEntry(existingFilledTerm,102,1,pl);

        lexicon.addLexiconEntry(existingLe);

        //adding an lexicon entry corresponding to an non existing term
        String nonExistingTerm = "nonExistingTerm2";
        String nonExistingFilledTerm = nonExistingTerm;
        for(int j = nonExistingTerm.length(); j< Config.TERM_BYTES_LENGTH; j++){//ADD BLANKSPACE TO THE STRING
            nonExistingFilledTerm = nonExistingFilledTerm.concat("\0");
        }
        LexiconEntry nonExistingLe = new LexiconEntry(nonExistingFilledTerm,102,1,pl);
        System.out.println("lexicon entry:  term:"+nonExistingLe.getTerm()+"\n"+
                "df: "+nonExistingLe.getDf()+"\n"+
                "cf: "+nonExistingLe.getCf()+"\n"+
                "posting list: "+nonExistingLe.getPostingList().toString());
        lexicon.addLexiconEntry(nonExistingLe);
        System.out.println("Printing the lexicon...");
        System.out.println("Lexicon: "+lexicon.toString());


        //-------------------ASSERT-------------------
        System.out.println("Testing adding LexiconEntry to existing term...");
        assertNotEquals(102,lexicon.getPostingList(existingFilledTerm).getList().getLast().getDocId());
        //assertNotEquals(10,lexicon.getPostingList(existingFilledTerm).getList().get(
        //        lexicon.getPostingList(existingFilledTerm).getList().size()-1).getDocId(),102);
        System.out.println("Assert doc id: TRUE");
        assertNotEquals(102,lexicon.getPostingList(existingFilledTerm).getList().getLast().getTermFreq());
/*        assertNotEquals(102,lexicon.getPostingList(existingFilledTerm).getList().get(
                lexicon.getPostingList(existingFilledTerm).getList().size()-1).getTermFreq());*/
        System.out.println("Assert termFreq: TRUE");
        assertNotEquals(1,lexicon.getDf(existingFilledTerm));
        System.out.println("Assert DF: TRUE");
        assertNotEquals(102,lexicon.getCf(existingFilledTerm));
        System.out.println("Assert CF: TRUE");

        System.out.println("Testing adding LexiconEntry to non existing term...");
        assertEquals(lexicon.getPostingList(nonExistingFilledTerm).getList().getLast().getDocId(),102);
        /*assertEquals(lexicon.getPostingList(nonExistingFilledTerm).getList().get(
                lexicon.getPostingList(nonExistingFilledTerm).getList().size()-1).getDocId(),102);
*/      System.out.println("Assert doc id: TRUE");
        assertEquals(lexicon.getPostingList(nonExistingFilledTerm).getList().getLast().getTermFreq(),102);
  /*      assertEquals(lexicon.getPostingList(nonExistingFilledTerm).getList().get(
                lexicon.getPostingList(nonExistingFilledTerm).getList().size()-1).getTermFreq(),102);
*/      System.out.println("Assert termFreq: TRUE");
        assertEquals(lexicon.getDf(nonExistingFilledTerm),1);
        System.out.println("Assert DF: TRUE");
        assertEquals(lexicon.getCf(nonExistingFilledTerm),102);
        System.out.println("Assert CF: TRUE");

    }
}
