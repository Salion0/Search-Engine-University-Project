package it.unipi.mircv.Index;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import static it.unipi.mircv.Config.*;


public class BlockMerger {
    private static int numberOfBlocks;
    private static String path = "./data/";
    private static int offsetToWrite = 0;
    private static final ArrayList<LexiconHandler> lexiconBlocks = new ArrayList<>();
    private static ArrayList<InvertedIndexHandler> postingListBlocks = new ArrayList<>();
    private static ArrayList<Boolean> blockFinished = new ArrayList<>();
    private static ArrayList<LexiconEntry> currentBlockEntry = new ArrayList<>();
    private static ArrayList<Boolean> minTermFoundInBlock = new ArrayList<>();

    private static int postingListOffset = 0;  //offset to write in the final lexicon file for each term
    private String getMinTerm() throws IOException {
        String minTerm = null;

        for (int i = 0; i < numberOfBlocks; i++) {      // cerco il term minore dal punto di vista lessicografico

            if (currentBlockEntry.get(i) == null) continue;  //skip iteration

            //If at the previous iteration the block i-th contains the minTerm => we have to read the next element (term) of the Lexicon of that block
            if (minTermFoundInBlock.get(i)) {
                currentBlockEntry.set(i, lexiconBlocks.get(i).nextTermLexiconFile());
            }
            else{
                if (minTerm == null) {  //Need to set minTerm at the first element of the Lexicon in the first iteration
                    minTerm = currentBlockEntry.get(i).getTerm();
                } else {
                    String currentTerm = currentBlockEntry.get(i).getTerm(); //this return -1, 0 or 1
                    if (currentTerm.compareTo(minTerm)<0)
                        minTerm = currentBlockEntry.get(i).getTerm();
                }
            }
        }
        return minTerm;
    }
    public void mergeBlocks() throws IOException {

        //TODO inserire conteggio dei blocchi calcolando il numero dei file in automatico da
        int numberOfBlocks = 0;


        //---------------------------------FILE HANDLER---------------------------------------------------------------------------------------------------
        for (int blockIndex = 0; blockIndex < numberOfBlocks; blockIndex++) {
            minTermFoundInBlock.add(true); // initialize arrayList
            blockFinished.add(false); // initialize arrayList

            LexiconHandler lexiconHandler = new LexiconHandler(path+"lexicon"+blockIndex+".dat");
            InvertedIndexHandler plHandler = new InvertedIndexHandler(path+"docIds"+blockIndex+".dat",path+"termFreq"+blockIndex+".dat");
            lexiconBlocks.add(blockIndex,lexiconHandler);
            postingListBlocks.add(blockIndex,plHandler);
        }
        FileOutputStream fosLexicon = new FileOutputStream(path+"lexicon.dat",true);
        FileOutputStream fosDocId = new FileOutputStream(path+"docIds.dat",true);
        FileOutputStream fosTermFreq = new FileOutputStream(path+"termFreq.dat",true);
        //--------------------------------------------------------------------------------------------------------------------------------------------------------


        String minTerm;
        while(true) {
            //at each iteration a new term is handled. The minTerm will be the first term in lexicographical increasing order
            //TODO ottimizzare la scelta del minTerm salvando il secondo minTerm, valutare se abbia senso in realtà
            minTerm = getMinTerm();
            if(minTerm == null)
                break;

            //----------------------------------MERGING--------------------------------------------------------------------
            PostingList postingList = new PostingList();
            int docFreqSum = 0;
            int collFreqSum = 0;
            for (int i = 0; i < numberOfBlocks; i++) {  //for each block merge the corresponding entry with the min term

                if (currentBlockEntry.get(i) == null) continue;  //skip iteration if block is completed

                if (minTerm.compareTo(currentBlockEntry.get(i).getTerm()) == 0) {
                    postingList.addPostingList(postingListBlocks.get(i).getPostingList(
                            currentBlockEntry.get(i).getOffset(),
                            currentBlockEntry.get(i).getDf()
                            )
                    );

                    docFreqSum += currentBlockEntry.get(i).getDf();
                    collFreqSum += currentBlockEntry.get(i).getCf();
                    minTermFoundInBlock.set(i, true);
                }
                else
                    minTermFoundInBlock.set(i, false);
            }
            //-------------------------------------------------------------------------------------------------------------


            //appending term and posting list in final files
            writeToDisk(fosLexicon,fosDocId,fosTermFreq,minTerm, offsetToWrite, docFreqSum, collFreqSum, postingList);
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
        /*//DEBUG ------printing the whole merged lexicon-------
        for (int i = 0; i < terms.size(); i++) {
            System.out.println(terms.get(i) + " --->> " + postingLists.get(i));
        }
        */
        //DEBUG ---------------------------------------
    }
    private void writeToDisk(FileOutputStream fosLexicon,FileOutputStream fosDocId,FileOutputStream fosTermFreq,String term, int offset, int docFreq, int collFreq, PostingList postingList) throws IOException {

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
        fosLexicon.write(termBuffer.array());
        fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
        fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq
    }
}
