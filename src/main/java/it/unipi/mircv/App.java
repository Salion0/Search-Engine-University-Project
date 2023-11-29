package it.unipi.mircv;
import it.unipi.mircv.Index.BlockMerger;
import it.unipi.mircv.Index.Index;

import java.io.*;
import java.nio.ByteBuffer;

import static it.unipi.mircv.Config.*;

public class App
{
    public static void main( String[] args )  {
        try{
            //Index index = new Index("test_collection.tsv");
            // count number of blocks
            String path = "./data/";
            File directory=new File(path);
            int numberOfBlocks = (directory.list().length)/3;
            System.out.println("Number of blocks: "+numberOfBlocks);
            BlockMerger blockMerger = new BlockMerger();
            blockMerger.mergeBlocks(numberOfBlocks);
            } catch(Exception e){
                e.printStackTrace();
            }


    }



/*    public void testWord() throws FileNotFoundException {

        int[] buffer = new int[64];

        String striga = "Hello World!";
        ByteBuffer stringaBuffer = ByteBuffer.allocate(64);
        stringaBuffer.put(striga.getBytes());
        FileOutputStream fos = new FileOutputStream("test.dat");
        fos.write(stringaBuffer.array());

        FileInputStream test = new FileInputStream("test.dat");
        BufferedInputStream testBuff = new BufferedInputStream(test);
        bytesRead = testBuff.read(buffer, offsetIncrement, offsetIncrement + termByteLength); //leggo il primo int

        String termTest = new String(buffer,StandardCharsets.UTF_8);
        System.out.println(termTest.charAt(10));
    }*/

}


