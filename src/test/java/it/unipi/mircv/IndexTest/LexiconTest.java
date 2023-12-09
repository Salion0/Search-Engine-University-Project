package it.unipi.mircv.IndexTest;

import com.sun.source.tree.ArrayTypeTree;
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

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;


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

    @Order(1)
    static void init() {
        try {
            lexiconFileHandler = new LexiconFileHandler(path + lexiconFile, false);
            invertedIndexFileHandler = new InvertedIndexFileHandler(path + docIdsFile, path + termFreqsFile);
        } catch (IOException io) {
            System.out.println("Exception in creating file handler");
            io.printStackTrace();
        }

        //init postingLists
        for (int i = 0; i < numTerms; i++) {
            PostingList pl = new PostingList();
            for (int j = 0; j < i; j++)
                pl.addPostingElement(new PostingElement(j, i));
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

            LexiconEntry le = new LexiconEntry("term" + i,
                    cf,
                    postingLists.size(),
                    postingLists.get(i));
            lexicon.addLexiconEntry(le);
        }

        //print lexicon
        for (int i = 0; i < numTerms; i++) {
            System.out.println("term" + i);
            System.out.println("Df: "+lexicon.getDf("term" + i));
            System.out.println("Cf: "+lexicon.getCf("term" + i));
            System.out.print("PostingList: ");
            for (PostingElement pe : lexicon.getPostingList("term" + i).getList())
                System.out.print("\s"+pe.getDocId() + " " + pe.getTermFreq()+" - ");
            System.out.println();
        }
    }

    @Test
    @Order(2)
    void toDiskTest() {
        init();
        //try write the lexicon to disk
        try {
            lexicon.toDisk(path, lexiconFile, docIdsFile, termFreqsFile);
        } catch (IOException io) {
            System.out.println("Exception in writing lexicon to disk");
            io.printStackTrace();
        }

        //try read the lexicon from disk
        Lexicon lexiconRead = new Lexicon();
        try {
            LexiconEntry le;
            while((le = lexiconFileHandler.nextEntryLexiconFile())!=null){
                lexiconRead.addLexiconEntry(le);
                System.out.println("Lexicon Entry: "+le.getTerm()+"\n");
                le.setPostingList(invertedIndexFileHandler.getPostingList(le.getOffset(),le.getDf()));
            }
        }catch(IOException io){
            System.out.println("Exception in reading from the lexicon file");
            io.printStackTrace();
        }

        //compare the two lexicon
        assertEquals(lexicon.toString(),lexiconRead.toString());
    }
}
