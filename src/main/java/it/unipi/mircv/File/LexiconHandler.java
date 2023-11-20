package it.unipi.mircv.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


import static it.unipi.mircv.Config.*;

public class LexiconHandler{
    //Class that create a file-channel to the lexicon file and implement write and read method for that file
    private final FileChannel lexiconFile;

    public LexiconHandler() throws FileNotFoundException{
        RandomAccessFile raf = new RandomAccessFile(LEXICON_FILE, "rw");
        this.lexiconFile = raf.getChannel();
    }

    //DEBUG
    public LexiconHandler(String filePath) throws FileNotFoundException{
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        this.lexiconFile = raf.getChannel();
    }

    //BINARY SEARCH
    public ByteBuffer findTermEntry(String term) throws IOException {
        //Find a term in the lexicon file by binary search assuming that a=0; b=FileSize; c = center that we calculate at each iteration
        for(int i=term.length();i<TERM_BYTES_LENGTH;i++){      //ADD BLANKSPACE TO THE STRING
            term = term.concat("\0");
        }
        ByteBuffer dataBuffer = ByteBuffer.allocate(LEXICON_ENTRY_LENGTH);  //ByteBuffer to return
        ByteBuffer termBuffer = ByteBuffer.allocate(TERM_BYTES_LENGTH);     //it changes at every iteration

        long fileSize = lexiconFile.size();  // size
        System.out.println("File size:"+fileSize); //DEBUG

        long left = 0;
        long numTerm = (fileSize/LEXICON_ENTRY_LENGTH);
        long right = numTerm - 1;
        //calculate the center using the file size

        //take the center element.
        while(left <= right){  //search another term if not found
            long center = (right + left)/2;
            lexiconFile.read(termBuffer,center * LEXICON_ENTRY_LENGTH);
            String centerTerm = new String(termBuffer.array(), StandardCharsets.UTF_8);

            if(centerTerm.compareTo(term) < 0){
                left = center + 1;  //move the left bound to centerRow
            }
            else if (centerTerm.compareTo(term) > 0){
                right = center - 1;   //move the right bound to centerRow
            }
            else{
                lexiconFile.read(dataBuffer, center * LEXICON_ENTRY_LENGTH);
                return dataBuffer;
            }
             termBuffer.clear();
         }

        System.out.println(); //DEBUG
        return dataBuffer;
    }

    public String getTerm(ByteBuffer dataBuffer) {
        return new String(Arrays.copyOfRange(dataBuffer.array(), 0, TERM_BYTES_LENGTH), StandardCharsets.UTF_8);
    }
    public int getOffset(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH).getInt();
    }
    public int getDf(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH).getInt();
    }
    public int getCf(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH).getInt();
    }
    public int getOffsetSkipDesc(ByteBuffer dataBuffer){
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH).getInt();
    }

}

