package it.unipi.mircv.indexing;

import com.sun.source.tree.Tree;

import javax.swing.text.Position;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

public class Lexicon {

    private TreeMap<String,LexiconEntry> treeMap = new TreeMap();

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

    //TODO
    public void toDisk(String  fileLexicon,String fileDocId,String fileTermFreq) throws IOException {
        boolean append = true; //Open all FileOutputStream in append mode
        FileOutputStream fosLexicon = new FileOutputStream(fileLexicon,append);
        FileOutputStream fosDocId = new FileOutputStream(fileDocId,append);
        FileOutputStream fosTermFreq = new FileOutputStream(fileTermFreq,append);

        int offset =0;
        int bufferLexiconSize = (treeMap.size())*(64+4);
        ByteBuffer lexiconBuffer = ByteBuffer.allocate(bufferLexiconSize);
        for(String term: treeMap.keySet()){
            //Write Lexicon on file using ByteBuffer
            lexiconBuffer.put(term.getBytes());
            lexiconBuffer.put((byte) offset);
            offset += getPostingList(term).getSize();
            fosLexicon.write(lexiconBuffer.array());

            //Write posting list in two files: docIds file and termFreq file
            byte [][] bytePostingList = getPostingList(term).getBytes();
            fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
            fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq

        }

        fosLexicon.close();
        fosDocId.close();
        fosTermFreq.close();
    }


    //TODO
    /*
    public byte[][] getBytes(){
    //[ [term,offset,term,offset] , [doc1,doc2,doc,3,doc3,doc1p2,doc],[tf1,tf2,tf3,tf4, tf1,tf2,tf3,tf4 ]
        byte[][] indexData = new byte[3][];
        int bufferLexiconSize = (treeMap.size())*(64+4);
        ByteBuffer lexiconBuffer = ByteBuffer.allocate(bufferLexiconSize);
        int offset = 0;
        ArrayList<Byte> docIdByte = new ArrayList<Byte>();
        ArrayList<Byte> termFreqByte = new ArrayList<Byte>();

        for(String term: treeMap.keySet()){
            int capacity =0;
           lexiconBuffer.put(term.getBytes());
           lexiconBuffer.put((byte) offset);
           offset += getPostingList(term).getSize();


           for(int i = 0;i<b.length;i++){
               docIdByte.add(b[0][i]);
               termFreqByte.add(b[1][i]);
           }
        }

        indexData[0] = lexiconBuffer.array();


    return indexData ;
    }*/
}


