package it.unipi.mircv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class TestCompressedReading {
    BufferedReader reader;
    if(flagCompressedReading) {
        FileInputStream fis = new FileInputStream("collection.tar.gz");
        GZIPInputStream gzis = new GZIPInputStream(fis);
        InputStreamReader inputStreamReader = new InputStreamReader(gzis, StandardCharsets.UTF_8);
        reader = new BufferedReader(inputStreamReader);

        //System.out.println(reader.readLine()); // DEBUG eseguite questo se volete vedere i metadati della prima riga
        reader.mark(1024); // 1024 è quanti byte può leggere prima che il mark diventi non più valido
        String[] values = reader.readLine().split("\t"); //per vedere alla prima linea quanto sono lunghi i metadati
        reader.reset(); // riporto il reader all' inizio perché era andato alla riga successiva
        reader.skip(values[0].length() - 1); // skip metadata
    }
    else reader = new BufferedReader(new FileReader(fileCollectionPath)); // vecchio reader prima della Compressed Reading


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
}
