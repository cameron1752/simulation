package org.example.elevator;

public class HelperClass {
    private final static boolean debug = false;
    public String name = "";

    public HelperClass(String name){
        this.name = name;
    }

    public void debug(String message){
        if (debug){
            System.out.println(name + ": [" + message + "]");
        }
    }

    public void debugAlways(String message){
        System.out.println(name + ": [" + message + "]");
    }
}
