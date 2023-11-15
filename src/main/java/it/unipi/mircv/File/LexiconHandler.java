package it.unipi.mircv.File;

import it.unipi.mircv.Config;
import it.unipi.mircv.Index.LexiconEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class LexiconHandler{
    private int lexiconPos;
    private int collectionFrequency;
    private int currentOffsetRead;
    private int successiveOffsetRead;

    public int numEntry;
    //Class that create a file-channel to the lexicon file and implement write and read method for that file
    private final FileChannel lexiconFile;
    public LexiconHandler() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(Config.LEXICON_FILE, "rw");
        this.lexiconFile = raf.getChannel();
        this.lexiconPos = 0;
        this.numEntry = (int) ((lexiconFile.size()/Config.LEXICON_ENTRY_LENGTH));
    }
    public LexiconHandler(String filePath) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        this.lexiconFile = raf.getChannel();
        this.lexiconPos = 0;
        this.numEntry = (int) ((lexiconFile.size()/Config.LEXICON_ENTRY_LENGTH));
    }
    public ByteBuffer findTermEntry(String term) throws IOException {
        //Find a term in the lexicon file by binary search assuming that a=0; b=FileSize; c = center that we calculate at each iteration
        for(int i=term.length();i<Config.TERM_BYTES_LENGTH;i++){      //ADD BLANKSPACE TO THE STRING
            term = term.concat("\0");
        }
        ByteBuffer dataBuffer = ByteBuffer.allocate(Config.LEXICON_ENTRY_LENGTH);  //ByteBuffer to return
        ByteBuffer termBuffer = ByteBuffer.allocate(Config.TERM_BYTES_LENGTH);     //it changes at every iteration

        long fileSize = lexiconFile.size();  // size
        System.out.println("File size:"+fileSize); //DEBUG

        long left = 0;
        long numTerm = (fileSize/Config.LEXICON_ENTRY_LENGTH);
        long right = numTerm - 1;
        //calculate the center using the file size

        //take the center element.
        while(left <= right){  //search another term if not found
            long center = (right + left)/2;
            lexiconFile.read(termBuffer,center * Config.LEXICON_ENTRY_LENGTH);
            String centerTerm = new String(termBuffer.array(), StandardCharsets.UTF_8);

            if(centerTerm.compareTo(term) < 0){
                left = center + 1;  //move the left bound to centerRow
            }
            else if (centerTerm.compareTo(term) > 0){
                right = center - 1;   //move the right bound to centerRow
            }
            else{
                lexiconFile.read(dataBuffer, center * Config.LEXICON_ENTRY_LENGTH);
                return dataBuffer;
            }
             termBuffer.clear();
         }

        System.out.println(); //DEBUG
        return dataBuffer;
    }
    public LexiconEntry nextTermLexiconFile() throws IOException {    // reading the next term with his offset

        if(this.lexiconPos > numEntry)
            return null;
        ByteBuffer dataBuffer = ByteBuffer.allocate(Config.LEXICON_ENTRY_LENGTH);
        this.lexiconFile.read(dataBuffer, (long) this.lexiconPos *Config.LEXICON_ENTRY_LENGTH);
        this.lexiconPos += 1;

        LexiconEntry le = new LexiconEntry();
        le.setTerm(getTerm(dataBuffer));
        le.setCf(getCf(dataBuffer));
        le.setDf(getDf(dataBuffer));
        le.setOffset(getOffset(dataBuffer));

        return le;
    }


    public String getTerm(ByteBuffer dataBuffer) {
        return new String(Arrays.copyOfRange(dataBuffer.array(), 0, Config.TERM_BYTES_LENGTH), StandardCharsets.UTF_8);
    }
    public int getOffset(ByteBuffer dataBuffer) {
        return dataBuffer.position(Config.TERM_BYTES_LENGTH).getInt();
    }
    public int getDf(ByteBuffer dataBuffer) {
        return dataBuffer.position(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH).getInt();
    }
    public int getCf(ByteBuffer dataBuffer) {
        return dataBuffer.position(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH + Config.DOCUMFREQ_BYTES_LENGTH).getInt();
    }

}

