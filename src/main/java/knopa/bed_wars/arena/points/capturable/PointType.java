package knopa.bed_wars.arena.points.capturable;

import org.bukkit.Material;

public enum PointType {

    IRON(Material.IRON_INGOT),
    GOLD(Material.GOLD_INGOT),
    DIAMOND(Material.DIAMOND);

    private final Material spawnItem;

    PointType(Material spawnItem){
        this.spawnItem = spawnItem;
    }

    public Material getSpawnItem() {
        return spawnItem;
    }
}
