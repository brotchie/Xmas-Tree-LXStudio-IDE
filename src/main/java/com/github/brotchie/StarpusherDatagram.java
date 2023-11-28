package com.github.brotchie;

import heronarts.lx.LX;
import heronarts.lx.output.IndexBuffer;
import heronarts.lx.output.LXDatagram;

import java.nio.ByteBuffer;

class StarpusherDatagram extends LXDatagram {
    /** Gamma correction factor to make WS2811 LEDs look good across their range. */
    private static final double WS2811_GAMMA_CORRECTION = 2.8;

    /** Size of the Starpusher LED update packet preamble. */
    private static final int PREAMBLE_SIZE = 7;

    /** Number of bytes per LED in the update packet (RGB byte ordering). */
    private static final int BYTES_PER_LED = 3;

    /** Maximum UDP packet size that the Starpusher accepts. */
    private static final int MAX_DATAGRAM_SIZE = 1472;

    /** Maximum number of LEDs that can be updated in one update packet. */
    public static final int MAX_LEDS_PER_DATAGRAM = (MAX_DATAGRAM_SIZE - PREAMBLE_SIZE) / BYTES_PER_LED;

    public static class InvalidIndexBufferException extends Exception {
        protected InvalidIndexBufferException(String message) {
            super(message);
        }
    }

    public StarpusherDatagram(LX lx, IndexBuffer indexBuffer, int ledPort, int startIndex) throws InvalidIndexBufferException {
        super(lx, validateIndexBuffer(indexBuffer), calculateDatagramSize(indexBuffer));
        addDatagramPreamble(buffer, indexBuffer, ledPort, startIndex);
        configureGammaCorrection();
    }

    void configureGammaCorrection() {
        gamma.setValue(WS2811_GAMMA_CORRECTION);
        gammaMode.setValue(GammaMode.DIRECT);
    }

    /** Ensures the given IndexBuffer has a single segment, RGB byte ordering, and that it doesn't exceed
     * the Starpusher's maximum UDP packet size. */
    static IndexBuffer validateIndexBuffer(IndexBuffer indexBuffer) throws InvalidIndexBufferException {
        if (indexBuffer.segments.length != 1) {
            throw new InvalidIndexBufferException("StarpusherDatagram Index buffer must have exactly one segment");
        }
        if (indexBuffer.segments[0].byteOrder != ByteOrder.RGB) {
            throw new InvalidIndexBufferException("StarpusherDatagram Index buffer is " + indexBuffer.segments[0].byteOrder + ", must be RGB");
        }
        final int datagramSize = calculateDatagramSize(indexBuffer);
        if (datagramSize > MAX_DATAGRAM_SIZE) {
            throw new InvalidIndexBufferException("StarpusherDatagram Index buffer is " + datagramSize + ", must be <= " + MAX_DATAGRAM_SIZE);
        }
        return indexBuffer;
    }

    /** Calculate the size, in bytes, of a Starpusher LED update packet for the given IndexBuffer. */
    static int calculateDatagramSize(IndexBuffer indexBuffer) {
        return PREAMBLE_SIZE + BYTES_PER_LED * indexBuffer.segments[0].indices.length;
    }

    static void addDatagramPreamble(byte[] datagramBuffer, IndexBuffer indexBuffer, int ledPort, int startIndex) {
        final IndexBuffer.Segment segment = indexBuffer.segments[0];

        ByteBuffer bytes = ByteBuffer.wrap(datagramBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        bytes.put(0, (byte) '*');
        bytes.put(1, (byte) 'P');
        bytes.put(2, (byte) ledPort);
        bytes.putShort(3, (short) startIndex);
        bytes.putShort(5, (short) segment.indices.length);
        LX.log(String.format("StarpusherDatagram Initialized LED Port: %d Start Index: %d LED Count: %d Model Index Range: [%d, %d]", ledPort, startIndex, segment.indices.length, segment.indices[0], segment.indices[segment.indices.length - 1]));
    }

    @Override
    public int getDataBufferOffset() {
        // Start writing raw RGB values after the UDP packet preamble.
        return PREAMBLE_SIZE;
    }
}
