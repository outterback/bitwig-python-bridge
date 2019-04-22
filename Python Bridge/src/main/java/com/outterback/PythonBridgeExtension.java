package com.outterback;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;
import py4j.GatewayServer;

import java.util.ArrayList;

public class PythonBridgeExtension extends ControllerExtension {

    // Objects we want to expose to Python, accessed via getters found below.
    private Clip c;
    private CursorTrack selectionCt;
    private CursorTrack ct;
    private CursorDevice cd;
    private CursorRemoteControlsPage rc;
    private Transport transport;
    private TrackBank tb;
    private SceneBank sb;

    private ArrayList<CursorTrack> cts = new ArrayList<>();
    private ArrayList<String> namesChanged = new ArrayList<>();
    private ArrayList<String> clipsTriggered = new ArrayList<>();

    int[][] clipContent;

    public static final int clipWidth = 1024;
    public static final int clipHeight = 12;
    private static final int numCursorTracks = 16;
    public static final int tbTracks = 512;
    public static final int tbSends = 64;

    // The object that bridges Java to Python
    private GatewayServer gatewayServer;

    protected PythonBridgeExtension(final PythonBridgeExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    private ArrayList<Object> toMarkInterested = new ArrayList<>();

    public ArrayList<String> getNamesChanged() {
        return this.namesChanged;
    }

    public ArrayList<String> getClipsTriggered() {
        return this.clipsTriggered;
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();
        clipContent = new int[clipHeight][];
        for (int i = 0; i < clipHeight; i++) {
            clipContent[i] = new int[clipWidth];
            for (int j = 0; j < clipWidth; j++) {
                clipContent[i][j] = 0;
            }
        }

        // Setting up API objects that we want to access from Python
        this.transport = host.createTransport();
        this.ct = host.createCursorTrack("PyBridge", "PyBridge", 0, 1, false);
        this.ct.position().addValueObserver((msg) -> getHost().println(Integer.toString(msg)));
        this.ct.name().markInterested();

        this.tb = host.createMainTrackBank(tbTracks, tbSends, 64);
        this.tb.channelCount().markInterested();
        this.tb.scrollPosition().markInterested();
        this.tb.canScrollBackwards().markInterested();
        this.tb.canScrollForwards().markInterested();


        for (int i = 0; i < tbTracks; i++) {
            Track t_i = this.tb.getItemAt(i);
            t_i.name().markInterested();
            t_i.clipLauncherSlotBank().addNameObserver((index, name) -> {
                getHost().println(String.format("track: %s i: %d n: %s", t_i.name().get(), index, name));
                namesChanged.add(name);
            });
            t_i.clipLauncherSlotBank().addIsPlayingObserver((index, playing) -> {
                getHost().println(String.format("clip name: %s %s", t_i.clipLauncherSlotBank().getItemAt(index).name().get(), playing));
                if (playing) {
                    clipsTriggered.add(t_i.clipLauncherSlotBank().getItemAt(index).name().get());
                }
                
            });

        }
        this.sb = this.tb.sceneBank();
        this.sb.addNameObserver((index, name) -> getHost().println(String.format("i%d n: %s", index, name)));


        for (int i = 0; i < numCursorTracks; i++) {
            String id = String.format("map_%d", i);
            CursorTrack cursor = host.createCursorTrack(id, id, 0, 8, false);
            cts.add(cursor);
        }


        this.selectionCt = host.createCursorTrack("PyBridge_sel", "PyBridge_sel", 0, 1, true);
        this.selectionCt.position().addValueObserver((msg) -> getHost().println(Integer.toString(msg)));
        this.selectionCt.name().markInterested();


        this.c = host.createLauncherCursorClip(clipWidth, clipHeight);
        this.c.addStepDataObserver((i, i1, i2) -> {
            clipContent[i1][i] = i2;
            getHost().println(String.format("x: %d y: %d state: %d", i, i1, i2));
        });


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

        c.getPlayStart().markInterested();
        c.getPlayStop().markInterested();
        c.setStepSize(4.0 / 16.0);

        for (Object o : toMarkInterested
        ) {
            ((Value) o).markInterested();
        }

        initGateway();

        host.showPopupNotification("Python Bridge Initialized");
    }

    // Getters are needed, even though the member variables are exposed in Python.
    // If you ask for the members directly, you will not get the methods of that member. Asking for the members
    // via getters seem to solve this.

    public TrackBank getMainTrackBank() {
        return this.tb;
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

    public CursorTrack getFollowCursorTrack() {
        return this.selectionCt;
    }

    public Transport getTransport() {
        return this.transport;
    }

    public int[][] getClipContent() {
        return this.clipContent;
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
