package it.unipi.mircv;

import it.unipi.mircv.indexing.PostingElement;
import it.unipi.mircv.indexing.PostingList;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Test{
    public static void main(String[] args){
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
*/
        }


    }
