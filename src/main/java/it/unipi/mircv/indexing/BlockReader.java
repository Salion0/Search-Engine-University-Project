package it.unipi.mircv.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BlockReader {

    // Positions (offset) we start reading from, We start reading from position 0
    private int positionDocId;
    private int positionLexicon;
    private int positionTermFreq;
    private int blockId;
    private int currentOffsetRead;
    private int successiveOffsetRead;
    private final int TERM_BYTES_LENGTH = 64;
    private final int OFFSET_BYTES_LENGTH = 4;
    private final RandomAccessFile lexiconFile;
    private final RandomAccessFile docIdFile;
    private final RandomAccessFile termFreqFile;
    public boolean endOfLexiconReached;

    public BlockReader(String directoryPath, String lexiconPath, String docIdPath, String termFreqPath, int blockId) throws FileNotFoundException {
        positionLexicon = 0;
        positionDocId = 0;
        positionTermFreq = 0;
        currentOffsetRead = 0;
        successiveOffsetRead = 0;
        this.blockId = blockId;
        lexiconFile = new RandomAccessFile(directoryPath + lexiconPath + blockId + ".dat", "r");
        docIdFile = new RandomAccessFile(directoryPath + docIdPath + blockId + ".dat", "r");
        termFreqFile = new RandomAccessFile(directoryPath + termFreqPath + blockId + ".dat", "r");
        endOfLexiconReached = false;
    }

    public String nextTermLexiconFile() throws IOException {    // reading the next term with his offset
        //TODO da rivedere

        if (endOfLexiconReached)
            return null;

        int numberOfBytesRead;
        String termRead;
        byte[] bufferForTermRead = new byte[TERM_BYTES_LENGTH]; // Define a buffer to hold the term read
        byte[] bufferForOffsetRead = new byte[OFFSET_BYTES_LENGTH]; // Define a buffer to hold the offset read

        lexiconFile.seek(positionLexicon); // Seek to the desired position, Read data from that position
        ///////// ***************   TERM    ******************** ///////
        numberOfBytesRead = lexiconFile.read(bufferForTermRead);   // metto nel buffer il term letto
        if (numberOfBytesRead == -1) {
            System.out.println("Raggiunta la fine del Lexicon file");
            lexiconFile.close();
            endOfLexiconReached = true;
            return null;
        }
        termRead = new String(bufferForTermRead, StandardCharsets.UTF_8);

        ///////   ****************  OFFSET  ************* //////
        positionLexicon += TERM_BYTES_LENGTH;
        lexiconFile.seek(positionLexicon);
        numberOfBytesRead = lexiconFile.read(bufferForOffsetRead);  //  metto nel buffer l'offset letto
        if (numberOfBytesRead == -1) {
            System.out.println("Raggiunta la fine del Lexicon file");
            lexiconFile.close();
            endOfLexiconReached = true;
            return null;
        }

        currentOffsetRead = ByteBuffer.wrap(bufferForOffsetRead).getInt();

        positionLexicon += OFFSET_BYTES_LENGTH; //  metto a 68 ma per leggere l'offset dopo faccio +64 non in-place
        lexiconFile.seek(positionLexicon + TERM_BYTES_LENGTH);

        ///////    *******************   OFFSET SUCCESSIVA   *************  //////

        numberOfBytesRead = lexiconFile.read(bufferForOffsetRead);
        if (numberOfBytesRead == -1) {
            System.out.println("Raggiunta la fine del Lexicon file");
            lexiconFile.close();
            endOfLexiconReached = true;
            return null;
        }

        successiveOffsetRead = ByteBuffer.wrap(bufferForOffsetRead).getInt();

        lexiconFile.seek(positionLexicon);  //  rimetto il seek a 68 così al ciclo dopo leggo correttamente il term successivo

        return termRead;

    }

    public void readPostingListFiles(PostingList postingList) throws IOException {
        int numberOfBytesRead;
        int docIdRead;
        int termFreqRead;
        byte[] bufferForIntegerRead = new byte[OFFSET_BYTES_LENGTH]; // Define a buffer to hold the offset read

        positionDocId = currentOffsetRead * OFFSET_BYTES_LENGTH;
        positionTermFreq = currentOffsetRead * OFFSET_BYTES_LENGTH;
        for (int i = currentOffsetRead; i < successiveOffsetRead; i++) {
            docIdFile.seek(positionDocId); // Seek to the desired position, Read data from that position
            termFreqFile.seek(positionTermFreq);

            numberOfBytesRead = docIdFile.read(bufferForIntegerRead);
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del DocId file");
                docIdFile.close();
                return ;
            }

            docIdRead = ByteBuffer.wrap(bufferForIntegerRead).getInt();

            numberOfBytesRead = termFreqFile.read(bufferForIntegerRead);
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del TermFreq file");
                termFreqFile.close();
                return ;
            }

            termFreqRead = ByteBuffer.wrap(bufferForIntegerRead).getInt();


            PostingElement postingElement = new PostingElement(docIdRead, termFreqRead);
            postingList.addPostingElement(postingElement);

            positionDocId += OFFSET_BYTES_LENGTH;
            positionTermFreq += OFFSET_BYTES_LENGTH;
            //docIdFile.seek(offsetDocId);
            //termFreqFile.seek(offsetTermFreq);
        }
    }
}
