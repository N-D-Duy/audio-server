package com.duynguyen;

public class AudioDataConverter {
    // Chuyển từ byte array sang signed 16-bit
    public static short[] bytesToShort(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) ((bytes[i * 2] & 0xFF) | (bytes[i * 2 + 1] << 8));
        }
        return shorts;
    }

    // Chuyển từ unsigned 16-bit sang signed 16-bit
    public static short unsignedToSigned(int unsignedValue) {
        // Chuyển từ unsigned (0-65535) sang signed (-32768 to 32767)
        return (short) (unsignedValue >= 32768 ? unsignedValue - 65536 : unsignedValue);
    }

    // Chuyển signed 16-bit thành byte array
    public static byte[] shortToBytes(short value) {
        return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
    }
}
