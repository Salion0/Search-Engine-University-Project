package it.unipi.mircv.Index;
import it.unipi.mircv.File.DocumentIndexHandler;

import java.io.IOException;


public class DocumentIndex {
    //private final ArrayList<Integer> documentLengths = new ArrayList<>();
    //private final ArrayList<String> documentNos = new ArrayList<>();
    private int numberOfDocuments;
    private final DocumentIndexHandler documentIndexHandler;
    private long numberOfTokens;

    public DocumentIndex() throws IOException {
        documentIndexHandler = new DocumentIndexHandler();
        this.numberOfDocuments = 0;
    }

    public void add(String docNo, int docLength) throws IOException {
        documentIndexHandler.writeEntry(docNo, docLength);
        numberOfDocuments ++;
        numberOfTokens += docLength;
    }

    public void addAverageDocumentLength() throws IOException {
        documentIndexHandler.writeAverageDocumentLength(numberOfTokens / (float) numberOfDocuments, numberOfDocuments);
        documentIndexHandler.closeFileChannel();
    }

}
