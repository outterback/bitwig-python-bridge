package com.j8;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Stack {
    private ConcurrentLinkedQueue<ShortMidiMessage> internalQueue = new ConcurrentLinkedQueue<>();
    private List<ShortMidiMessage> internalList = new LinkedList<>();

    public void push(ShortMidiMessage element) {
        internalQueue.add(element);
        //internalList.add(0, element);
    }

    public ShortMidiMessage pop() {
        return internalQueue.poll();
        // return internalList.remove(0);
    }


    public List<ShortMidiMessage> getInternalList() {
        return internalList;
    }

    public void pushAll(List<ShortMidiMessage> elements) {
        for (ShortMidiMessage element : elements) {
            this.push(element);
        }
    }

    public boolean hasElement() {
        return !internalList.isEmpty();
    }

    public void reinitialize() {
        this.internalList = new LinkedList<ShortMidiMessage>();
    }

}
