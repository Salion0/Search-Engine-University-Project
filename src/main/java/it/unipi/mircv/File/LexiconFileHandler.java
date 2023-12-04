package it.unipi.mircv.File;

import it.unipi.mircv.Config;
import it.unipi.mircv.Index.LexiconEntry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class LexiconFileHandler {
    private int lexiconRow;
    private int collectionFrequency;
    private int currentOffsetRead;
    private int successiveOffsetRead;
    public int numEntry;
    //Class that create a file-channel to the lexicon file and implement write and read method for that file
    private final FileChannel lexiconFile;
    public LexiconFileHandler() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(Config.LEXICON_FILE, "rw");
        this.lexiconFile = raf.getChannel();
        this.lexiconRow = 0;
        this.numEntry = (int) ((lexiconFile.size()/(Config.LEXICON_ENTRY_LENGTH)));
    }
    public LexiconFileHandler(String filePath) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        this.lexiconFile = raf.getChannel();
        this.lexiconRow = 0;
        this.numEntry = (int) ((lexiconFile.size()/(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH)));
    }
    public ByteBuffer findTermEntry(String term) throws IOException {
        //Find a term in the lexicon file by binary search assuming that a=0; b=FileSize; c = center that we calculate at each iteration
        for(int i=term.length();i<Config.TERM_BYTES_LENGTH;i++){      //ADD BLANKSPACE TO THE STRING
            term = term.concat("\0");
        }
        ByteBuffer dataBuffer = ByteBuffer.allocate(Config.LEXICON_ENTRY_LENGTH);  //ByteBuffer to return
        ByteBuffer termBuffer = ByteBuffer.allocate(Config.TERM_BYTES_LENGTH);     //it changes at every iteration

        long fileSize = lexiconFile.size();  // size

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

        return dataBuffer;
    }
    public LexiconEntry nextBlockEntryLexiconFile() throws IOException {
        // reading the next term with his offset
        if(this.lexiconRow >= numEntry)
            return null;
        ByteBuffer dataBuffer = ByteBuffer.allocate(
                Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH + Config.DOCUMFREQ_BYTES_LENGTH
                + Config.COLLECTIONFREQ_BYTES_LENGTH);
        this.lexiconFile.read(dataBuffer,
                (long) this.lexiconRow *(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH)
        );
        this.lexiconRow += 1;

        LexiconEntry le = new LexiconEntry();
        le.setTerm(getTerm(dataBuffer));
        le.setCf(getCf(dataBuffer));
        le.setDf(getDf(dataBuffer));
        le.setOffset(getOffset(dataBuffer));

        return le;
    }
    public LexiconEntry nextEntryLexiconFile() throws IOException {
        // reading the next term with his offset
        if(this.lexiconRow >= numEntry)
            return null;
        ByteBuffer dataBuffer = ByteBuffer.allocate(Config.LEXICON_ENTRY_LENGTH);
        this.lexiconFile.read(dataBuffer,
                (long) this.lexiconRow *(Config.LEXICON_ENTRY_LENGTH));
        this.lexiconRow += 1;

        LexiconEntry le = new LexiconEntry();
        le.setTerm(getTerm(dataBuffer));
        le.setCf(getCf(dataBuffer));
        le.setDf(getDf(dataBuffer));
        le.setOffset(getOffset(dataBuffer));

        return le;
    }


/*    public String getTerm(ByteBuffer dataBuffer) {
        return new String(Arrays.copyOfRange(dataBuffer.array(), 0, Config.TERM_BYTES_LENGTH), StandardCharsets.UTF_8);
    }*/
    public String getTerm(ByteBuffer dataBuffer) {
        byte[] termBuffer = new byte[Config.TERM_BYTES_LENGTH];
        dataBuffer.position(0);
        dataBuffer.get(termBuffer);
        return new String(termBuffer, StandardCharsets.UTF_8);
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
    public float getTermUpperBoundScore(ByteBuffer dataBuffer) {
        return dataBuffer.position(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH
                + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH).getFloat();
    }
    public int getOffsetSkipDesc(ByteBuffer dataBuffer){
        return dataBuffer.position(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH
                + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH + Config.UPPER_BOUND_SCORE_LENGTH).getInt();
    }

    public void close() throws IOException {
        this.lexiconFile.close();
    }

}

