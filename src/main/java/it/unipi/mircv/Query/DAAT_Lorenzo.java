/*
package it.unipi.mircv.Query;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.mircv.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DAAT_Lorenzo {
    // We utilize the fact that the posting lists are ordered by document ID. Then, it's enough to iterate in parallel
    // through term query term's posting list, and score the minimum docid at each iteration.
    // We keep a pointer for each query term and we move it forward every time a docid is scored.
    private ArrayList<Integer> documentScores;
    private final RandomAccessFile lexiconFile;
    private final RandomAccessFile docIdFile;
    private final RandomAccessFile termFreqFile;
    private HashMap<String, Integer> currentPositionsInPostingLists = new HashMap<String, Integer>();

    private HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
    private PorterStemmer stemmer = new PorterStemmer();

    public DAAT_Lorenzo() throws FileNotFoundException {
        lexiconFile = new RandomAccessFile("lexicon.dat","r");
        docIdFile = new RandomAccessFile("docIdFile.dat","r");
        termFreqFile = new RandomAccessFile("termFreq.dat","r");
        documentScores = new ArrayList<>();
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

    public void computeScore(String query,int docId) throws IOException {  // fa BM25 della query per quel docID

        HashMap<String,Integer> queryTermsFrequency = new HashMap<String, Integer>(); // ci tengo ogni term della query con la sua frequency
        int queryLength = 0; // aggiornato per ogni termine trovato nella query, rappresenta la lunghezza della query
        String[] queryTerms = query.split(" "); // ottengo i term della query

        for (String token : queryTerms) {  //map with frequencies only
            token = stemmer.stemWord(token);
            //TODO stopWordRemoval
            //token = stopWordRemoval (token);
            if (token != null){
                queryLength++;
                if (queryTermsFrequency.get(token) == null)
                    queryTermsFrequency.put(token, 1);
                else
                    queryTermsFrequency.put(token, queryTermsFrequency.get(token) + 1);
            }
        }

        float BM25value = 0;
        for(Map.Entry<String, Integer> entry : queryTermsFrequency.entrySet()) {
            int termFrequency; // la term frequency si intende quante volte quel term compare nella query,
                               // che è un caso che va consdirato. Siccome BM25 è una sommatoria per ogni term che compare
                               // nella query, salvandomi la frequency la uso come moltiplicatore invece che ricalcolarmi
                               // BM25 per i termini duplicati
            int documentFrequency;
            int documentLength;
            int averageDocumentLength;
            byte[] bufferForIntegerRead = new byte[Config.OFFSET_BYTES_LENGTH];

            termFreqFile.seek(currentPositionInPostingLists.get(entry.getValue())); // mi posiziono per leggere la frequency del query term per quel docId
            termFreqFile.read(bufferForIntegerRead);
            termFrequency = ByteBuffer.wrap(bufferForIntegerRead).getInt();

            documentFrequency = lexicon.get(entry.getKey());
            documentLength = index.getDocumentIndex().get(docId);
            averageDocumentLength = index.getDocumentIndex().getAverageDocumentLength;


        }

    }

    public void getAllLexicon() throws IOException {  // ottengo tutti i termini del lexicon e la lunghezza della loro posting list

        int positionLexicon = 0;
        while(true) {
            int numberOfBytesRead;
            String termRead;
            int currentOffsetRead;
            int successiveOffsetRead;
            byte[] bufferForTermRead = new byte[Config.TERM_BYTES_LENGTH]; // Define a buffer to hold the term read
            byte[] bufferForOffsetRead = new byte[Config.OFFSET_BYTES_LENGTH]; // Define a buffer to hold the offset read

            lexiconFile.seek(positionLexicon); // Seek to the desired position, Read data from that position
            //////// ***************   TERM    ******************** ///////
            numberOfBytesRead = lexiconFile.read(bufferForTermRead);   // metto nel buffer il term letto
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del Lexicon file");
                lexiconFile.close();
                return;
            }
            termRead = new String(bufferForTermRead, StandardCharsets.UTF_8);

            ///////   ****************  OFFSET  ************* //////
            positionLexicon += Config.TERM_BYTES_LENGTH;
            lexiconFile.seek(positionLexicon);
            numberOfBytesRead = lexiconFile.read(bufferForOffsetRead);  //  metto nel buffer l'offset letto
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del Lexicon file");
                lexiconFile.close();
                return;
            }
            currentOffsetRead = ByteBuffer.wrap(bufferForOffsetRead).getInt();

            positionLexicon += Config.OFFSET_BYTES_LENGTH; //  metto a 68 ma per leggere l'offset dopo faccio +64 non in-place
            lexiconFile.seek(positionLexicon + Config.TERM_BYTES_LENGTH);

            ///////    *******************   OFFSET SUCCESSIVA   *************  //////
            numberOfBytesRead = lexiconFile.read(bufferForOffsetRead);
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del Lexicon file");
                lexiconFile.close();
                return;
            }

            successiveOffsetRead = ByteBuffer.wrap(bufferForOffsetRead).getInt();

            lexiconFile.seek(positionLexicon);  //  rimetto il seek a 68 così al ciclo dopo leggo correttamente il term successivo

            lexicon.put(termRead,successiveOffsetRead - currentOffsetRead);

        }
    }


*/
