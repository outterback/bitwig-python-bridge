package com.j8;

import com.bitwig.extension.controller.api.Clip;

public class pyClip {
    public Clip c;

    public pyClip(Clip c) {
        this.c = c;
    }

    public Clip getClip() {
        return this.c;
    }

    public void toggleStep(int x, int y, int vel) {
        this.c.toggleStep(x, y, vel);
    }

    public void up() {
        this.c.scrollKeysStepUp();
    }

    public void down() {
        this.c.scrollKeysStepDown();
    }

    public void left() {
        this.c.scrollStepsStepBackwards();
    }
    public void right() {
        this.c.scrollStepsPageForward();
    }
    public void clear_all() {
        this.c.clearSteps();
    }

}
