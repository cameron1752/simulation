package org.example.neuralnet;

public class Row {
    private long id;
    private String time;
    private float tempF;
    private float pressure;
    private float precip;
    private double lat;
    private double lon;

    public Row(String time, float tempF, float pressure, float precip, double lat, double lon) {
        this.time = time;
        this.tempF = tempF;
        this.pressure = pressure;
        this.precip = precip;
        this.lat = lat;
        this.lon = lon;
    }

    public Row(long id, String time, float tempF, float pressure, float precip, double lat, double lon) {
        this.id = id;
        this.time = time;
        this.tempF = tempF;
        this.pressure = pressure;
        this.precip = precip;
        this.lat = lat;
        this.lon = lon;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getTempF() {
        return tempF;
    }

    public void setTempF(float tempF) {
        this.tempF = tempF;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getPrecip() {
        return precip;
    }

    public void setPrecip(float precip) {
        this.precip = precip;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
