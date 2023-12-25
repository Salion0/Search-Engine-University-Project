package it.unipi.mircv.IndexTest;


import it.unipi.mircv.index.DocumentIndex;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DocumentIndexTest {

    static String filePath = "dataTest/documentIndexTest.dat";
    static DocumentIndex docIndex;
    static DocumentIndexFileHandler documentIndexFileHandler;

    static int numDocToTest = 7;
    @Test
    @Order(1)
     void addTest(){

        try{
            //try create the file
            docIndex= new DocumentIndex(filePath);

            //try add elements like doc0, doc1, doc2 with length 0,1,2
            for(int i=0;i<numDocToTest;i++){
                docIndex.add("doc"+ i, i);
                System.out.println("doc"+ i+ ","+ i);
            }

        }catch(IOException io){
            System.out.println("Error in creating document index object");
            io.printStackTrace();
        }

        try{
            //try read the file and check if the elements are correct
            documentIndexFileHandler = new DocumentIndexFileHandler(filePath);
            System.out.println("numDoc: "+ docIndex.getNumDocs());
            int docsLen[] = documentIndexFileHandler.loadAllDocumentLengths(docIndex.getNumDocs());
            for(int i=0;i<numDocToTest;i++){
                assertEquals(docsLen[i],i);
                System.out.println("Assert num"+ i+ ": True");
            }
        }catch(IOException io){
            System.out.println("Error in creating document index file handler");
            io.printStackTrace();
        }
    }
    @Test
    @Order(2)
    void addAverageDocumentLengthTest() {
        //Calculate average to compare with
        int sum=0;
        for(int i =0; i<numDocToTest;i++){
            sum += i;
        }
        float avgToCompare = sum/(float)numDocToTest;

        try{
            //try add average document length
            docIndex.addAverageDocumentLength();
        }catch(IOException io){
            System.out.println("Exception in adding Average Document Length");
            io.printStackTrace();
        }
        try {
            //try read the file and check if the elements are correct
            float avgDocLen = documentIndexFileHandler.readAvgDocLen();
            assertEquals(avgDocLen,avgToCompare);
            System.out.println("Assert avgDocLen: True");
        }catch(IOException io){
            System.out.println("Error in reading average document length");
            io.printStackTrace();
        }
    }
}
