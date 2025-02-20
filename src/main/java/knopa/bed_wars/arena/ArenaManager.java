package knopa.bed_wars.arena;

import knopa.bed_wars.arena.points.BedPoint;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import knopa.bed_wars.arena.points.capturable.PointResource;
import knopa.bed_wars.arena.team.Team;
import knopa.bed_wars.data.DataBaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ArenaManager {

    public static  final  ArenaManager instance = new ArenaManager();

    private final List<SiegeArena> arenas = new ArrayList<>();

    public  List<SiegeArena> getArenas(){
        return new ArrayList<>(arenas);
    }

    @Nullable
    public SiegeArena getArenaOf(Player player){
        for (SiegeArena arena: arenas){
            if (arena.getGame() != null){
                if (arena.getGame().getPlayers().contains(arena.getGame().getSiegePlayer(player))){
                    return arena;
                }
            }
        }
        return null;
    }

    public void loadArenas(){
        arenas.addAll(DataBaseManager.instance.getArenas());
    }

    @Nullable
    public SiegeArena getArenaBy(String name){
        for (SiegeArena arena: arenas){
            if (arena.getName().equals(name)){
                return arena;
            }
        }
        return null;
    }

    public void createArena(String name, int minPlayers, int maxPlayers, Location center){
        SiegeArena siegeArena = new SiegeArena(name, minPlayers, maxPlayers, center);
        arenas.add(siegeArena);

        DataBaseManager.instance.createArena(siegeArena);
    }

    public  void addTeam(SiegeArena arena, String name, int size, ChatColor color, Location spawn /*ItemStack itemStack*/){
        Team team = new Team(name, size, color, spawn, /*itemStack,*/ new BedPoint(spawn));
        arena.addTeam(team);

        DataBaseManager.instance.createTeam(team, arena.getName());
    }

    public void addPoint(SiegeArena arena, String teamName, Location location, double hp, PointResource type){
        CapturablePoint capturablePoint = new CapturablePoint(location, hp, type, arena.getTeamBy(teamName));
        arena.addPoint(teamName, capturablePoint);

        DataBaseManager.instance.createPoint(capturablePoint, teamName);
    }
}