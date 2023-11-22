package it.unipi.mircv.Index;

import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.LexiconHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static it.unipi.mircv.Config.*;

public class BlockMerger {
    private final int numberOfBlocks;
    private int offsetToWrite;
    private int docFreqSum;
    private int collFreqSum;
    private int offsetSkipDescriptor;
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
    SkipDescriptorFileHandler skipDescriptorFileHandler;


    public BlockMerger(int numberOfBlocks) throws IOException {
        offsetToWrite = 0;
        docFreqSum = 0;
        offsetSkipDescriptor = 0;
        fosLexicon = new FileOutputStream("./data/lexicon.dat",true);
        fosDocId = new FileOutputStream("./data/docIds.dat",true);
        fosTermFreq = new FileOutputStream("./data/termFreq.dat",true);

        skipDescriptorFileHandler = new SkipDescriptorFileHandler();

        this.numberOfBlocks = numberOfBlocks;

        for (int i = 0; i < numberOfBlocks; i++) {
            BlockReader blockReader = new BlockReader("data/", "lexicon", "docIds", "termFreq", i);
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
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        //int avgDocLength = documentIndexHandler.readAvgDocLen();


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

            // ********** TERM UPPER BOUND ************
            //float termUpperBoundScore = computeTermUpperBound(documentIndexHandler,postingList,avgDocLength);
            // TODO scrivere nel lexicon.dat il termUpperBoundScore

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
        //DEBUG ---------------------------------------
    }

    private float computeTermUpperBound(DocumentIndexHandler documentIndexHandler,
                                        PostingList postingList,int avgDocLength) throws IOException {
        int documentFrequency = postingList.getSize();
        float maxScore = -1;

        for (PostingElement postingElement: postingList.getPostingList())
        {
            float currentScore = computeBM25(postingElement.getTermFreq(),
                    documentIndexHandler.readDocumentLength(postingElement.getDocId()),documentFrequency,avgDocLength,1);
            if (currentScore > maxScore)
                maxScore = currentScore;
        }

        return maxScore;
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

        //Write posting list in docIds and termFreq files
        byte[][] bytePostingList = postingList.getBytes();

        fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
        fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq

        ///SUUUUUUUUU///////SUUUUUUUUU///////SUUUUUUUUU///////SUUUUUUUUU///////SUUUUUUUUU///////SUUUUUUUUU

        int postingListSize = postingList.getSize();
        if (postingListSize > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
            SkipDescriptor skipDescriptor = new SkipDescriptor();
            int postingListSizeBlock = (int) Math.sqrt(postingListSize);

            for (int i = 0; i <= postingListSize - postingListSizeBlock; i += postingListSizeBlock){
                int maxDocId = postingList.getPostingList().get(i + postingListSizeBlock - 1).getDocId();
                int offsetMaxDocId = offsetToWrite + i;
                skipDescriptor.add(maxDocId, offsetMaxDocId);
            }

            //the last offset will be written here
            if (postingListSize%postingListSizeBlock != 0) {
                int maxDocId = postingList.getPostingList().get(postingListSize - 1).getDocId();
                int offsetMaxDocId = offsetToWrite + postingListSizeBlock*postingListSizeBlock;
                skipDescriptor.add(maxDocId, offsetMaxDocId);
            }

            termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH);
            termBuffer.putInt(offsetSkipDescriptor);

            skipDescriptorFileHandler.writeSkipDescriptor(offsetSkipDescriptor, skipDescriptor);
            offsetSkipDescriptor += skipDescriptor.size(); //aggiorno l'offset che devo inserire nel lexiconEntry,
        }
        fosLexicon.write(termBuffer.array());
    }

    private float computeBM25(int termFrequency, int documentLength, int documentFrequency, int avgDocLen, int collectionSize) {
        return (float) (( termFrequency / (termFrequency + 1.5 * ((1 - 0.75) + 0.75*(documentLength / avgDocLen))) )
                * (float) Math.log10(collectionSize/documentFrequency));
    }
}
