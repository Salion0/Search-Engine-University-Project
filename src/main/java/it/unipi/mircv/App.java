package it.unipi.mircv;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.BlockMerger;
import it.unipi.mircv.index.Index;

import static it.unipi.mircv.Config.*;

public class App
{
    public static void main( String[] args )  {
        try{
            DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
            Config.collectionSize = documentIndexFileHandler.readCollectionSize();

            flagStemming=false;
            flagStopWordRemoval=true;
            flagCompressedReading=false;

            Index index = new Index("data/","collection.tsv",false);

            BlockMerger blockMerger = new BlockMerger();
            blockMerger.mergeBlocks(index.getNumberOfBlocks());

            //read the first PostingList
            //InvertedIndexFileHandler plFileHandler = new InvertedIndexFileHandler();

            //test verifica merging
            /*LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
            InvertedIndexFileHandler plHandler = new InvertedIndexFileHandler();
            String term = "break";
            ByteBuffer dataBuffer = lexiconFileHandler.findTermEntry(term);
            int offset = lexiconFileHandler.getOffset(dataBuffer);
            int length = lexiconFileHandler.getDf(dataBuffer);
            System.out.println("Offset: "+offset+" - Length: "+length);
            System.out.println(term+": pl: "+plHandler.getPostingList(offset,length).getPostingList());
*/
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


