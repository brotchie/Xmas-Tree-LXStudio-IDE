package com.github.brotchie;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

public class TreeUtils {
    public static int[][] extractRingIndexes(LXModel model) {
        if (model.children.length == 0){
            LX.error("Model has no children");
            return new int[0][];
        }
        if (model.children[0].children.length  <= 1) {
            LX.error("Expected multiple model sub children");
            return new int[0][];
        }
        LXModel ringModels[] = model.children[0].children;
        int[][] ringIndexes = new int[ringModels.length][];
        for (int i = 0; i < ringModels.length; ++i) {
            LXModel ring = ringModels[i];
            int[] indexes = new int[ring.size];
            for (int j = 0; j < ring.points.length; ++j) {
                indexes[j] = ring.points[j].index;
            }
            ringIndexes[i] = indexes;
        }
        return ringIndexes;
    }
}
