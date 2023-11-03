package it.unipi.mircv.Index;

import java.io.ByteArrayOutputStream;

public class Compression {

    //this method works with the assumption that value is a positive int
    public static byte[] variableByteCompression(int value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (value > 0) {
            byte tempByte = (byte) (value & 0x7F);

            value >>>= 7;
            if (value > 0) {
                tempByte |= 0x80;
            }

            byteArrayOutputStream.write(tempByte);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static int variableByteDecompression(byte[] bytes) {
        int result = 0;
        int shift = 0;
        for (byte b : bytes) {
            result |= (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return result;
    }

    public static byte[] reverseArray(byte[] array) {
        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = array[length - 1 - i];
        }
        return reversedArray;
    }

    static public void printBytes(byte[] bytesToPrint){
        System.out.print("bytes: " + bytesToPrint.length + "\t");
        for (byte b :bytesToPrint) {
            for (int i = 7; i >= 0; i--) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }
}
