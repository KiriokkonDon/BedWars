package knopa.bed_wars.arena.points.capturable;

import org.bukkit.Material;

public enum PointResource {

    IRON(Material.IRON_INGOT),
    GOLD(Material.GOLD_INGOT),
    DIAMOND(Material.DIAMOND),
    EMERALD(Material.EMERALD),;

    private final Material spawnItem;

    PointResource(Material spawnItem){
        this.spawnItem = spawnItem;
    }

    public Material getSpawnItem() {
        return spawnItem;
    }
}
