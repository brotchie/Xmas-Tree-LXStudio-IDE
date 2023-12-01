package com.github.brotchie.pattern;

import com.github.brotchie.TreeUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SnowPattern extends LXPattern {

    private final class Particle {
        public float offset = Float.POSITIVE_INFINITY;

        public LXVector delta;

        public LXVector start;
    }

    BoundedParameter velocity = new BoundedParameter("velocity", 1, 0, 5);
    DiscreteParameter count = new DiscreteParameter("count", 5, 1, 50);

    private final LXVector[] vectors;

    private final int[][] ringIndexes;

    private Particle[] particles;

    Random generator = new Random();

    @Override
    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == count) {
            initializeParticles();
        }
    }

    public SnowPattern(LX lx) {
        super(lx);
        addParameter("velocity", velocity);
        addParameter("count", count);

        ringIndexes = TreeUtils.extractRingIndexes(lx.getModel());
        vectors = new LXVector[lx.getModel().points.length];
        for (int i = 0; i < lx.getModel().points.length; ++i) {
            vectors[i] = new LXVector(lx.getModel().points[i]);
        }
        initializeParticles();
    }

    private void initializeParticles() {
        particles = new Particle[count.getValuei()];
        for (int i = 0; i < particles.length; i++)  {
            Particle particle = new Particle();
            particle.start = vectors[vectors.length - 1];
            LXVector end = vectors[ringIndexes[0][generator.nextInt(ringIndexes[0].length)]];
            particle.delta = end.copy().sub(particle.start);
            particle.offset = generator.nextFloat(1.0f);
            particles[i] = particle;
        }
    }

    @Override
    protected void run(double deltaMs) {
        Arrays.fill(colors, LXColor.BLACK);

        for (int i = 0; i < particles.length; ++i) {
            Particle particle = particles[i];
            if (particle.offset >= 1.0f) {
                particle.start = vectors[vectors.length - 1];
                LXVector end = vectors[ringIndexes[0][generator.nextInt(ringIndexes[0].length)]];
                particle.delta = end.copy().sub(particle.start);
                particle.offset = 0;
            }
            LXVector current = particle.start.copy().add(particle.delta.copy().mult(particle.offset));
            for (int j = 0; j < vectors.length; ++j) {
                float distance = current.dist(vectors[j]);
                colors[j] = LXColor.add(colors[j], LXColor.grayn(1 / Math.pow(distance, 3)));
            }
            particle.offset += (velocity.getValuef() / 10) * deltaMs / 1000.0;
        }
    }
}
