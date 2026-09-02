package org.example;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;

public class Star extends Planet{
    public Star(float gravity, float mass, float radius, AssetManager assetManager, ColorRGBA color) {
        super(gravity, mass, radius, assetManager, color);
    }

    @Override
    public void update(float tpf){
        g.rotate(0f, .10f, 0f);
    }
}
