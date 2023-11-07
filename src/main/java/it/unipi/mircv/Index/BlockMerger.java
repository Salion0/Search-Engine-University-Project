package it.unipi.mircv.Index;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static it.unipi.mircv.Index.Config.*;


public class BlockMerger {
    private final int numberOfBlocks;
    private int offsetToWrite;
    private int docFreqSum;
    private int collFreqSum;
    private final ArrayList<BlockReader> blocks = new ArrayList<>();

    //TODO questi due in realtà dovranno sparire nella verisione finale, perché già li scriviamo su file
    //-----
    private final ArrayList<String> terms = new ArrayList<>();
    private final ArrayList<PostingList> postingLists = new ArrayList<>();
    //-----

    private final ArrayList<Boolean> blockFinished = new ArrayList<>();
    private final ArrayList<String> currentTermOfBlocks = new ArrayList<>();
    private final ArrayList<Boolean> minTermFoundInBlock = new ArrayList<>();
    private final FileOutputStream fosLexicon;
    private final FileOutputStream fosDocId;
    private final FileOutputStream fosTermFreq;
    int postingListOffset;  //offset to write in the final lexicon file for each term


    public BlockMerger(int numberOfBlocks) throws FileNotFoundException {
        offsetToWrite = 0;
        docFreqSum = 0;
        fosLexicon = new FileOutputStream("./data/lexicon.dat",true);
        fosDocId = new FileOutputStream("./data/docIds.dat",true);
        fosTermFreq = new FileOutputStream("./data/termFreq.dat",true);

        this.numberOfBlocks = numberOfBlocks;

        for (int i = 0; i < numberOfBlocks; i++) {
            BlockReader blockReader = new BlockReader("data./", "lexicon", "docIds", "termFreq", i);
            minTermFoundInBlock.add(true); // initialize arrayList
            blockFinished.add(false); // initialize arrayList
            blocks.add(i, blockReader);
            currentTermOfBlocks.add(""); // initialize arrayList
        }
        postingListOffset = 0;
    }

    public void mergeBlocks() throws IOException {

        //int countBlockFinished = 0;
        String minTerm; // come valore iniziale prendo questo che controllerò con un if
        //DEBUG
        int iterations = 0;

        while(true) {
            //at each iteration a new term is handled. The minTerm will be the first term in lexicographical increasing order

            //TODO ottimizzare la scelta del minTerm salvando il secondo minTerm, valutare se abbia senso in realtà
            minTerm = null;
            for (int i = 0; i < numberOfBlocks; i++) {      // cerco il term minore dal punto di vista lessicografico

                if (blockFinished.get(i)) continue;

                if (minTermFoundInBlock.get(i)) //If at the previous iteration the block i-th contains the minTerm =>
                    //we have to read the next element (term) of the Lexicon of that block
                    // TODO qui in realtà al posto di next term dovrebbe essere next entry perchè poi nel file finale ci andranno scritte tutte le statistiche di un termine
                    currentTermOfBlocks.set(i, blocks.get(i).nextTermLexiconFile());

                if (currentTermOfBlocks.get(i) == null) { //ho finito il blocco e incremento contatore dei blocchi letti
                    blockFinished.set(i, true);
                    //countBlockFinished++;
                } else {
                    if (minTerm == null)
                        minTerm = currentTermOfBlocks.get(i);  // serve all' inizio per settare minTerm al primo term trovato
                    int compare = currentTermOfBlocks.get(i).compareTo(minTerm); //this return -1, 0 or 1
                    if (compare < 0)
                        minTerm = currentTermOfBlocks.get(i);
                }
            }
            /*
            if (countBlockFinished == numberOfBlocks) // condizione di terminazione del while, i.e ho letto tutti i lexicon
                break;
            countBlockFinished = 0;
            */
            if(minTerm == null)
                break;

            PostingList postingList = new PostingList();
            BlockReader blockReader;
            docFreqSum = 0;
            collFreqSum = 0;
            for (int i = 0; i < numberOfBlocks; i++) {  // scorro i blocchi e se ne trovo uno con term che matcha lo aggiungo alla Posting List del term
                if (currentTermOfBlocks.get(i) == null) continue;
                int compare = minTerm.compareTo(currentTermOfBlocks.get(i));
                if (compare == 0) {
                    blockReader = blocks.get(i);
                    blockReader.readPostingListFiles(postingList);
                    docFreqSum += blockReader.getDocumentFrequency();
                    collFreqSum += blockReader.getCollectionFrequency();
                    minTermFoundInBlock.set(i, true);
                }
                else
                    minTermFoundInBlock.set(i, false);
            }

            //appending term and posting list in final files
            writeToDisk(minTerm, offsetToWrite, docFreqSum, collFreqSum, postingList);
            offsetToWrite += docFreqSum;

            //DEBUG -----------------------------
            //terms.add(minTerm);  //salvo term e Posting List associata
            //postingLists.add(postingList);
            //System.out.println(minTerm + " --->> " + postingList);
            //DEBUG ---------------------------------------

        }
        fosLexicon.close();
        fosDocId.close();
        fosTermFreq.close();

        //DEBUG ------printing the whole merged lexicon-------
        /*
        for (int i = 0; i < terms.size(); i++) {
            System.out.println(terms.get(i) + " --->> " + postingLists.get(i));
        }*/
        System.out.println(terms.size());
        //DEBUG ---------------------------------------
    }

    //TODO da vedere se funziona
    private void writeToDisk(String term, int offset, int docFreq, int collFreq, PostingList postingList) throws IOException {

        byte[] termBytes = term.getBytes(StandardCharsets.UTF_8);
        ByteBuffer termBuffer = ByteBuffer.allocate(LEXICON_ENTRY_LENGTH);
        termBuffer.put(termBytes);
        termBuffer.position(TERM_BYTES_LENGTH);
        termBuffer.putInt(offset);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH);
        termBuffer.putInt(docFreq);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH);
        termBuffer.putInt(collFreq);

        //update the offset to write in the lexicon for the next term (next iteration)
        postingListOffset += postingList.getSize();
        fosLexicon.write(termBuffer.array());

        //Write posting list in docIds and termFreq files
        byte[][] bytePostingList = postingList.getBytes();

        fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
        fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq
    }
}
