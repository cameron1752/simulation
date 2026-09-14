package org.example.gravity;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class Container {
    Geometry object;
    Vector3f position;
    float y_vel =  0.0f;
    float y_pos = 20f;
    float g = -9.8f;
    float bounce = .7f;
    float cur_y_vel = 0f;
    long startTime;
    int grid_x;
    int grid_y;
    float ground;
    float x_vel;
    float x_pos;
    float rightThreshold = -2;
    float leftThreshold = 2;
    float z_vel;
    float z_pos;
    float frontThreshold;
    float backThreshold;
    float sphere_radius;
    boolean fallOutOfBox = true;
    boolean start = true;

    public Container(Geometry object, Vector3f position){
        this.object = object;
        this.position = position.clone();

        int num = (int) (Math.random() * 100);
        if (num % 2 == 0){
            // create random x velocity for now
            this.x_vel = 10f;
            this.z_vel = 10f;
        } else {
            // create random x velocity for now
            this.x_vel = -10f;
            this.z_vel = -10f;
        }
        y_vel = -10;

        // add random bounciness
        bounce = (float) Math.random();

        this.x_pos = this.position.x;
        this.z_pos = this.position.z;


    }

    public void update(float tpf, Container[][] objects){
        if (start){
            startTime = System.nanoTime();
            start = false;
        }
        position.y = getYPosition(tpf, objects);
        position.x = getXPosition(tpf, objects);
        position.z = getZPosition(tpf, objects);
        object.setLocalTranslation(position);
    }

    private float getYPosition(float tpf, Container[][] objects){

        float t = (float) ((System.nanoTime() - startTime) / 1_000_000_000.0);
        float new_pos = y_pos + (y_vel * t) + (0.5f * g * t * t);

        if (new_pos <= ground){
            float impact_vel = y_vel + g * t;   // true instantaneous velocity at impact
            y_pos = ground;
            y_vel = -impact_vel * bounce;
            startTime = System.nanoTime();
            return ground;
        }
        return new_pos;
    }

    private float getXPosition(float tpf, Container[][] objects){

        x_pos = x_pos + (x_vel * tpf);

        // if there's a collision reverse the velocity
        if (isColliding(objects)){
            if (isHigherThanWall()){
                // let it fly, remove the thresholds and ground values
                ground = -10000;
                leftThreshold = leftThreshold * 100;
                rightThreshold = rightThreshold * 100;
            } else {
                // if we're not greater than the wall do nothing
                x_vel = (-1 * x_vel) * .95f;
                System.out.println("New X velocity: " + x_vel);
            }

        }

        return x_pos;
    }

    private float getZPosition(float tpf, Container[][] objects){

        z_pos = z_pos + (z_vel * tpf);

        // if there's a collision reverse the velocity
        if (isColliding(objects)){
            // if we're not greater than the wall do nothing
            if (isHigherThanWall()){
                // let it fly, remove the thresholds and ground values
                ground = -10000;
                leftThreshold = leftThreshold * 100;
                rightThreshold = rightThreshold * 100;
            } else {
                // if we're not greater than the wall do nothing
                z_vel = (-1 * z_vel) * .95f;
                System.out.println("New Z velocity: " + z_vel);
            }
        }

        return z_pos;
    }

    private boolean isHigherThanWall(){
        if (fallOutOfBox){
            float wallHeight = (-ground + sphere_radius);
            return y_pos > wallHeight;
        } else {
            return false;
        }
    }

    private boolean isColliding(Container[][] objects){
        if (x_pos > rightThreshold){ x_pos = rightThreshold; return true; }
        if (x_pos < leftThreshold){ x_pos = leftThreshold; return true; }
        if (z_pos < frontThreshold){ z_pos = frontThreshold; return true; }
        if (z_pos > backThreshold){ z_pos = backThreshold; return true; }
        if (y_pos <= ground){return true;}

        for (Container[] row : objects){
            for (Container other : row){
                if (other == this) continue;

                float dx = this.x_pos - other.x_pos;
                float dy = this.y_pos - other.y_pos;
                float dz = this.z_pos - other.z_pos;
                float distSq = dx*dx + dy*dy + dz*dz;
                float minDist = this.sphere_radius + other.sphere_radius;

                if (distSq <= minDist * minDist){
                    System.out.print("[" + grid_x + "," + grid_y + "] intersects [" + other.grid_x + "," + other.grid_y + "] ");
                    System.out.println(this.position + " intersects " + other.position);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isXColliding(Container[][] objects){
        if (x_pos > rightThreshold){
            x_pos = rightThreshold;
            return true;
        } else if (x_pos < leftThreshold){
            x_pos = leftThreshold;
            return true;
        }


//        printCords();

        int rows = objects.length;
        int cols = objects[0].length;

        // for each item in the objects array
        // check and see if that objects x y z (plus and minus sphere radius)
        // intersect w/ current x y z (plus and minus sphere radius)
        float y1 = this.position.x - sphere_radius;
        float y2 = this.position.x + sphere_radius;
        for (int x = 0; x < rows; x++){
            for (int y = 0; y < cols; y++){
                // see if x range and obj x range are overlapping
                float x1 = objects[x][y].position.x - sphere_radius;
                float x2 = objects[x][y].position.x + sphere_radius;

                if (x1 < y1 && y1 < x2){
                    System.out.println("[" + this.grid_x + "," + this.grid_y +"] intersects ["+objects[x][y].grid_x + "," + objects[x][y].grid_y +"]");
                    return true;

                }

                if (x1 < y2 && y2 < x2){
                    System.out.println("[" + this.grid_x + "," + this.grid_y +"] intersects ["+objects[x][y].grid_x + "," + objects[x][y].grid_y +"]");
                    return true;
                }
            }
        }

        return false;
    }

    private void printCords(){
        float[][] cords = new float[3][2];

        cords[0][0] = this.x_pos - sphere_radius;
        cords[0][1] = this.x_pos + sphere_radius;

        cords[1][0] = this.y_pos - sphere_radius;
        cords[1][1] = this.y_pos + sphere_radius;

        cords[2][0] = this.z_pos - sphere_radius;
        cords[2][1] = this.z_pos + sphere_radius;

        for (int x = 0; x < 3; x++){
            System.out.println("X: " + cords[x][0] + " to " + cords[x][1]);
        }
    }

    private boolean isZColliding(Container[][] objects){
        if (z_pos < frontThreshold){
            z_pos = frontThreshold;
            return true;
        }else if (z_pos > backThreshold){
            z_pos = backThreshold;
            return true;
        }
        int rows = objects.length;
        int cols = objects[0].length;

        // for each item in the objects array
        // check and see if that objects x y z (plus and minus sphere radius)
        // intersect w/ current x y z (plus and minus sphere radius)
        float y1 = this.position.z - sphere_radius;
        float y2 = this.position.z + sphere_radius;
        for (int x = 0; x < rows; x++){
            for (int y = 0; y < cols; y++){
                // see if x range and obj x range are overlapping
                float x1 = objects[x][y].position.z - sphere_radius;
                float x2 = objects[x][y].position.z + sphere_radius;

                if (x1 < y1 && y1 < x2){
                    System.out.println("[" + this.grid_x + "," + this.grid_y +"] intersects ["+objects[x][y].grid_x + "," + objects[x][y].grid_y +"]");
                    return true;
                }

                if (x1 < y2 && y2 < x2){
                    System.out.println("[" + this.grid_x + "," + this.grid_y +"] intersects ["+objects[x][y].grid_x + "," + objects[x][y].grid_y +"]");
                    return true;
                }
            }
        }
        return false;
    }
}
