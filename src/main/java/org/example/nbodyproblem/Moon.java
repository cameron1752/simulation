package org.example.nbodyproblem;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import org.example.nbodyproblem.Body;

public class Moon extends Body {

    public Moon(String name, float mass, float radius, Vector3f initialPosition, AssetManager assetManager, float g_constant, boolean isStar) {
        super(name, mass, radius, initialPosition, assetManager, g_constant, isStar);
    }
}
