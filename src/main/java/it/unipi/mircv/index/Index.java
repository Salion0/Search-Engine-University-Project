package it.unipi.mircv.index;
import it.unipi.mircv.utility.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static it.unipi.mircv.utility.Parameters.*;
import static it.unipi.mircv.utility.Config.*;
import static it.unipi.mircv.utility.Utils.*;

public class Index {
    /*
    * This class is used to create the index using
    * the single pass in memory indexing algorithm (SPIMI)
    */
    private final DocumentIndex documentIndex;
    private int numberOfBlocks;
    private int currentDocId;
    private boolean debugFlag;
    private int count = 0; //DEBUG
    private final String blockFolder;
    private String collectionFile;


    public Index(String blockFolder,String collectionFile,boolean debugFlag) throws IOException {
        /*-------------------------------------------------
            The constructor of the class start the indexing
            of the collection file by the SPIMI algorithm
        -------------------------------------------------*/

        this.blockFolder = blockFolder;
        this.collectionFile = collectionFile;
        this.debugFlag = debugFlag;

        //this method remove precedent files
        cleanFolder(blockFolder);
        loadStopWordList();

        BufferedReader reader;
        //try to read the file as compressed
        if (flagCompressedReading) {
            FileInputStream fis = new FileInputStream(collectionFile);
            GZIPInputStream gzis = new GZIPInputStream(fis);
            InputStreamReader inputStreamReader = new InputStreamReader(gzis, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);

            reader.mark(1024);  //1024 is how many bytes can read before the mark becomes invalid
            String[] values = reader.readLine().split("\t"); //per vedere alla prima linea quanto sono lunghi i metadati

            // bring the reader back to the beginning of the file cause it
            // was already read a line ahead
            reader.reset();
            reader.skip(values[0].length() - 1); // skip metadata
        }
        else
            reader = new BufferedReader(new FileReader(collectionFile)); // old way to read the file

        documentIndex = new DocumentIndex();
        currentDocId = 0;
        int blockID = 0;
        try {
            while(reader!=null){
                System.out.println("BlockID: "+blockID); //DEBUG
                //singlePassInMemoryIndexing may stop for memory lack
                reader = singlePassInMemoryIndexing(blockID,reader);
                System.gc(); //call garbage collector to free memory
                blockID++;
            }
            numberOfBlocks = blockID;
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        documentIndex.addAverageDocumentLength();
    }

    private void writeLexiconToBlock(Lexicon lexicon, int blockID) throws IOException {
        /*-------------------------------------------------
            This method write the lexicon to the block file
            specified by the blockID
        -------------------------------------------------*/
        String fileLexicon = "lexicon" + blockID + ".dat";
        String fileDocIds = "docIds" + blockID+".dat";
        String fileTermFreq = "termFreq" + blockID+".dat";
        lexicon.toBlock(this.blockFolder,fileLexicon,fileDocIds,fileTermFreq);
    }

    private double freeMemoryPercentage() {
        /*-------------------------------------------------
            This method return the percentage of free memory
        -------------------------------------------------*/
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (double) freeMemory / totalMemory * 100;
    }

    private BufferedReader singlePassInMemoryIndexing(int blockID, BufferedReader reader) throws IOException {
        /*-------------------------------------------------
           Create a block of the index using
           the SPIMI algorithm
        -------------------------------------------------*/

        Lexicon lexicon = new Lexicon();
        BufferedReader readerToReturn = null;

        while (true) {
            count++; //DEBUG

            if(freeMemoryPercentage() < MEMORY_THRESHOLD_PERC){
                //poor memory qt available -> break
                readerToReturn = reader;
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

            //parsing and processing the document corresponding
            String[] values = line.split("\t");
            String[] tokens = Utils.tokenization(values[1]);  //take tokens from the text
            String docNo = values[0];
            int docLength = processDocument(lexicon, tokens);
            documentIndex.add(docNo, docLength);

            //DEBUG for testing
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
        /*-------------------------------------------------
            This method process a document and update the
            lexicon with the results
        -------------------------------------------------*/
        HashMap<String, Integer> wordCountDocument = new HashMap<>();
        int tokenCount = 0;

        //Count all occurrence of all terms in a document
        for (String token : tokens) //map with frequencies only
        {
            if (token == null || token.isEmpty()) continue;

            if (flagStopWordRemoval && Utils.seekInStopwords(token))  continue;// stopWordRemoval

            if (flagStemming) token = stemWord(token); // stemming

            //token is too long -> skip
            if(token.getBytes(StandardCharsets.UTF_8).length > TERM_BYTES_LENGTH) continue;

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
}