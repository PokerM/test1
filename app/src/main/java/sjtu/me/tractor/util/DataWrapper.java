package sjtu.me.tractor.util;

/**
 * Created by billhu on 2017/11/19.
 */

public class DataWrapper {
    private double lat;
    private double lng;
    private double x;
    private double y;
    private int satellite;
    private int gps;
    private double north;
    private double velocity;
    private int command;
    private double direction;
    private double lateral;
    private double turnning;
    private double seeding;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getSatellite() {
        return satellite;
    }

    public void setSatellite(int satellite) {
        this.satellite = satellite;
    }

    public int getGps() {
        return gps;
    }

    public void setGps(int gps) {
        this.gps = gps;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getLateral() {
        return lateral;
    }

    public void setLateral(double lateral) {
        this.lateral = lateral;
    }

    public double getTurnning() {
        return turnning;
    }

    public void setTurnning(double turnning) {
        this.turnning = turnning;
    }

    public double getSeeding() {
        return seeding;
    }

    public void setSeeding(double seeding) {
        this.seeding = seeding;
    }
}
