package knopa.bed_wars.arena.points;

import org.bukkit.Location;

public class BedPoint {

    private  final Location location;

    private PointStatus status = PointStatus.BLOCKED;

    public BedPoint(Location location) {
        this.location = location;
    }

    public void onBreak(){
        status = PointStatus.DESTROYED;
    }

    public void activate(){
        status = PointStatus.ACTIVE;
    }

    public Location getLocation() {
        return location;
    }

    public PointStatus getStatus() {
        return status;
    }
}
