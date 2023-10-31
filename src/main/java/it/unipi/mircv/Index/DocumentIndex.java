package it.unipi.mircv.Index;
import it.unipi.mircv.File.DocumentIndexHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class DocumentIndex {
    //private final ArrayList<Integer> documentLengths = new ArrayList<>();
    //private final ArrayList<String> documentNos = new ArrayList<>();
    private int collectionSize;
    private final DocumentIndexHandler documentIndexHandler;
    private long collectionLength;

    public DocumentIndex() throws IOException {
        documentIndexHandler = new DocumentIndexHandler();
        this.collectionSize = 0;
    }

    public void add(String docNo, int docLength) throws IOException {
        documentIndexHandler.writeEntry(docNo, docLength);
        collectionSize++;
        collectionLength += docLength;
    }

    public void addAverageDocumentLength() throws IOException {
        documentIndexHandler.addAverageDocumentLength(collectionLength / (float) collectionSize);
    }

}
