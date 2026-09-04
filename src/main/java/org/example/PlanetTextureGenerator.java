package org.example;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Generates fully procedural (no texture assets needed) materials for
 * different planet types using NoiseGenerator. Uses Common/MatDefs/Light/Lighting.j3md
 * so make sure at least one DirectionalLight + AmbientLight exist in the scene.
 */
public class PlanetTextureGenerator {



    private static final int WIDTH = 512;
    private static final int HEIGHT = 256;

    private final AssetManager assetManager;

    public PlanetTextureGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Builds a ready-to-use Material for the given planet type using a
     * randomly seeded noise generator. Pass a seed for reproducible results.
     */
    public Material generatePlanetMaterial(PlanetType type, long seed) {
        Random rand = new Random(seed);
        NoiseGenerator noise = new NoiseGenerator(seed);

        // Randomize generation parameters per-planet so same type still varies
        float scale = 40f + rand.nextFloat() * 80f;
        int octaves = 3 + rand.nextInt(4);
        float persistence = 0.35f + rand.nextFloat() * 0.3f;

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        switch (type) {
            case EARTH_LIKE:
                buildEarthLike(mat, noise, scale, octaves, persistence);
                break;
            case GAS_GIANT:
                buildGasGiant(mat, noise, octaves, persistence, rand);
                break;
            case LAVA:
                buildLava(mat, noise, scale, octaves, persistence);
                break;
            case ICE:
                buildIce(mat, noise, scale, octaves, persistence);
                break;
            case DESERT:
                buildDesert(mat, noise, scale, octaves, persistence);
                break;
            case MOON:
                buildMoon(mat, noise);
                break;
            case STAR:
                buildStar(mat, noise, octaves, persistence, rand);
                break;

        }

        return mat;
    }

    // ---------- Earth-like ----------

    private void buildEarthLike(Material mat, NoiseGenerator noise, float scale, int octaves, float persistence) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float nx = x / scale;
                float ny = y / scale;
                float value = noise.fractalNoise(nx, ny, octaves, persistence);

                int rgb;
                if (value < -0.1f) {
                    rgb = rgb(20, 60, 140);   // ocean
                } else if (value < 0.05f) {
                    rgb = rgb(50, 110, 60);   // coast/lowland
                } else if (value < 0.3f) {
                    rgb = rgb(90, 130, 70);   // plains
                } else {
                    rgb = rgb(140, 140, 140); // mountains
                }
                img.setRGB(x, y, rgb);
            }
        }

        applyDiffuse(mat, img);
        mat.setFloat("Shininess", 12f);
        mat.setColor("Specular", new ColorRGBA(0.3f, 0.3f, 0.35f, 1f));
    }

    // ---------- Gas giant ----------

    private void buildGasGiant(Material mat, NoiseGenerator noise, int octaves, float persistence, Random rand) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // pick a palette family randomly: warm (Jupiter-like) or cool (Neptune-like)
        boolean warmPalette = rand.nextBoolean();

        for (int y = 0; y < HEIGHT; y++) {
            float bandNoise = noise.fractalNoise(0, y / 20f, 3, 0.5f);
            for (int x = 0; x < WIDTH; x++) {
                float turbulence = noise.fractalNoise(x / 200f, y / 20f, octaves, persistence) * 0.15f;
                float band = (y / (float) HEIGHT) + bandNoise * 0.05f + turbulence;

                int rgb = warmPalette ? warmBandColor(band) : coolBandColor(band);
                img.setRGB(x, y, rgb);
            }
        }

        applyDiffuse(mat, img);
        mat.setFloat("Shininess", 2f); // matte, gassy - no sharp specular
        mat.setColor("Specular", ColorRGBA.Black);
    }

    private int warmBandColor(float t) {
        t = wrap01(t);
        // cycle through tan / cream / rust bands
        float phase = (t * 8f) % 1f;
        if (phase < 0.33f) return rgb(210, 180, 140);
        if (phase < 0.66f) return rgb(235, 220, 190);
        return rgb(170, 110, 70);
    }

    private int coolBandColor(float t) {
        t = wrap01(t);
        float phase = (t * 8f) % 1f;
        if (phase < 0.33f) return rgb(90, 140, 210);
        if (phase < 0.66f) return rgb(140, 180, 230);
        return rgb(60, 100, 170);
    }

    private float wrap01(float t) {
        t = t % 1f;
        if (t < 0) t += 1f;
        return t;
    }

    // ---------- Lava ----------

    private void buildLava(Material mat, NoiseGenerator noise, float scale, int octaves, float persistence) {
        BufferedImage diffuseImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        BufferedImage emissiveImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float nx = x / scale;
                float ny = y / scale;
                float value = noise.fractalNoise(nx, ny, octaves, persistence);

                int diffuseRgb;
                int emissiveRgb;
                if (value < -0.1f) {
                    diffuseRgb = rgb(255, 140, 0);  // glowing lava
                    emissiveRgb = rgb(255, 120, 0);
                } else if (value < 0.1f) {
                    diffuseRgb = rgb(80, 20, 10);   // cooling crust
                    emissiveRgb = rgb(60, 15, 0);
                } else {
                    diffuseRgb = rgb(40, 40, 40);   // dark rock
                    emissiveRgb = rgb(0, 0, 0);
                }
                diffuseImg.setRGB(x, y, diffuseRgb);
                emissiveImg.setRGB(x, y, emissiveRgb);
            }
        }

        applyDiffuse(mat, diffuseImg);
        Texture2D emissiveTex = toTexture(emissiveImg);
        mat.setTexture("GlowMap", emissiveTex);
        mat.setColor("GlowColor", ColorRGBA.Orange);
        mat.setFloat("Shininess", 4f);
    }

    // ---------- Ice ----------

    private void buildIce(Material mat, NoiseGenerator noise, float scale, int octaves, float persistence) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float nx = x / scale;
                float ny = y / scale;
                float value = noise.fractalNoise(nx, ny, octaves, persistence);

                int rgb = (value < 0f) ? rgb(210, 230, 245) : rgb(140, 170, 200);
                img.setRGB(x, y, rgb);
            }
        }

        applyDiffuse(mat, img);
        mat.setFloat("Shininess", 80f); // glossy, icy sheen
        mat.setColor("Specular", ColorRGBA.White);
    }

    // ---------- Desert ----------

    private void buildDesert(Material mat, NoiseGenerator noise, float scale, int octaves, float persistence) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float nx = x / scale;
                float ny = y / scale;
                float value = noise.fractalNoise(nx, ny, octaves, persistence);

                int rgb;
                if (value < 0f) {
                    rgb = rgb(180, 90, 50);   // lowland/dunes
                } else if (value < 0.25f) {
                    rgb = rgb(150, 70, 40);   // midland
                } else {
                    rgb = rgb(110, 50, 30);   // highland/canyons
                }
                img.setRGB(x, y, rgb);
            }
        }

        applyDiffuse(mat, img);
        mat.setFloat("Shininess", 1f); // matte, dusty
        mat.setColor("Specular", ColorRGBA.Black);
    }

    // ---------- Moon / barren (uses Worley crater noise) ----------

    private void buildMoon(Material mat, NoiseGenerator noise) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        BufferedImage normalImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float nx = x / (float) WIDTH;
                float ny = y / (float) HEIGHT;

                float crater = noise.craterNoise(nx, ny); // -1 (floor) .. 1 (untouched)
                int gray = 90 + (int) (crater * 40f);
                gray = Math.max(0, Math.min(255, gray));
                img.setRGB(x, y, rgb(gray, gray, gray));

                // cheap normal map: encode crater gradient as a bluish-tinted normal
                int nShade = 128 + (int) (crater * 60f);
                nShade = Math.max(0, Math.min(255, nShade));
                normalImg.setRGB(x, y, rgb(nShade, nShade, 255));
            }
        }

        applyDiffuse(mat, img);
        Texture2D normalTex = toTexture(normalImg);
        mat.setTexture("NormalMap", normalTex);
        mat.setFloat("Shininess", 1f);
        mat.setColor("Specular", ColorRGBA.Black);
    }

    // ---------- Star ----------

    // ---------- Star ----------

    private void buildStar(Material mat, NoiseGenerator noise, int octaves, float persistence, Random rand) {
        BufferedImage diffuseImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        BufferedImage glowImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // pick a star color family: yellow-white (Sun-like), orange/red (cooler), or blue-white (hot)
        int type = rand.nextInt(3);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float nx = x / 30f;   // tight scale = fine granulation, not continents
                float ny = y / 30f;

                float surface = noise.fractalNoise(nx, ny, octaves, persistence);
                // occasional dark sunspot clusters via a second, much coarser noise
                float spotNoise = noise.fractalNoise(x / 90f + 50f, y / 90f + 50f, 2, 0.5f);
                boolean sunspot = spotNoise < -0.35f;

                float brightness = 0.75f + surface * 0.25f; // granulation flicker
                if (sunspot) brightness *= 0.4f;             // darker, cooler patch

                int[] base = starBaseColor(type);
                int r = clamp255((int) (base[0] * brightness));
                int g = clamp255((int) (base[1] * brightness));
                int b = clamp255((int) (base[2] * brightness));

                diffuseImg.setRGB(x, y, rgb(r, g, b));
                // glow map: whole surface glows, sunspots glow less
                float glowStrength = sunspot ? 0.5f : 1f;
                glowImg.setRGB(x, y, rgb(
                        clamp255((int) (base[0] * glowStrength)),
                        clamp255((int) (base[1] * glowStrength)),
                        clamp255((int) (base[2] * glowStrength))));
            }
        }

        applyDiffuse(mat, diffuseImg);
        Texture2D glowTex = toTexture(glowImg);
        mat.setTexture("GlowMap", glowTex);

        ColorRGBA glowColor;
        switch (type) {
            case 0: glowColor = new ColorRGBA(1f, 0.55f, 0.15f, 1f); break; // orange/red, cooler star
            case 1: glowColor = new ColorRGBA(0.6f, 0.75f, 1f, 1f); break;  // blue-white, hot star
            default: glowColor = new ColorRGBA(1f, 0.95f, 0.7f, 1f); break; // yellow-white, Sun-like
        }
        mat.setColor("GlowColor", glowColor);

        // stars are self-illuminated; boost ambient response so they read as
        // "always lit" even from their own DirectionalLight's dark side
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.White);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setFloat("Shininess", 1f);
        mat.setColor("Specular", ColorRGBA.Black);
    }

    private int[] starBaseColor(int type) {
        switch (type) {
            case 0: return new int[]{255, 140, 60};  // orange/red, cooler
            case 1: return new int[]{180, 210, 255}; // blue-white, hot
            default: return new int[]{255, 240, 200}; // yellow-white, Sun-like
        }
    }

    private int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }



        // ---------- Shared helpers ----------

    private void applyDiffuse(Material mat, BufferedImage img) {
        Texture2D tex = toTexture(img);
        mat.setTexture("DiffuseMap", tex);
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Ambient", ColorRGBA.White);
    }

    private Texture2D toTexture(BufferedImage img) {
        Image jmeImg = new AWTLoader().load(img, false);
        return new Texture2D(jmeImg);
    }

    private int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }
}