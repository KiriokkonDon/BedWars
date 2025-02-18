package knopa.bed_wars.arena.points.capturable;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.points.PointStatus;
import knopa.bed_wars.arena.team.Team;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CapturablePoint {

    private final Location location;
    private double hp;
    private final PointResource type;
    private EnderCrystal entity;
    private Hologram hologram;
    private PointStatus status = PointStatus.BLOCKED;
    private final Team team;

    public CapturablePoint(Location location, double hp, PointResource type, Team team) {
        this.location = location;
        this.hp = hp;
        this.type = type;
        this.team = team;
    }

    public  void dropResource(){
        Location locToSpawn = location.clone().add(new Vector(0,2,0));
        locToSpawn.getWorld().dropItem(locToSpawn, new ItemStack(type.getSpawnItem()));
    }

    public  void spawn(){
        location.getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
        entity = location.getWorld().spawn(location, EnderCrystal.class);

        hologram = new Hologram(
                    Collections.emptyList(),
                location.add(new Vector(0,1,0))
        );

        updateHologram();
    }

    public void updateHologram() {
        Map<String, String> args = new HashMap<>();

        args.put("%status%", status.getValue());
        args.put("%hp%", String.valueOf(hp));
        args.put("%team%", team.getName());

        hologram.setText(
                ChatUtil.applyArgs(
                        Bed_wars.getInstance().getConfig().getStringList("capturable_point.hp_text"),
                        args
                )
        );
    }

    public  void damage(double amount){
        if (status != PointStatus.ACTIVE){
            return;
        }

        hp -= amount;

        updateHologram();

        if (hp <= 0 ){
            destroy();
        }
    }

    private  void destroy(){
        entity.remove();
        hp = 0;
        status = PointStatus.DESTROYED;

        updateHologram();
    }

    public void active(){
        status = PointStatus.ACTIVE;
        updateHologram();
    }

    public Location getLocation() {
        return location;
    }

    public double getHp() {
        return hp;
    }

    public PointResource getType() {
        return type;
    }

    public EnderCrystal getEntity() {
        return entity;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public PointStatus getStatus() {
        return status;
    }

    public Team getTeam() {
        return team;
    }
}
