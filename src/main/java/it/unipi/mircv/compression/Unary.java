package it.unipi.mircv.compression;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Unary {
    public static byte[] compress(int[] intArray) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        for (int i = 0; i<intArray.length; i++){

        }
        //int numBit = value/8;
        //if (value%8 != 0) numBit++;

        
        ByteManipulator byteManipulator = new ByteManipulator();

        return byteArrayOutputStream.toByteArray();
    }
}
