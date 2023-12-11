package it.unipi.mircv.IndexTest;

import it.unipi.mircv.Config;
import it.unipi.mircv.Utils;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.*;

import java.util.HashMap;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Utils.stemWord;


public class IndexTest {
    //FILE name ---------------------------
    static String blockFolder = "dataTest/spimiTest/";
    static String testCollection = "collection_for_test.tsv";
    static String lexiconFile = "lexicon.dat";
    static String docIdsFile = "docIds.dat";
    static String termFreqsFile = "termFreq.dat";
    // ---------------------------

    static Index index;

    static int currentDocId = 0;
        static int processDocument(Lexicon lexicon, String[] tokens) throws IOException {
        HashMap<String, Integer> wordCountDocument = new HashMap<>();
        int tokenCount = 0;

        //Count all occurrence of all terms in a document
        for (String token : tokens) //map with frequencies only
        {
            if (token == null) continue;

            if (flagStopWordRemoval && Utils.seekInStopwords(token))  continue;// stopWordRemoval

            if (flagStemming) token = stemWord(token); // stemming

            if (token.length() > TERM_BYTES_LENGTH) continue;// il token è più lungo di 64 byte quindi lo scartiamo

            tokenCount++;
            if (wordCountDocument.get(token) == null)
                wordCountDocument.put(token, 1);
            else
                wordCountDocument.put(token, wordCountDocument.get(token) + 1);
        }

        //updating the lexicon with the document processing results
        for (String term: wordCountDocument.keySet()) {
            lexicon.addPostingElement(term, currentDocId , wordCountDocument.get(term));
            String filledTerm = term;
            for(int i=term.length();i<Config.TERM_BYTES_LENGTH;i++){//ADD BLANKSPACE TO THE STRING
                filledTerm = filledTerm.concat("\0");
            }
            lexicon.getMap().get(term).setTerm(filledTerm);
        }
        currentDocId ++;

        return tokenCount;
        }
        static String lexiconToString(Lexicon lexicon){
            StringBuilder stringBuilder = new StringBuilder();
            for(String term: lexicon.getAllTerms()){
                stringBuilder.append("Term: "+lexicon.getMap().get(term).getTerm()+"\n")
                        .append("Posting List: "+ lexicon.getPostingList(term)+"\n")
                        .append("DF: "+lexicon.getDf(term)+"\n")
                        .append("CF: "+lexicon.getCf(term)+"\n");
            }
            return stringBuilder.toString();
        }
        static Lexicon createLexiconTest() throws IOException {
            Lexicon lexicon = new Lexicon();
            BufferedReader reader = new BufferedReader(new FileReader("collection_for_test.tsv"));
            String document;
            while((document = reader.readLine()) !=null ){
                String[] values = document.split("\t");
                String[] tokens = Utils.tokenization(values[1]);  //take tokens from the text
                processDocument(lexicon,tokens);
            }
            reader.close();
            return lexicon;
        }
        static Lexicon loadMergedLexicon() throws IOException {
        //constrcuct the lexicon by loading data from the merged file
            Lexicon lexicon = new Lexicon();
            InvertedIndexFileHandler invertedIndexFileHandler = new InvertedIndexFileHandler(blockFolder+docIdsFile,blockFolder+termFreqsFile);
            LexiconFileHandler lexiconFileHandler = new LexiconFileHandler(blockFolder+lexiconFile,false);
            LexiconEntry le;
            while((le = lexiconFileHandler.nextEntryLexiconFile())!=null){
                lexicon.addLexiconEntry(le);
                PostingList pl = invertedIndexFileHandler.getPostingList(le.getOffset(),le.getDf());
                lexicon.setPostingList(le.getTerm(),pl);
            }
            return lexicon;
        }

        @Test
        void createIndexTest() {
            //Test for Index creation on the first three document of the collection.
            //The collection is in the dataTest folder.
            //The index is created in the dataTest folder.
            //The index is merged in the dataTest folder.
            //The index is created with the debug flag set to true
            // to create three blocks each one contain the first three document of the collectioncleanFolder

            System.out.println("Current Directory: "+System.getProperty("user.dir"));

            Utils.loadStopWordList();
            flagStopWordRemoval=true;
            flagStemming=false;
            flagCompressedReading = false;

            Lexicon testLexicon = null;
            Lexicon mergedLexicon = null;

            try{
                //CREATING LEXICON TO COMPARE WITH MERGED LEXICON FOR ASSERT
                testLexicon = createLexiconTest();
                //INDEXING
                index = new Index(blockFolder,testCollection,true);
                //MERGING
                BlockMerger.path = "dataTest/spimiTest/";
                BlockMerger.mergeBlocks(index.getNumberOfBlocks());
                //LOAD MERGED LEXICON
                mergedLexicon = loadMergedLexicon();
            }catch(IOException io){io.printStackTrace();}

            System.out.println(" ");
            System.out.println("Merged Lexicon:"+lexiconToString(mergedLexicon));
            System.out.println(" ");
            System.out.println("Test lexicon: "+lexiconToString(testLexicon));

            assertEquals(lexiconToString(mergedLexicon),lexiconToString(testLexicon));
        }

}

