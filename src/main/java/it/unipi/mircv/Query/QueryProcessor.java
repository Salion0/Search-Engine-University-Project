/*
package it.unipi.mircv.Query;

import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.Lexicon;
import it.unipi.mircv.Index.LexiconEntry;
import it.unipi.mircv.Index.PostingList;
import it.unipi.mircv.Index.PostingListBlock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static it.unipi.mircv.Index.Config.*;

public class QueryProcessor {

    //------------------------------------------------------------------------//
    private int collectionSize;

    //------------------------------------------------------------------------//
    private int[] docIDRetrieved;  //list of docIDs retrieved sorted by ranking

    private int[] docsScore; //list of score related to docIDs

    //------------------------------------------------------------------------//
    private int[] query_tf;  //list of term frequencies in the queries
    private boolean[] endOfPostingListFlag;
    private String[] queryTerms;//query terms
    private int[] docFreqs; //doc frequencies of query terms
    private int[] collectionFreqs; // collection frequencies of query terms
    private int[] offsets; // offsets of the posting list of query terms


    //------------------------------------------------------------------------//
    public QueryProcessor(String query, int collectionSize) throws IOException {
        this.queryTerms = query.split("\\s");
        this.query_tf = new int[queryTerms.length];
        this.collectionSize = collectionSize;

        this.docFreqs =  new int[queryTerms.length];
        this.collectionFreqs = new int[queryTerms.length];
        this.offsets = new int[queryTerms.length];

        LexiconHandler lexiconHandler = new LexiconHandler(LEXICON_FILE);
        for (int i = 0; i < queryTerms.length; i++) {
                ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
                docFreqs[i] = lexiconHandler.getDf(entryBuffer);
                collectionFreqs[i] = lexiconHandler.getCf(entryBuffer);
                offsets[i] = lexiconHandler.getOffset(entryBuffer);
        }


    }

    public QueryProcessor(String[] query, int collectionSize) {
        this.queryTerms = query;
        this.collectionSize = collectionSize;
    }

    //TODO
    private void posUpdate() {
        //increment the position in each posting list of the term query
    }


    private int getMinDocId(Lexicon lexicon) {

        int minDocId = this.collectionSize;  //valore che indica che le posting list sono state raggiunte
        //TODO
        PostingList term_pl;
        for (String term : this.queryTerms) {
            term_pl = lexicon.getPostingList(term);
            int current_doc_id = term_pl.currentDocId();  //return the current doc id pointed
            if (current_doc_id < minDocId) {
                minDocId = current_doc_id;
            }
        }
        return minDocId;
    }

    private float termScore() {
        float termscore = 0;
        //TODO

        return termscore;
    }

    public int[] DAAT() {
        //Process the query using Document At A Time
        //TODO

        int minDocId;
        float score = 0;
        while ((minDocId = getMinDocId(lexicon)) != this.collectionSize) {

            int i = 0;
            int currentTf = 0;
            for (String term : queryTerms) {
                PostingListBlock termPl = (PostingListBlock) lexicon.getPostingList(term);
                if (termPl.currentDocId() == minDocId) {
                    currentTf = termPl.currentTf();
                    termPl.next();  //increment the position in the posting list
                    score += termScore();
                }
                //TODO check if the score is correct
            }


        }
        //TODO sort docIDRetrieved

        return this.docIDRetrieved;
    }


    public int[] TAAT(){
        //Process the query using Document At A Time
        //TODO


        return this.docIDRetrieved;
    }

    public String[] getQuery() {
        return queryTerms;
    }

    public void setCollectionSize(int collectionSize) {
        this.collectionSize = collectionSize;
    }
}
*/
