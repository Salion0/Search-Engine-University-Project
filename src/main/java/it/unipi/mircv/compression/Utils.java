package it.unipi.mircv.compression;

public class Utils {
    static public void printReverseBytes(byte[] bytesToPrint){
        bytesToPrint = reverseByteArray(bytesToPrint);
        System.out.print("bytes: " + bytesToPrint.length + "\tcode: ");
        for (byte b :bytesToPrint) {
            for (int i = 7; i >= 0; i--) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }

    static public void printBytes(byte[] bytesToPrint){
        System.out.print("bytes: " + bytesToPrint.length + "\tcode: ");
        for (byte b :bytesToPrint) {
            for (int i = 0; i < 8; i++) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }
    static public void printByte(byte b){
        System.out.print("bytes: 1" + "\tcode: ");
        for (int i = 0; i < 8; i++) {
            byte bit = (byte) ((b >> i) & 1);
            System.out.print(bit);
        }System.out.println();
    }

    public static byte[] reverseByteArray(byte[] array) {
        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = array[length - 1 - i];
        }
        return reversedArray;
    }
}