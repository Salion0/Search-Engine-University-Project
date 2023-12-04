package it.unipi.mircv;

import it.unipi.mircv.File.LexiconFileHandler;
import it.unipi.mircv.Index.LexiconEntry;

import java.io.IOException;


public class TestSalvo{

    private static void swap(int a, int b, int[] array){
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }
    public static void main(String[] args) throws IOException {

    LexiconFileHandler lexiconHandler = new LexiconFileHandler();
    LexiconEntry le = new LexiconEntry();
    while(le != null){
        le = lexiconHandler.nextEntryLexiconFile();
        System.out.println("Term: "+le.getTerm()+" - Offset: "+le.getOffset()+" - Df: "+le.getDf()+" - Cf: "+le.getCf());
    }

    /* DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        System.out.println(documentIndexHandler.readAvgDocLen());*/

/*
        //TEST SORT POSTING LIST TERMù
        int numTermQuery=3;
        int[] docFreqs =  {5,2,4};
        int[] offsets =  {1,3,4};
        int[] collectionFreqs = {4,577,87};

        for(int i=0;i<numTermQuery;i++) {
            for (int k = i + 1; k < numTermQuery; k++) {
                if (docFreqs[k] < docFreqs[i]) {
                    swap(k, i, docFreqs);
                    swap(k, i, offsets);
                    swap(k, i, collectionFreqs);
                }
            }
        }
        System.out.print("Doc Freqs: ");
        for(int j=0;j<numTermQuery;j++)
            System.out.print(+docFreqs[j]+"-");
        System.out.println();
        System.out.print("offsets: ");
        for(int j=0;j<numTermQuery;j++)
            System.out.print(offsets[j]+"-");
        System.out.println();
        System.out.print("collection freq: ");
        for(int j=0;j<numTermQuery;j++)
            System.out.print(collectionFreqs[j]+"-");
        System.out.println();*/
/*        // ---------------------TEST DAAT-----------------------------
        String query = "railroad workers";
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<Integer> docId = queryProcessor.conjunctiveDAAT();
        System.out.println("Doc Id retrieved: ");
        System.out.println(docId);*/

        //---------------------------------------------------------------

        /*
        //Test per leggere senza unzippare
        String tarFilePath = "collection.tar.gz";

        try {
            FileInputStream fis = new FileInputStream(tarFilePath);
            GZIPInputStream gzis = new GZIPInputStream(fis);
            InputStreamReader reader = new InputStreamReader(gzis);
            BufferedReader br = new BufferedReader(reader);

            String line;
            br.readLine(); // la prima riga contiene metadati quindi la salto
            int count = 0;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                count++;
                if (count == 5) break; //DEBUG
            }

            br.close();
            reader.close();
            gzis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        FileInputStream fis = new FileInputStream(tarFilePath);
        TarArchiveInputStream tis = new TarArchiveInputStream(fis);

        TarArchiveEntry entry;

        while ((entry = tis.getNextTarEntry()) != null) {
            System.out.println("File: " + entry.getName() + ", Size: " + entry.getSize());

            byte[] content = new byte[(int) entry.getSize()];
            int bytesRead = tis.read(content);

            if (bytesRead != -1) {
                System.out.println(new String(content, 0, bytesRead));
            }
        }*/
        /*
        try{
            /* FileChannel fcw = new RandomAccessFile("prova.dat","rw").getChannel();
            fc.write(ByteBuffer.wrap(stringa.getBytes()),0);
           */
            /*String stringa = "Hello World!";

            FileChannel fcr = new RandomAccessFile("prova.dat","rw").getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(stringa.length());
            fcr.read(buffer,0);

            String fileString = new String(buffer.array(), StandardCharsets.UTF_8);
            System.out.println("File size: "+fcr.size());
            System.out.print("Stringa: "+fileString);
        }catch(Exception e){
            e.printStackTrace();
        }/
        */

        //----------TEST PER LA RICERCA BINARIA------------------

    /*    try {
            LexiconHandler lexhandler = new LexiconHandler("lexicon.dat");
            ByteBuffer dataBuffer = lexhandler.findTermEntry("Solis,");
            //System.out.println("Byte size: "+dataBuffer.array().length);

            byte [] termData = new byte[64];
            dataBuffer.get(0,termData);

            dataBuffer.position(72);
            int cf = dataBuffer.getInt();

            System.out.println("Term: "+new String(termData,StandardCharsets.UTF_8));
            System.out.println("CF "+cf);

        }catch(IOException e){
            e.printStackTrace();
        }

*/

        ///TEST PER LA LETTURA DELLA POSTING LIST DA FILE

       /* InvertedIndexHandler invertedIndexHandler = new InvertedIndexHandler();
        PostingList pl = invertedIndexHandler.getPostingList(2323*4,2);
        System.out.println("Size: "+pl.getSize());

        for(PostingElement pe: pl.getPostingList()){
            System.out.print("Doc ID: "+pe.getDocId()+" -- ");
            System.out.println("TermFreq :"+pe.getTf());
        }
*/
        /*
        int length = 10;
        LexiconHandler le = new LexiconHandler();
        ByteBuffer dataBuffer = le.findTermEntry("your");
        int offset = le.getOffset(dataBuffer);

        FileChannel file = new RandomAccessFile("data/docIds.dat","rw").getChannel();

        ByteBuffer data = ByteBuffer.allocate(4*length);
        file.read(data,offset*4);
        data.position(0);
        for(int i=1;i<length+1;i++) {
            System.out.println(data.getInt());
        }
       */




       //Test per check if file exist



        //Classe per testare funzioni
        //Test bytes
        /*
        byte[] data = "à".getBytes();
        int dataLength = data.length;
        int a= 2;
        int b =3;
        ByteBuffer DueIntBuffer = ByteBuffer.allocate(8);
        DueIntBuffer.putInt(a).putInt(b);
        DueIntBuffer.position(0);
        System.out.println("A from Buffer "+DueIntBuffer.getInt());
        System.out.println("B from Buffer "+DueIntBuffer.getInt());

        String string0 = "aiudooo";
        byte[] stringData0  = string0.getBytes();
        int stringDataLength0 = stringData0.length;
        ByteBuffer StringBuffer = ByteBuffer.allocate(stringData0.length);
        StringBuffer.put(stringData0);

        String string1 = "aiudooo1";
        byte[] stringData1  = string1.getBytes();
        int stringDataLength1 = stringData1.length;
        ByteBuffer StringBuffer1 = ByteBuffer.allocate(stringData1.length);
        StringBuffer.put(stringData1);

        String string2 = "aiudooo2";
        byte[] stringData2  = string2.getBytes();
        int stringDataLength2 = stringData2.length;
        ByteBuffer StringBuffer2 = ByteBuffer.allocate(stringData2.length);
        StringBuffer.put(stringData2);

        ArrayList<Integer> listDataLength = new ArrayList<>();
        listDataLength.add(stringDataLength0);
        listDataLength.add(stringDataLength1);
        listDataLength.add(stringDataLength2);
        for (int i=0;i<3;i++)

        StringBuffer.position(i+stringDataLength0);
        byte[] stringByte = new byte[stringDataLength0];
        StringBuffer.get(stringByte,0,stringDataLength0);
        String s = new String(stringByte, StandardCharsets.UTF_8);
        System.out.println("Stringa "+i+": "+s);

        }

    }

    public void merge() {
            //HashMap<String, PostingList> index = new HashMap<String, PostingList>();

            //File file = new File("C:/temp/test.txt");
            //byte[] bytes = new byte[(int) fiepath.length()];
            //FileInputStream fis = new FileInputStream(file);

            String filePath = "example.txt"; // Replace with your file path
            byte[] buffer = null;
            int bytesRead = 4;
            int bytesToRead = 0; // Specify the number of bytes you want to read
            int postingListLength = 0;
            int offset = 0;
            String term = null;
            buffer = new byte[bytesToRead];

            FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            //fai un array di bufferedInputStream
            for (allfiles) {  //indice senza Lexicon
                //alloca reader nell'array
                //(meglio fare un block descriptor?)
            }



            while (true) {
                String term = getMinTermFromBlockReading();

                bytesRead = bufferedInputStream.read(buffer, offset, offset + 4); //leggo il primo int
                offset++;
                term = String.valueOf(bufferedInputStream.read(buffer, offset, bytesRead + offset));
                offset += bytesRead;
                postingListLength = bufferedInputStream.read(buffer, offset, offset + 4);
            }

            while (bytesRead != -1) { //controlla se ho raggiunto la fine del file
                bytesRead = bufferedInputStream.read(buffer, offset, offset + 4); //leggo il primo int
                offset++;
                term = String.valueOf(bufferedInputStream.read(buffer, offset, bytesRead + offset));
                offset += bytesRead;
                postingListLength = bufferedInputStream.read(buffer, offset, offset + 4);
            }
        }

        public String getMinTermFromBlockReading() {
            String minTerm = "";
            int count = 0;
            for (BlockDescriptor blockDescriptor: Blocks) {
                if (blockdescriptor.term < minTerm) {
                    minTerm = blockdescriptor.term;
                    count = 0;
                }
                else if (blockDescriptor.term == minTerm)
                    count++;
            }

            if (count == 0) computePostingList(blockReader);
            else

            return "prova";
        }

        public void computePostingList(blockReader) {
            for (quanto) {
            bytesRead = bufferedInputStream.read(buffer, offset, offset + 4); //leggo il primo int
*/
        }

    }

