package com.j8;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.callback.StepDataChangedCallback;
import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;

import py4j.GatewayServer;


public class debuggerExtension extends ControllerExtension {
    public Clip c;
    public CursorTrack ct;
    public CursorDevice cd;
    public CursorRemoteControlsPage rc;
    public pyClip pc;
    public String test = "hello";

    protected debuggerExtension(final debuggerExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        mTransport = host.createTransport();
        host.getMidiInPort(0).setMidiCallback((ShortMidiMessageReceivedCallback) msg -> onMidi0(msg));
        host.getMidiInPort(0).setSysexCallback((String data) -> onSysex0(data));
        this.ct = host.createCursorTrack("Robot", "Robot", 0, 1, true);
        this.ct.position().addValueObserver(msg -> printVal(msg));
        this.ct.name().markInterested();

        this.c = host.createLauncherCursorClip(8, 12);
        this.c.addStepDataObserver((i, i1, i2) -> {
            getHost().println(String.format("x: %d y: %d state: %d", i, i1, i2));
        });
        this.pc = new pyClip(this.c);
        this.ct.name().addValueObserver(this::printStr);
        this.cd = this.ct.createCursorDevice("cd", "Main", 4, CursorDeviceFollowMode.FOLLOW_SELECTION);
        this.cd.name().addValueObserver((String s) -> {
            getHost().println(String.format("device: %s", s));
        });

        this.c.getTrack().addIsSelectedInEditorObserver(msg -> derp(msg));

        this.ct.hasNext().markInterested();
        this.ct.hasPrevious().markInterested();

        int rcParameters = 8;
        this.rc = this.cd.createCursorRemoteControlsPage(8);
        rc.pageNames().markInterested();
        for (int i = 0; i < rcParameters; i++) {
            rc.getParameter(i).markInterested();
            rc.getParameter(i).name().markInterested();
        }

        // TODO: Perform your driver initialization here.
        // For now just show a popup notification for verification that it is running.
        host.showPopupNotification("debugger Initialized");
        what();
    }

    public Clip getClip() {
        return this.c;
    }

    public CursorDevice getCursorDevice() {
        return this.cd;
    }

    public CursorTrack getCursorTrack() {
        return this.ct;
    }

    public CursorRemoteControlsPage getRemote() {
        return this.rc;
    }

    void derp(boolean b) {
        getHost().println("derp " + b);
    }
    void printStr(String s) { getHost().println(s);}
    public void printVal(int s) {
        getHost().println(Integer.toString(s));
    }

    private StackEntryPoint sp;
    private GatewayServer gatewayServer;
    void what() {
        sp = new StackEntryPoint();
        gatewayServer = new GatewayServer(this);
        try {
            gatewayServer.start();
        } catch (py4j.Py4JNetworkException e) {
            getHost().println("Already running.");
        }
        System.out.println("Gateway Server Started");
    }

    @Override
    public void exit() {
        // TODO: Perform any cleanup once the driver exits
        // For now just show a popup notification for verification that it is no longer running.
        getHost().showPopupNotification("debugger Exited");
        gatewayServer.shutdown();
    }

    @Override
    public void flush() {
        // TODO Send any updates you need here.
    }

    /**
     * Called when we receive short MIDI message on port 0.
     */
    private void onMidi0(ShortMidiMessage msg) {
        final ControllerHost h = getHost();
        String msgString = msg.getChannel() + " " + msg.getStatusByte() + " " + msg.getData1() + " " + msg.getData2();
        h.println(msgString);
        sp.getStack().push(msg);
        if (msg.getChannel() == 1) {
            switch (msg.getData1()) {
                case 0:
                    this.cd.selectPrevious();
                    h.println("prev " + this.cd);
                    break;

                case 1:
                    this.cd.selectNext();
                    h.println("next " + this.cd);
                    break;

                case 2:
                    this.c.getTrack().setName("Hello");
                    break;

                case 3:
                    this.c.setName("My Clip");
                    break;
                case 4:
                    h.println(Integer.toHexString(msg.getData2()));
                    int x = (msg.getData2() & 0b11110000) >> 4;
                    int y = msg.getData2() & 0b00001111;
                    h.println("x: " + x + " y: " + y);
                    this.c.setStep(x, y, 100, 1.0/4.0 );
                    break;

                case 5:
                    getHost().println("Yo");
                    byte[] data = {(byte) msg.getData1(), (byte) msg.getData2()};
                    getHost().println(data.toString());
                    getHost().sendDatagramPacket("127.0.0.1", 20001, data);
                    break;
                case 6:
                    getHost().println("sysex");
                    getHost().getMidiOutPort(0).sendSysex("asf0123");
                    break;

                default:
                    break;
            }


            // this.c.setStep(msg.getData1(), msg.getData2(), 48, 100);
        }
        // TODO: Implement your MIDI input handling code here.
    }

    /**
     * Called when we receive sysex MIDI message on port 0.
     */
    private void onSysex0(final String data) {
        // MMC Transport Controls:
        if (data.equals("f07f7f0605f7"))
            mTransport.rewind();
        else if (data.equals("f07f7f0604f7"))
            mTransport.fastForward();
        else if (data.equals("f07f7f0601f7"))
            mTransport.stop();
        else if (data.equals("f07f7f0602f7"))
            mTransport.play();
        else if (data.equals("f07f7f0606f7"))
            mTransport.record();
    }

    private Transport mTransport;
}
