package it.unipi.mircv.Index;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import static it.unipi.mircv.Config.*;

public class Lexicon {

    private final TreeMap<String,LexiconEntry> treeMap = new TreeMap<>();

    public boolean termExists(String term){
        return treeMap.containsKey(term);
    }

    public PostingList getPostingList(String term){
        return treeMap.get(term).getPostingList();
    }

    public int getDf(String term){
        return treeMap.get(term).getDf();
    }

    public int getCf(String term){
        return treeMap.get(term).getCf();
    }




    public void addPostingElement(String term, int docId, int tf){
       //update term entry if exist and create an entry in the lexicon if term not exist.
        if (termExists(term)) {
            getPostingList(term).addPostingElement(new PostingElement(docId, tf));
            treeMap.get(term).setDf(treeMap.get(term).getDf()+1);
            treeMap.get(term).setCf(treeMap.get(term).getCf()+tf);
        }else {
            //add lexicon entry to the lexicon
            PostingList pl = new PostingList(new PostingElement(docId,tf));
            LexiconEntry le = new LexiconEntry();
            le.setPostingList(pl);
            le.setDf(1);
            le.setCf(tf);
            treeMap.put(term,le);
        }
    }

        public void toDisk(String path, String  fileLexicon, String fileDocId, String fileTermFreq) throws IOException {
            FileOutputStream fosLexicon = new FileOutputStream(path+fileLexicon,true);
            FileOutputStream fosDocId = new FileOutputStream(path+fileDocId,true);
            FileOutputStream fosTermFreq = new FileOutputStream(path+fileTermFreq,true);

            //offset to save in lexicon
            int offset = 0;

            for(String term: treeMap.keySet()) {
                //Write Lexicon on file using ByteBuffer
                byte[] termBytes = term.getBytes(StandardCharsets.UTF_8);


                if (termBytes.length > TERM_BYTES_LENGTH)
                    continue; //TODO questo è da spostare da qui, il termine non dovrebbe proprio arrivarci (->da gestire nella tokenization)

                ByteBuffer entryBuffer = ByteBuffer.allocate( TERM_BYTES_LENGTH+OFFSET_BYTES_LENGTH+DOCUMFREQ_BYTES_LENGTH+COLLECTIONFREQ_BYTES_LENGTH);
                entryBuffer.put(termBytes);
                entryBuffer.position(TERM_BYTES_LENGTH);

                entryBuffer.putInt(offset);
                entryBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH);

                entryBuffer.putInt(treeMap.get(term).getDf());
                entryBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH);

                entryBuffer.putInt(treeMap.get(term).getCf());


                //update the offset to write in the lexicon for the next term (next iteration)
                offset += getPostingList(term).getSize();
                fosLexicon.write(entryBuffer.array());

                //Write posting list in two different files: docIds file and termFreq file
                byte[][] bytePostingList = getPostingList(term).getBytes();
                fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
                fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq
            }
            fosLexicon.close();
            fosDocId.close();
            fosTermFreq.close();
        }
    }