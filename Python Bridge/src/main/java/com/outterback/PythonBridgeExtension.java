package com.outterback;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;
import py4j.GatewayServer;

import java.util.ArrayList;

public class PythonBridgeExtension extends ControllerExtension {

    // Objects we want to expose to Python, accessed via getters found below.
    private Clip c;
    private CursorTrack ct;
    private CursorDevice cd;
    private CursorRemoteControlsPage rc;
    private Transport transport;

    // The object that bridges Java to Python
    private GatewayServer gatewayServer;

    protected PythonBridgeExtension(final PythonBridgeExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    private ArrayList<Object> toMarkInterested = new ArrayList<>();

    @Override
    public void init() {
        final ControllerHost host = getHost();

        // Setting up API objects that we want to access from Python
        this.transport = host.createTransport();
        this.ct = host.createCursorTrack("PyBridge", "PyBridge", 0, 1, true);
        this.ct.position().addValueObserver((msg) -> getHost().println(Integer.toString(msg)));
        this.ct.name().markInterested();

        this.c = host.createLauncherCursorClip(8, 12);
        this.c.addStepDataObserver((i, i1, i2) -> getHost().println(String.format("x: %d y: %d state: %d", i, i1, i2)));
        this.ct.name().addValueObserver((msg) -> getHost().println(msg));
        this.cd = this.ct.createCursorDevice("cd", "Main", 4, CursorDeviceFollowMode.FOLLOW_SELECTION);
        this.cd.name().addValueObserver((String s) -> getHost().println(String.format("device: %s", s)));


        this.ct.hasNext().markInterested();
        this.ct.hasPrevious().markInterested();

        int rcParameters = 8;
        this.rc = this.cd.createCursorRemoteControlsPage(8);
        rc.pageNames().markInterested();

        for (int i = 0; i < rcParameters; i++) {
            rc.getParameter(i).markInterested();
            rc.getParameter(i).name().markInterested();
        }

        this.toMarkInterested.add(this.transport.tempo());
        this.toMarkInterested.add(this.transport.tempo().displayedValue());
        for (Object o: toMarkInterested
             ) {
            ((Value) o).markInterested();
        }

        initGateway();

        host.showPopupNotification("Python Bridge Initialized");
    }

    // Getters are needed, even though the member variables are exposed in Python.
    // If you ask for the members directly, you will not get the methods of that member. Asking for the members
    // via getters seem to solve this.
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

    public Transport getTransport() {
        return this.transport;
    }

    // Initialize the GatewayServer with a pointer to this class. This could be refactored, but I wanted to keep
    // the number of classes down.
    void initGateway() {
        gatewayServer = new GatewayServer(this);

        // This part is not thoroughly tested.
        try {
            gatewayServer.start();
            getHost().println("Gateway Server Started");
        } catch (py4j.Py4JNetworkException e) {
            getHost().println("Gateway Server already running.");
        }
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
