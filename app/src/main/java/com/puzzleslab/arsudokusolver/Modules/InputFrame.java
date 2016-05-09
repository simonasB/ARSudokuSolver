package com.puzzleslab.arsudokusolver.Modules;

import com.puzzleslab.arsudokusolver.Modules.FramePipeline;

/**
 * Created by Simonas on 2016-04-03.
 */
public class InputFrame {
    private int nr;
    private FramePipeline framePipeline;

    public InputFrame() {
    }

    public InputFrame(int nr, FramePipeline framePipeline) {
        this.nr = nr;
        this.framePipeline = framePipeline;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public FramePipeline getFramePipeline() {
        return framePipeline;
    }

    public void setFramePipeline(FramePipeline framePipeline) {
        this.framePipeline = framePipeline;
    }
}
