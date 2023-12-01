package com.github.brotchie.pattern;

import com.github.brotchie.TreeUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.ArrayList;
import java.util.List;

public class TreeRingPattern extends LXPattern {

    public final CompoundParameter ring = new CompoundParameter("ring", 0, 1);

    private final int[][] ringIndexes;

    public TreeRingPattern(LX lx) {
        super(lx);
        ring.setMappable(true);
        addParameter("ring", ring);
        LX.log(String.format("%d %d %d", lx.getModel().size, lx.getModel().children.length, lx.getModel().children[0].children.length));
        ringIndexes = TreeUtils.extractRingIndexes(lx.getModel());
    }

    @Override
    protected void run(double deltaMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = LXColor.BLACK;
        }

        double scaledIndex = (ringIndexes.length + 1) * ring.getValue();
        int ringIndex = Math.min((int)Math.round(Math.floor(scaledIndex)), ringIndexes.length);

        double brightness = scaledIndex - ringIndex;

        if (ringIndex >= 1) {
            for (int index : ringIndexes[ringIndex - 1]) {
                colors[index] = LXColor.grayn(1 - brightness);
            }
        }
        if (ringIndex >= 0 && ringIndex < ringIndexes.length) {
            for (int index : ringIndexes[ringIndex]) {
                colors[index] = LXColor.grayn(brightness);
            }
        }
    }
}
