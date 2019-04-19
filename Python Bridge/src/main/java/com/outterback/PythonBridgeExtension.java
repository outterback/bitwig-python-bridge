package com.outterback;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;
import py4j.GatewayServer;

public class PythonBridgeExtension extends ControllerExtension {

    public Clip c;
    public CursorTrack ct;
    public CursorDevice cd;
    public CursorRemoteControlsPage rc;
    public Transport transport;

    protected PythonBridgeExtension(final PythonBridgeExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        transport = host.createTransport();
        this.ct = host.createCursorTrack("PyBridge", "PyBridge", 0, 1, true);
        this.ct.position().addValueObserver(msg -> printVal(msg));
        this.ct.name().markInterested();

        this.c = host.createLauncherCursorClip(8, 12);
        this.c.addStepDataObserver((i, i1, i2) -> {
            getHost().println(String.format("x: %d y: %d state: %d", i, i1, i2));
        });
        this.ct.name().addValueObserver(this::printStr);
        this.cd = this.ct.createCursorDevice("cd", "Main", 4, CursorDeviceFollowMode.FOLLOW_SELECTION);
        this.cd.name().addValueObserver((String s) -> {
            getHost().println(String.format("device: %s", s));
        });


        this.ct.hasNext().markInterested();
        this.ct.hasPrevious().markInterested();

        int rcParameters = 8;
        this.rc = this.cd.createCursorRemoteControlsPage(8);
        rc.pageNames().markInterested();
        for (int i = 0; i < rcParameters; i++) {
            rc.getParameter(i).markInterested();
            rc.getParameter(i).name().markInterested();
        }

        host.showPopupNotification("debugger Initialized");
        initGateway();

        // TODO: Perform your driver initialization here.
        // For now just show a popup notification for verification that it is running.
        host.showPopupNotification("Python Bridge Initialized");
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

    void printStr(String s) {
        getHost().println(s);
    }

    public void printVal(int s) {
        getHost().println(Integer.toString(s));
    }

    private GatewayServer gatewayServer;

    void initGateway() {
        gatewayServer = new GatewayServer(this);

        // This part is thoroughly tested
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
        gatewayServer.shutdown();
        getHost().showPopupNotification("Python Bridge Exited");
    }

    @Override
    public void flush() {
        // TODO Send any updates you need here.
    }


}
