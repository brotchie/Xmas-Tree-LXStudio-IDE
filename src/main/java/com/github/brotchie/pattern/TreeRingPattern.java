package com.github.brotchie.pattern;

import com.github.brotchie.TreeUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.ArrayList;
import java.util.List;

public class TreeRingPattern extends LXPattern {

    public final CompoundParameter ring = new CompoundParameter("ring", 0, 1);

    public final BooleanParameter snap = new BooleanParameter("snap", false);

    public final DiscreteParameter height = new DiscreteParameter("height", 1, 0, 1);

    public final DiscreteParameter skip = new DiscreteParameter("skip", 0, 0, 1);

    private final int[][] ringIndexes;

    public TreeRingPattern(LX lx) {
        super(lx);
        ring.setMappable(true);
        addParameter("ring", ring);
        addParameter("snap", snap);
        addParameter("height", height);
        addParameter("skip", skip);

        LX.log(String.format("%d %d %d", lx.getModel().size, lx.getModel().children.length, lx.getModel().children[0].children.length));
        ringIndexes = TreeUtils.extractRingIndexes(lx.getModel());

        height.setRange(1, ringIndexes.length);
        height.setValue(1);
        skip.setRange(1, ringIndexes.length);
        skip.setValue(1);
    }

    @Override
    protected void run(double deltaMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = LXColor.BLACK;
        }

        double scaledIndex = (ringIndexes.length + 1) * ring.getValue();
        int ringIndex = Math.min((int) Math.round(Math.floor(scaledIndex)), ringIndexes.length);

        double brightness = scaledIndex - ringIndex;

        if (snap.getValueb()) {
            brightness = 1.0;
        }

        if (ringIndex >= 1) {
            for (int z = 0; z < height.getValuei(); z += skip.getValuei()) {
                for (int index : ringIndexes[(z + ringIndex - 1) % ringIndexes.length]) {
                    colors[index] = LXColor.grayn(1 - brightness);
                }
            }

        }
        if (ringIndex >= 0 && ringIndex < ringIndexes.length) {
            for (int z = 0; z < height.getValuei(); z += skip.getValuei()) {

                for (int index : ringIndexes[(z + ringIndex) % ringIndexes.length]) {
                    colors[index] = LXColor.grayn(brightness);
                }
            }
        }
    }
}
