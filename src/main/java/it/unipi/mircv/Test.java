package it.unipi.mircv;

import it.unipi.mircv.indexing.PostingElement;
import it.unipi.mircv.indexing.PostingList;

import java.nio.ByteBuffer;

public class Test {
    public static void main(String[] args){
        //Classe per testare funzioni
        //Test bytes
        byte[] data = "à".getBytes();
        int dataLength = data.length;
        int a= 2;
        int b =3;
        ByteBuffer DueIntBuffer = ByteBuffer.allocate(8);
        DueIntBuffer.putInt(a).putInt(b);
        DueIntBuffer.position(0);
        System.out.println("A from Buffer "+DueIntBuffer.getInt());
        System.out.println("B from Buffer "+DueIntBuffer.getInt());

        String string = "aiudooo";
        byte[] stringData  = string.getBytes();
        int stringDataLength = stringData.length;
        ByteBuffer StringBuffer = ByteBuffer.allocate(stringData.length);
        StringBuffer.put(stringData);

        StringBuffer.position(0);
        StringBuffer.get();


        System.out.println(StringBuffer.get);

    }
}

    InvertedIndex.getBytes() --> byte[]