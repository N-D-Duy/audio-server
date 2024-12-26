package com.duynguyen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFileWriter {

    public static void writeWavFile(short[] audioData, int sampleRate, String filePath) throws IOException {
        File wavFile = new File(filePath);

        try (FileOutputStream fos = new FileOutputStream(wavFile)) {
            // WAV header
            fos.write("RIFF".getBytes()); // Chunk ID
            fos.write(intToByteArray(36 + audioData.length * 2)); // Chunk Size
            fos.write("WAVE".getBytes()); // Format
            fos.write("fmt ".getBytes()); // Subchunk1 ID
            fos.write(intToByteArray(16)); // Subchunk1 Size (PCM header size)
            fos.write(shortToByteArray((short) 1)); // Audio Format (1 = PCM)
            fos.write(shortToByteArray((short) 1)); // Num Channels (1 = Mono)
            fos.write(intToByteArray(sampleRate)); // Sample Rate
            fos.write(intToByteArray(sampleRate * 2)); // Byte Rate (SampleRate * NumChannels * BitsPerSample/8)
            fos.write(shortToByteArray((short) 2)); // Block Align (NumChannels * BitsPerSample/8)
            fos.write(shortToByteArray((short) 16)); // Bits per Sample

            // Data header
            fos.write("data".getBytes()); // Subchunk2 ID
            fos.write(intToByteArray(audioData.length * 2)); // Subchunk2 Size

            // Write audio data
            ByteBuffer buffer = ByteBuffer.allocate(audioData.length * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (short sample : audioData) {
                buffer.putShort(sample);
            }
            fos.write(buffer.array());
        }
    }

    private static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    private static byte[] shortToByteArray(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }
}

