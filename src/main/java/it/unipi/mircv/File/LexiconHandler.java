package it.unipi.mircv.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;


public class LexiconHandler{
    //Class that create a filechannel to the lexicon file and implement write and read method for that file
    private FileChannel lexiconFile;

    private String name = "lexicon.dat";
    private String path = "data/";
    public LexiconHandler() throws FileNotFoundException{
        RandomAccessFile raf = new RandomAccessFile(this.path + this.name, "rw");
        this.lexiconFile = raf.getChannel();
    }

    public LexiconHandler(String name) throws FileNotFoundException{
        RandomAccessFile raf = new RandomAccessFile(this.path + name, "rw");
        this.lexiconFile = raf.getChannel();
    }

    private byte[] findTermEntry(String term) throws IOException {
        //Find a term in the lexicon file by binary search assuming that a=0; b=FileSize; c = center that we calculate at each iteration
        byte[] data = null;

        //TODO ricerca binaria su file binario con acesso casuale
        ByteBuffer termBuffer = ByteBuffer.allocate(64);
        long b = lexiconFile.size();  // size = b

        //calculate the center using the file size
        int centerRow = Math.round((float) b /2);

        //take the center element.
        lexiconFile.read(termBuffer,centerRow);
        String centerTerm = new String(termBuffer.array(), StandardCharsets.UTF_8);

         while(centerTerm!=term){
             if(centerTerm.compareTo(term)==)
             termBuffer.clear();
             //calculate new center using as new end of interval the old center
             int b = 0;


         }
        return data;
    }

    public int getCf(String term) throws IOException {
        int cf = 0;
        byte[] entry = findTermEntry(term);
        //TODO


        return cf;
    }
    public int getDf(String term) throws IOException {
        int df=0;
        byte[] entry = findTermEntry(term);
        //TODO

        return df;
    }
    public int getOffset(String term) throws IOException {
        int offset = 0;
        byte[] entry = findTermEntry(term);
        //TODO

        return offset;
    }
}

