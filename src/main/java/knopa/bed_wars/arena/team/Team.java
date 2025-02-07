package knopa.bed_wars.arena.team;

import knopa.bed_wars.arena.points.BedPoint;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private final String name;
    private final int size;
    private final ChatColor color;
    private final Location spawn;
    private final List<Player> players = new ArrayList<>();
    private final ItemStack teamItem;
    private final  List<CapturablePoint> pointsToCapture = new ArrayList<>();
    private  final BedPoint bedPoint;

    public Team(String name, int size, ChatColor color, Location spawn, ItemStack teamItem, BedPoint bedPoint) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.spawn = spawn;
        this.teamItem = teamItem;
        this.bedPoint = bedPoint;
    }

    public void addPoint(CapturablePoint capturablePoint){
        pointsToCapture.add(capturablePoint);
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public ChatColor getColor() {
        return color;
    }

    public Location getSpawn() {
        return spawn;
    }

    public ItemStack getTeamItem() {
        return teamItem;
    }

    public BedPoint getBedPoint() {
        return bedPoint;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<CapturablePoint> getPointsToCapture() {
        return pointsToCapture;
    }
}
