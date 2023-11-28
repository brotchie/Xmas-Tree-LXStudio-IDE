package com.github.brotchie;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.IndexBuffer;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StarpusherOutputGroup extends LXOutputGroup implements LXOutput.InetOutput {
    /** A Starpusher v3 has 8 LED output ports. */
    private final int MAX_LED_PORT = 8;

    private final List<StarpusherDatagram> datagrams = new ArrayList<>();
    private InetAddress deviceAddress = null;
    private int udpPort = NO_PORT;
    public StarpusherOutputGroup(LX lx, LXModel model, int ledsPerLedPort) throws StarpusherOutputGroupException {
        super(lx);
        buildOutputs(lx, model, ledsPerLedPort);
    }

    /** Maps a LXModel linearly onto Starpusher LED ports, maximizing the size of each UDP packet. */
    void buildOutputs(LX lx, LXModel model, final int ledsPerLedPort) throws StarpusherOutputGroupException {
        final int[] indexBuffer = model.toIndexBuffer();
        int currentLedPort = 1;
        int currentLedPortIndex = 0;
        int currentIndex = 0;

        while (currentIndex < indexBuffer.length) {
            if (currentLedPort > MAX_LED_PORT) {
                throw new StarpusherOutputGroupException("Not enough led ports for model");
            }
            // Next datagram is the minimum of:
            //  1. Maximum LEDs that will fit in a datagram,
            //  2. Remaining LEDs available on the current LED port,
            //  3. Remaining points in the LXModel.
            int datagramLedCount = Math.min(Math.min(StarpusherDatagram.MAX_LEDS_PER_DATAGRAM, ledsPerLedPort - currentLedPortIndex), indexBuffer.length - currentIndex);
            int[] datagramIndexBuffer = Arrays.copyOfRange(indexBuffer, currentIndex, currentIndex + datagramLedCount);

            StarpusherDatagram datagram;
            try {
                datagram = new StarpusherDatagram(lx, new IndexBuffer(datagramIndexBuffer), currentLedPort, currentLedPortIndex);
            } catch (StarpusherDatagram.InvalidIndexBufferException error) {
                throw new StarpusherOutputGroupException(error);
            }

            datagrams.add(datagram);
            addChild(datagram);

            currentIndex += datagramLedCount;
            currentLedPortIndex += datagramLedCount;

            if (currentLedPortIndex == ledsPerLedPort) {
                currentLedPort += 1;
                currentLedPortIndex = 0;
            }
        }
    }

    @Override
    public InetOutput setAddress(InetAddress address) {
        this.deviceAddress = address;
        for (StarpusherDatagram datagram : this.datagrams) {
            datagram.setAddress(address);
        }
        return this;
    }

    @Override
    public InetAddress getAddress() {
        return this.deviceAddress;
    }

    @Override
    public InetOutput setPort(int port) {
        this.udpPort = port;
        for (StarpusherDatagram datagram : this.datagrams) {
            datagram.setPort(port);
        }
        return this;
    }

    @Override
    public int getPort() {
        return this.udpPort;
    }

    public static final class StarpusherOutputGroupException extends Exception {
        public StarpusherOutputGroupException(Exception thrown) {
            super(thrown);
        }

        public StarpusherOutputGroupException(String message) {
            super(message);
        }
    }
}
