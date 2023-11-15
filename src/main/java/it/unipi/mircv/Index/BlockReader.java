package it.unipi.mircv.Index;

import it.unipi.mircv.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BlockReader {

    // Positions (offset) we start reading from, We start reading from position 0
    private int docIdPos;
    private int tfPos;
    private int blockId;
    private int lexiconPos;
    private int collectionFrequency;
    private final RandomAccessFile lexiconFile;
    private int documentFrequency;
    private int currentOffsetRead;
    private int successiveOffsetRead;
    private final RandomAccessFile docIdFile;
    private final RandomAccessFile termFreqFile;
    public boolean endOfLexiconReached;
    public BlockReader(String directoryPath, String lexiconPath, String docIdPath, String termFreqPath, int blockId) throws FileNotFoundException {
        this.lexiconPos = 0;
        this.docIdPos = 0;
        this.tfPos = 0;
        this.currentOffsetRead = 0;
        this.successiveOffsetRead = 0;
        this.blockId = blockId;
        this.lexiconFile = new RandomAccessFile(directoryPath + lexiconPath + blockId + ".dat", "r");
        this.docIdFile = new RandomAccessFile(directoryPath + docIdPath + blockId + ".dat", "r");
        this.termFreqFile = new RandomAccessFile(directoryPath + termFreqPath + blockId + ".dat", "r");
        this.endOfLexiconReached = false;
    }

    public int getCurrentOffsetRead() {
        return currentOffsetRead;
    }
    public int getCollectionFrequency() {
        return collectionFrequency;
    }
    public int getDocumentFrequency() {
        return documentFrequency;
    }

}
