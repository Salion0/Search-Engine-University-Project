package it.unipi.mircv.indexing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

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

    public void update(String term,int docId,int tf){
       //update  term posting lists if exist and create an entry in the lexicon if term not exist.
        if (termExists(term)) {
            getPostingList(term).addPostingElement(new PostingElement(docId, tf));
        }else {
            PostingList pl = new PostingList(new PostingElement(docId,tf));
            LexiconEntry le = new LexiconEntry();
            le.setPostingList(pl);
            treeMap.put(term,le);
        }
    }

    public void toDisk(String path, String  fileLexicon, String fileDocId, String fileTermFreq) throws IOException {
        FileOutputStream fosLexicon = new FileOutputStream(path+fileLexicon,true);
        FileOutputStream fosDocId = new FileOutputStream(path+fileDocId,true);
        FileOutputStream fosTermFreq = new FileOutputStream(path+fileTermFreq,true);
        //offset to save in lexicon

        int offset =0;
        //int count = 0; //DEBUG

        for(String term: treeMap.keySet()) {
            //Write Lexicon on file using ByteBuffer
            byte[] termBytes = term.getBytes(StandardCharsets.UTF_8);
            //System.out.println("term:" + term); //DEBUG
            //System.out.println("size posting list: " + treeMap.get(term).getPostingList().getSize()); //DEBUG
            //System.out.println("posting:" + treeMap.get(term).getPostingList().toString()); //DEBUG
            //System.out.println("termBytes length: " + termBytes.length); //DEBUG

            if (termBytes.length > 64)
                continue; //TODO questo è da spostare da qui, il termine non dovrebbe proprio arrivarci (->da gestire nella tokenization)
            ByteBuffer termBuffer = ByteBuffer.allocate(64 + 4);
            termBuffer.put(termBytes);
            termBuffer.position(64);
            termBuffer.putInt(offset);
            //update the offset to write in the lexicon for the next term (next iteration)
            offset += getPostingList(term).getSize();
            fosLexicon.write(termBuffer.array());

            //Write posting list in two different files: docIds file and termFreq file
            byte[][] bytePostingList = getPostingList(term).getBytes();
            fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
            fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq

            //System.out.print(offset); //DEBUG
            //count++; //DEBUG
            //DEBUG
            /*
            int c = 0;
            for (Byte b : termBuffer.array()) {
                System.out.println("Byte " + c + "-" + b);
                c++;
            }*/
        }
        fosLexicon.close();
        fosDocId.close();
        fosTermFreq.close();
        }
    }