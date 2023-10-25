package it.unipi.mircv.queryprocessing;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class DAAT {
    // We utilize the fact that the posting lists are ordered by document ID. Then, it's enough to iterate in parallel
    // through term query term's posting list, and score the minimum docid at each iteration.
    // We keep a pointer for each query term and we move it forward every time a docid is scored.
    private final RandomAccessFile lexiconFile;
    private final RandomAccessFile docIdFile;
    private final RandomAccessFile termFreqFile;
    private HashMap<String, Integer> currentPositionInPostingLists = new HashMap<String, Integer>();

    public DAAT() throws FileNotFoundException {
        lexiconFile = new RandomAccessFile("lexicon.dat","r");
        docIdFile = new RandomAccessFile("docIdFile.dat","r");
        termFreqFile = new RandomAccessFile("termFreq.dat","r");
    }

    public void getMinDocId() {  // Returns the minimum docid across the posting lists, or num_docs if all
        //postings in all posting lists have been processed.
        int minDocId = totalNumberOfDocuments;
        for (Map.Entry<String, Integer> entry : currentPositionInPostingLists.entrySet())
            int currentDocId = // entro nel lexicon, guardo l' offset per il key term e ci sommo il position value per ottenere
                                // current docId e confrontarlo con minDocId, devo anche controllare di non essere arrivato
                                // all'offset del termine successivo, in tal caso è finita la posting list del term corrente
        }
    }
}
