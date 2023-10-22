package it.unipi.mircv.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BlockReadingHandler {

    // Positions (offset) we start reading from, We start reading from position 0
    private int offsetDocId;
    private int offsetLexicon;
    private int offsetTermFreq;
    private int blockId;
    private int currentOffsetRead;
    private int successiveOffsetRead;
    private int termByteLength = 64;
    private int offsetByteLength = 4;
    private RandomAccessFile lexiconFile;
    private RandomAccessFile docIdFile;
    private RandomAccessFile termFreqFile;
    public boolean endOfLexiconReached;

    public BlockReadingHandler(String directoryPath, String lexiconPath, String docIdPath, String termFreqPath, int blockId) throws FileNotFoundException {
        offsetLexicon = 0;
        offsetDocId = 0;
        offsetTermFreq = 0;
        currentOffsetRead = 0;
        successiveOffsetRead = 0;
        this.blockId = blockId;
        lexiconFile = new RandomAccessFile(directoryPath + lexiconPath + blockId + ".dat", "r");
        docIdFile = new RandomAccessFile(directoryPath + docIdPath + blockId + ".dat", "r");
        termFreqFile = new RandomAccessFile(directoryPath + termFreqPath + blockId + ".dat", "r");
        endOfLexiconReached = false;
    }

    public String readLexiconFile() throws IOException {    // leggo un term e il suo offset

        if (endOfLexiconReached == true) return null;

        int numberOfBytesRead;
        String termRead;
        byte[] bufferForTermRead = new byte[64]; // Define a buffer to hold the term read
        byte[] bufferForOffsetRead = new byte[4]; // Define a buffer to hold the offset read

        lexiconFile.seek(offsetLexicon); // Seek to the desired position, Read data from that position
        ///////// ***************   TERM    ******************** ///////
        numberOfBytesRead = lexiconFile.read(bufferForTermRead);   // metto nel buffer il term letto
        if (numberOfBytesRead == -1) {
            System.out.println("Raggiunta la fine del Lexicon file");
            lexiconFile.close();
            endOfLexiconReached = true;
            return "";
        }
        //System.out.println("Numero di bytes letti: " + numberOfBytesRead);
        termRead = new String(bufferForTermRead, StandardCharsets.UTF_8);
        //System.out.println("term: " + termRead);

        offsetLexicon += termByteLength;
        lexiconFile.seek(offsetLexicon);

        /////  ****************  OFFSET  ************* //////
        numberOfBytesRead = lexiconFile.read(bufferForOffsetRead);  //  metto nel buffer l'offset letto
        if (numberOfBytesRead == -1) {
            System.out.println("Raggiunta la fine del Lexicon file");
            lexiconFile.close();
            endOfLexiconReached = true;
            return "";
        }
        //System.out.println("Numero di bytes letti: " + numberOfBytesRead);
        currentOffsetRead = ByteBuffer.wrap(bufferForOffsetRead).getInt();
        //System.out.println("offset: " + offsetRead);
        offsetLexicon += offsetByteLength; //  metto a 68 ma per leggere l'offset dopo faccio +64 non in-place
        lexiconFile.seek(offsetLexicon + termByteLength);

        ///////    *******************   OFFSET SUCCESSIVO   *************  //////

        numberOfBytesRead = lexiconFile.read(bufferForOffsetRead);
        if (numberOfBytesRead == -1) {
            System.out.println("Raggiunta la fine del Lexicon file");
            lexiconFile.close();
            endOfLexiconReached = true;
            return "";
        }
        //System.out.println("Numero di bytes letti: " + numberOfBytesRead);
        successiveOffsetRead = ByteBuffer.wrap(bufferForOffsetRead).getInt();
        //System.out.println("offset: " + successiveOffsetRead);
        lexiconFile.seek(offsetLexicon);  //  rimetto il seek a 68 così al ciclo dopo leggo correttamente il term successivo


        return termRead;
        //for (int i = offsetRead; i < successiveOffsetRead; i++)
        //    readPostingListFiles(offsetRead);

    }

    public void readPostingListFiles(PostingList postingList) throws IOException {
        int numberOfBytesRead;
        int docIdRead;
        int termFreqRead;
        byte[] bufferForIntegerRead = new byte[4]; // Define a buffer to hold the offset read

        offsetDocId = currentOffsetRead*4;
        offsetTermFreq = currentOffsetRead*4;
        for (int i = currentOffsetRead; i < successiveOffsetRead; i++) {
            docIdFile.seek(offsetDocId); // Seek to the desired position, Read data from that position
            termFreqFile.seek(offsetTermFreq);

            numberOfBytesRead = docIdFile.read(bufferForIntegerRead);
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del DocId file");
                docIdFile.close();
                return ;
            }
            //System.out.println("Numero di bytes letti: " + numberOfBytesRead);
            docIdRead = ByteBuffer.wrap(bufferForIntegerRead).getInt();
            //System.out.println("docId: " + docIdRead);

            numberOfBytesRead = termFreqFile.read(bufferForIntegerRead);
            if (numberOfBytesRead == -1) {
                System.out.println("Raggiunta la fine del TermFreq file");
                termFreqFile.close();
                return ;
            }
            //System.out.println("Numero di bytes letti: " + numberOfBytesRead);
            termFreqRead = ByteBuffer.wrap(bufferForIntegerRead).getInt();
            //System.out.println("termFreq: " + termFreqRead);

            PostingElement postingElement = new PostingElement(docIdRead, termFreqRead);
            postingList.addPostingElement(postingElement);

            offsetDocId += offsetByteLength;
            offsetTermFreq += offsetByteLength;
            //docIdFile.seek(offsetDocId);
            //termFreqFile.seek(offsetTermFreq);
        }
    }
}
