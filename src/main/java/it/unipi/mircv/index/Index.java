package it.unipi.mircv.index;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.mircv.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.Utils.*;

public class Index {
    private final DocumentIndex documentIndex;
    private final PorterStemmer stemmer = new PorterStemmer();
    private int numberOfBlocks;
    private int currentDocId;
    private boolean debugFlag;
    private int count = 0; //DEBUG
    private String blockFolder;
    private String collectionFile;


    public Index(String blockFolder,String collectionFile,boolean debugFlag) throws IOException {
        this.blockFolder = blockFolder;
        this.collectionFile = collectionFile;
        this.debugFlag = debugFlag;
        //this method remove precedent files
        cleanFolder(blockFolder);
        loadStopWordList();

        BufferedReader reader;
        if (flagCompressedReading) {
            FileInputStream fis = new FileInputStream(collectionFile+".gz");
            GZIPInputStream gzis = new GZIPInputStream(fis);
            InputStreamReader inputStreamReader = new InputStreamReader(gzis, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);

            //System.out.println(reader.readLine()); // DEBUG eseguite questo se volete vedere i metadati della prima riga
            reader.mark(1024); // 1024 è quanti byte può leggere prima che il mark diventi non più valido
            String[] values = reader.readLine().split("\t"); //per vedere alla prima linea quanto sono lunghi i metadati
            reader.reset(); // riporto il reader all' inizio perché era andato alla riga successiva
            reader.skip(values[0].length() - 1); // skip metadata
        }
        else reader = new BufferedReader(new FileReader(collectionFile)); // vecchio reader prima della Compressed Reading


        documentIndex = new DocumentIndex();
        currentDocId = 0;
        int blockID = 0;
        try {
            while(reader!=null){
                System.out.println("BlockID: "+blockID); //DEBUG
                //singlePassInMemoryIndexing may stop for memory lack
                reader = singlePassInMemoryIndexing(blockID,reader);
                System.gc();
                blockID++;
            }
            numberOfBlocks = blockID;
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        documentIndex.addAverageDocumentLength();
    }

    private void writeLexiconToBlock(Lexicon lexicon, int blockID) throws IOException {
        String fileLexicon = "lexicon" + blockID + ".dat";
        String fileDocIds = "docIds" + blockID+".dat";
        String fileTermFreq = "termFreq" + blockID+".dat";
        lexicon.toBlock(this.blockFolder,fileLexicon,fileDocIds,fileTermFreq);
    }

    private double freeMemoryPercentage() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (double) freeMemory / totalMemory * 100;
    }

    private BufferedReader singlePassInMemoryIndexing(int blockID, BufferedReader reader) throws IOException {
        Lexicon lexicon = new Lexicon();
        BufferedReader readerToReturn = null;

        while (true) {
            count++; //DEBUG

            if(freeMemoryPercentage() < MEMORY_THRESHOLD_PERC){
                //poor memory qt available -> break
                readerToReturn = reader;
                //System.out.println("Memory leak! Free memory: "+ freeMemoryPercentage()); //DEBUG - print the memory available
                break;
            }

            String line = reader.readLine();
            if (!flagCompressedReading) {
                if (line == null) {
                    //we reached the end of the file -> close file reader and break
                    reader.close();
                    break;
                }
            }
            else {
                if (line.startsWith("\0\0\0\0\0")) {
                    //we reached the end of the file -> close file reader and break
                    reader.close();
                    break;
                }
            }


            //DEBUG - every tot document print the memory available
            //if (count%10000 == 0)
              //  System.out.println("Free memory percentage: "+ freeMemoryPercentage());

            //parsing and processing the document corresponding
            String[] values = line.split("\t");
            String[] tokens = Utils.tokenization(values[1]);  //take tokens from the text
            String docNo = values[0];
            int docLength = processDocument(lexicon, tokens);
            documentIndex.add(docNo, docLength);

            //DEBUG per creare più di un blocco
            if(debugFlag){
                if (count == 2 || count == 3){
                    readerToReturn = reader;
                    System.out.println("blocco finito per debug");
                    break;
                }
            }
            //DEBUG
            if(debugFlag){ if(count == 4) break;}

        }
        writeLexiconToBlock(lexicon, blockID);
        return readerToReturn;
    }

    private int processDocument(Lexicon lexicon, String[] tokens) throws IOException {
        HashMap<String, Integer> wordCountDocument = new HashMap<>();
        int tokenCount = 0;

        //Count all occurrence of all terms in a document
        for (String token : tokens) //map with frequencies only
        {
            if (token == null) continue;

            if (flagStopWordRemoval && Utils.seekInStopwords(token))  continue;// stopWordRemoval

            if (flagStemming) token = stemWord(token); // stemming

            if (token.length() > TERM_BYTES_LENGTH) continue;// il token è più lungo di 64 byte quindi lo scartiamo

            tokenCount++;
            if (wordCountDocument.get(token) == null)
                wordCountDocument.put(token, 1);
            else
                wordCountDocument.put(token, wordCountDocument.get(token) + 1);
        }

        //updating the lexicon with the document processing results
        for (String term: wordCountDocument.keySet()) {
            lexicon.addPostingElement(term, currentDocId , wordCountDocument.get(term));
        }
        currentDocId ++;

        return tokenCount;
    }
    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }
    public DocumentIndex getDocumentIndex() {
        return documentIndex;
    }
}