package com.github.brotchie.pattern;

import com.github.brotchie.TreeUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class FirePattern extends LXPattern {

    BoundedParameter spawnRate = new BoundedParameter("spawnRate", 0.5, 1, 100);
    private final int[][] ringIndexes;
    public FirePattern(LX lx) {
        super(lx);
        addParameter("spawnRate", spawnRate);
        ringIndexes = TreeUtils.extractRingIndexes(lx.getModel());
    }

    @Override
    protected void run(double v) {
        for (int i = 0; i < ringIndexes[0].length; i++) {
            if (Math.random() < (spawnRate.getValue() / 100.0)) {
                colors[ringIndexes[0][i]] = LXColor.WHITE;
            }
        }
        for (int i = 1; i < ringIndexes.length; i++) {

        }
    }
}
