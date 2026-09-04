package org.example;

import java.util.Random;

/**
 * Procedural noise generator for planet texture generation.
 * Provides value noise, fractal (multi-octave) noise, and Worley/cellular
 * noise (useful for crater-like patterns on moons/barren worlds).
 */
public class NoiseGenerator {

    private final int[] perm = new int[512];
    private final float[][] worleyPoints;
    private final int worleyGridSize;

    public NoiseGenerator(long seed) {
        this(seed, 12); // default 12x12 worley cell grid
    }

    public NoiseGenerator(long seed, int worleyGridSize) {
        Random rand = new Random(seed);

        // Build permutation table for value noise
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];

        // Build random feature points for Worley noise, one per grid cell
        this.worleyGridSize = worleyGridSize;
        worleyPoints = new float[worleyGridSize * worleyGridSize][2];
        for (int i = 0; i < worleyPoints.length; i++) {
            worleyPoints[i][0] = rand.nextFloat();
            worleyPoints[i][1] = rand.nextFloat();
        }
    }

    // ---------- Value noise ----------

    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 7;
        float u = h < 4 ? x : y;
        float v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /** Single-octave 2D noise, returns roughly -1..1 */
    public float noise(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);

        float u = fade(x);
        float v = fade(y);

        int aa = perm[perm[X] + Y];
        int ab = perm[perm[X] + Y + 1];
        int ba = perm[perm[X + 1] + Y];
        int bb = perm[perm[X + 1] + Y + 1];

        float x1 = lerp(u, grad(aa, x, y), grad(ba, x - 1, y));
        float x2 = lerp(u, grad(ab, x, y - 1), grad(bb, x - 1, y - 1));

        return lerp(v, x1, x2);
    }

    /** Fractal (multi-octave) noise, normalized to roughly -1..1 */
    public float fractalNoise(float x, float y, int octaves, float persistence) {
        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }

        return total / maxValue;
    }

    // ---------- Worley / Cellular noise (for craters) ----------

    /**
     * Worley noise: distance from (x, y) in [0,1] range (wraps/tiles) to the
     * nearest random feature point. Returns the distance to the closest point
     * (F1) by default. Great for crater fields, cracked ground, cell patterns.
     *
     * @param x normalized x coordinate, 0..1 (wraps)
     * @param y normalized y coordinate, 0..1 (wraps)
     */
    public float worleyNoise(float x, float y) {
        return worleyNoise(x, y, 1)[0];
    }

    /**
     * Returns the distances to the closest N feature points (F1, F2, ...).
     * F2 - F1 is useful for crisp crater rims; F1 alone gives soft craters.
     */
    public float[] worleyNoise(float x, float y, int numClosest) {
        x = x - (float) Math.floor(x); // wrap into 0..1
        y = y - (float) Math.floor(y);

        float[] closest = new float[numClosest];
        for (int i = 0; i < numClosest; i++) closest[i] = Float.MAX_VALUE;

        int cellX = (int) (x * worleyGridSize);
        int cellY = (int) (y * worleyGridSize);

        // check the 3x3 neighboring cells (with wraparound) for feature points
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int cx = ((cellX + dx) % worleyGridSize + worleyGridSize) % worleyGridSize;
                int cy = ((cellY + dy) % worleyGridSize + worleyGridSize) % worleyGridSize;
                int idx = cy * worleyGridSize + cx;

                float px = (cx + worleyPoints[idx][0]) / worleyGridSize;
                float py = (cy + worleyPoints[idx][1]) / worleyGridSize;

                // account for wraparound distance
                float ddx = wrapDelta(x - px);
                float ddy = wrapDelta(y - py);
                float dist = (float) Math.sqrt(ddx * ddx + ddy * ddy);

                // insertion into sorted "closest" array
                for (int i = 0; i < numClosest; i++) {
                    if (dist < closest[i]) {
                        for (int j = numClosest - 1; j > i; j--) closest[j] = closest[j - 1];
                        closest[i] = dist;
                        break;
                    }
                }
            }
        }
        return closest;
    }

    private float wrapDelta(float d) {
        if (d > 0.5f) d -= 1f;
        if (d < -0.5f) d += 1f;
        return d;
    }

    /**
     * Crater-style noise: combines F1 and F2 Worley distances to produce
     * round crater basins with slightly raised rims. Returns roughly -1..1,
     * negative = crater floor, near zero = rim, positive = untouched terrain.
     */
    public float craterNoise(float x, float y) {
        float[] f = worleyNoise(x, y, 2);
        float f1 = f[0];
        float f2 = f[1];
        float rim = f2 - f1; // sharpens near cell borders (rims)
        return (rim * 2f) - 1f;
    }
}