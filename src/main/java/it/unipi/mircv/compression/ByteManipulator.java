package it.unipi.mircv.compression;

public class ByteManipulator {
    private byte data;

    public ByteManipulator() {
        this.data = 0; // Initialize the byte with all bits set to 0
    }

    public ByteManipulator(byte newData) {
        data = newData;
    }

    public void setBitToOne(int position) {
        if (position < 0 || position > 7) {
            throw new IllegalArgumentException("Bit position must be in the range [0, 7]");
        }
        data |= (1 << position);
    }

    public void setBitToZero(int position) {
        if (position < 0 || position > 7) {
            throw new IllegalArgumentException("Bit position must be in the range [0, 7]");
        }

        data &= ~(1 << position);
    }

    public boolean getBit(int position) {
        if (position < 0 || position > 7) {
            throw new IllegalArgumentException("Bit position must be in the range [0, 7]");
        }

        return (data & (1 << position)) != 0;
    }

    public byte getByte() {
        return data;
    }

    public void setData(byte newData) {
        data = newData;
    }

    @Override
    public String toString() {
        return String.format("%8s", Integer.toBinaryString(data & 0xFF)).replace(' ', '0');
    }
}
