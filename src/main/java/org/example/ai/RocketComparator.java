package org.example.ai;

import java.util.Comparator;

public class RocketComparator implements Comparator<Rocket> {
    @Override
    public int compare(Rocket a, Rocket b) {
        return Float.compare(b.fitness, a.fitness); // note: b, a reversed
    }
}