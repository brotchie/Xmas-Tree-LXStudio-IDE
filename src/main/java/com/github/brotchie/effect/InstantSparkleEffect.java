package com.github.brotchie.effect;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;

public class InstantSparkleEffect extends LXEffect {

    public final BoundedParameter probability = new BoundedParameter("probability", 0.5, 0, 100);

    public InstantSparkleEffect(LX lx) {
        super(lx);
        addParameter("probability", probability);
    }

    @Override
    protected void run(double deltaMs, double enabledAmount) {
        double p = probability.getValue() / 100;
        for (int i = 0; i < colors.length; ++i) {
            if (Math.random() < p) {
                colors[i] = LXColor.WHITE;
            }
        }
    }
}
