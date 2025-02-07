package knopa.bed_wars.arena.points;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.util.ChatUtil;

public enum PointStatus {

    BLOCKED(Bed_wars.getInstance().getConfig().getString("capturable_point.blocked")),
    ACTIVE(Bed_wars.getInstance().getConfig().getString("capturable_point.active")),
    DESTROYED(Bed_wars.getInstance().getConfig().getString("capturable_point.destroyed")),;

    private final String value;

    PointStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
