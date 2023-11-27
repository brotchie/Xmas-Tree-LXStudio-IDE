package com.github.brotchie;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.IndexBuffer;
import heronarts.lx.output.LXDatagram;

import java.nio.ByteBuffer;

public class StarpusherOutput extends LXDatagram {
    private static final double WS2811_GAMMA_CORRECTION = 2.8;
    private static final int PREAMBLE_SIZE = 7;
    private static final int BYTES_PER_LED = 3;
    private static final int LED_PORT = 1;

    public StarpusherOutput(LX lx, LXModel model) {
        super(lx, new IndexBuffer(model.toIndexBuffer()), calculateDatagramSize(model.size));
        addDatagramPreamble(buffer, model.size);
        configureGammaCorrection();
    }

    void configureGammaCorrection() {
        gamma.setValue(WS2811_GAMMA_CORRECTION);
        gammaMode.setValue(GammaMode.DIRECT);
    }

    static int calculateDatagramSize(int ledCount) {
        return PREAMBLE_SIZE + BYTES_PER_LED * ledCount;
    }

    static void addDatagramPreamble(byte[] buffer, int ledCount) {
        ByteBuffer bytes = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        bytes.put(0, (byte)'*');
        bytes.put(1, (byte)'P');
        bytes.put(2, (byte)LED_PORT);
        bytes.putShort(3, (short)20);
        bytes.putShort(5, (short)ledCount);
    }

    @Override
    public int getDataBufferOffset() {
        // Start writing raw RGB values after the UDP packet preamble.
        return PREAMBLE_SIZE;
    }
}
