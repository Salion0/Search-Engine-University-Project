package it.unipi.mircv.file;
import it.unipi.mircv.utility.Config;
import it.unipi.mircv.index.LexiconEntry;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import static it.unipi.mircv.utility.Config.*;

public class LexiconFileHandler {
    /*
     * This class represent an I/O handler for the lexicon data structure
     * that allows to handle all the read and write and operation from and the
     * lexicon file. This handler allow to get a term entry from the lexicon file
     * and all the information about the term (cf,df,offset) and the upper bound score
     */
    private int lexiconRow;  //current row of the lexicon file
    public int numEntry;
    private final FileChannel lexiconFile;
    public LexiconFileHandler() throws IOException {

        // open the lexicon file in read/write mode
        RandomAccessFile raf = new RandomAccessFile(Config.LEXICON_FILE, "rw");
        this.lexiconFile = raf.getChannel();
        //set the current row to 0
        this.lexiconRow = 0;
        //calculate the number of entry in the lexicon file
        this.numEntry = (int) ((lexiconFile.size()/(Config.LEXICON_ENTRY_LENGTH)));
    }

    //same as above but with different paths
    public LexiconFileHandler(String filePath,boolean isABlock) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        this.lexiconFile = raf.getChannel();
        this.lexiconRow = 0;
        int entryLength = 0;
        if(isABlock)
             entryLength = (Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH);
        else
            entryLength = (Config.LEXICON_ENTRY_LENGTH);

        this.numEntry = (int) lexiconFile.size()/entryLength;
    }

    public ByteBuffer findTermEntry(String term) throws IOException {
        /*---------------------------------------------------------
            get a term entry from the lexicon file given the term
            using binary search.
       ---------------------------------------------------------*/

        // Add blank space to the string
        for(int i=term.length();i<Config.TERM_BYTES_LENGTH;i++){
            term = term.concat("\0");
        }

        // ByteBuffer to return that contains the term entry
        ByteBuffer dataBuffer = ByteBuffer.allocate(Config.LEXICON_ENTRY_LENGTH);
        // ByteBuffer to read the term from the lexicon file
        ByteBuffer termBuffer = ByteBuffer.allocate(Config.TERM_BYTES_LENGTH);

        //calculate the center using the file size
        long fileSize = lexiconFile.size();
        long left = 0;
        long numTerm = (fileSize/Config.LEXICON_ENTRY_LENGTH);
        long right = numTerm - 1;

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
        return null;
    }
    public ByteBuffer findTermEntryCompression(String term) throws IOException {
        /*---------------------------------------------------------
            get a term entry from the lexicon file given the term
            using binary search assuming that lexicon is compressed.
        ---------------------------------------------------------*/

        // Add blank space to the string
        for(int i=term.length();i<Config.TERM_BYTES_LENGTH;i++){
            term = term.concat("\0");
        }

        // ByteBuffer to return that contains the term entry
        ByteBuffer dataBuffer = ByteBuffer.allocate(LEXICON_COMPRESS_ENTRY_LENGTH);
        ByteBuffer termBuffer = ByteBuffer.allocate(Config.TERM_BYTES_LENGTH);

        //calculate the center using the file size
        long fileSize = lexiconFile.size();
        long left = 0;
        long numTerm = (fileSize/LEXICON_COMPRESS_ENTRY_LENGTH);
        long right = numTerm - 1;


        //take the center element and compare it with the term using compressed lexicon metadata
        while(left <= right){  //search another term if not found
            long center = (right + left)/2;
            lexiconFile.read(termBuffer,center * LEXICON_COMPRESS_ENTRY_LENGTH);
            String centerTerm = new String(termBuffer.array(), StandardCharsets.UTF_8);

            if(centerTerm.compareTo(term) < 0){
                left = center + 1;  //move the left bound to centerRow
            }
            else if (centerTerm.compareTo(term) > 0){
                right = center - 1;   //move the right bound to centerRow
            }
            else{
                lexiconFile.read(dataBuffer, center * LEXICON_COMPRESS_ENTRY_LENGTH);
                return dataBuffer;
            }
            termBuffer.clear();
        }

        return dataBuffer;
    }
    public LexiconEntry nextBlockEntryLexiconFile() throws IOException {
       /*---------------------------------------------------------
            get the next term entry from the lexicon file, assuming it
            is a block of the SPIMI algorithm, using the current row.
        ---------------------------------------------------------*/
       if(this.lexiconRow >= numEntry)
            return null;

       //allocate the buffer with appropriate dimensions that consider the block of the SPIMI algorithm
       ByteBuffer dataBuffer = ByteBuffer.allocate(
                Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH + Config.DOCUMFREQ_BYTES_LENGTH
                + Config.COLLECTIONFREQ_BYTES_LENGTH
        );

       //read the entry from the file using the current row and increment
        // /it using appropriate dimension of the entry of a block of the SPIMI algorithm.
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
        /*---------------------------------------------------------
            get the next term entry from the lexicon file
            using the current row.
        ---------------------------------------------------------*/

        //check if the current row is greater than the number of entry
        if(this.lexiconRow >= numEntry)
            return null;

        //allocate the buffer with appropriate dimensions of the lexicon file
        ByteBuffer dataBuffer = ByteBuffer.allocate(Config.LEXICON_ENTRY_LENGTH);

        //read the entry from the file using the current row and increment it
        this.lexiconFile.read(dataBuffer,
                (long) this.lexiconRow *(Config.LEXICON_ENTRY_LENGTH));
        this.lexiconRow += 1;

        //set the fields of the lexicon entry
        LexiconEntry le = new LexiconEntry();
        le.setTerm(getTerm(dataBuffer));
        le.setCf(getCf(dataBuffer));
        le.setDf(getDf(dataBuffer));
        le.setOffset(getOffset(dataBuffer));
        return le;
    }



    //*---ALL THE FOLLOWING METHODS ARE USED TO GET THE FIELDS OF A LEXICON ENTRY---*//
    public String getTerm(ByteBuffer dataBuffer) {
        byte[] termBuffer = new byte[TERM_BYTES_LENGTH];
        dataBuffer.position(0);
        dataBuffer.get(termBuffer);
        return new String(termBuffer, StandardCharsets.UTF_8);
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
    public float getTermUpperBoundScoreBM25(ByteBuffer dataBuffer) {
        return dataBuffer.position(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH
                + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH).getFloat();
    }
    public float getTermUpperBoundScoreTFIDF(ByteBuffer dataBuffer) {
        return dataBuffer.position(Config.TERM_BYTES_LENGTH + Config.OFFSET_BYTES_LENGTH
                + Config.DOCUMFREQ_BYTES_LENGTH + Config.COLLECTIONFREQ_BYTES_LENGTH + Config.UPPER_BOUND_SCORE_LENGTH).getFloat();
    }
    public int getOffsetSkipDesc(ByteBuffer dataBuffer){
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH
                + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + UPPER_BOUND_SCORE_LENGTH).getInt();
    }

    //-----------------VERSION FOR IMPLEMENTING COMPRESSION-----------------//
    public long getOffsetDocIdCompression(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH).getLong();
    }
    public long getOffsetTermFreqCompression(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH).getLong();
    }
    public int getDfCompression(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH).getInt();
    }
    public int getCfCompression(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH
                + DOCUMFREQ_BYTES_LENGTH).getInt();
    }
    public float getTermUpperBoundScoreBM25Compression(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH
                + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH).getFloat();
    }
    public float getTermUpperBoundScoreTFIDFCompression(ByteBuffer dataBuffer) {
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH
                + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH).getFloat();
    }
    public int getNumByteDocId(ByteBuffer dataBuffer){
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH
                + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + UPPER_BOUND_SCORE_LENGTH).getInt();
    }
    public int getNumByteTermFreq(ByteBuffer dataBuffer){
        return dataBuffer.position(TERM_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH
                + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + UPPER_BOUND_SCORE_LENGTH + NUM_BYTE_TO_READ_BYTE_LENGTH).getInt();
    }
    public int getOffsetSkipDescCompression(ByteBuffer dataBuffer){
        return dataBuffer.position(LEXICON_COMPRESS_ENTRY_LENGTH - OFFSET_SKIP_DESC_BYTES_LENGTH).getInt();
    }
    public void close() throws IOException {
        this.lexiconFile.close();
    }
}

