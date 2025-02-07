package knopa.bed_wars.arena;

import knopa.bed_wars.arena.game.SiegeGame;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import knopa.bed_wars.arena.team.Team;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SiegeArena {

    private String name;

    private int minPlayers;
    private int maxPlayers;

    private Location center;

    private final List<Team> teams = new ArrayList();

    private SiegeGame game = null;

    public SiegeArena(String name, int minPlayers, int maxPlayers, Location center) {
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.center = center;
    }

    @Nullable
    public Team getTeamBy(ItemStack itemStack){
        for (Team team: teams){
            if (team.getTeamItem() == itemStack){
                return team;
            }
        }
        return null;
    }

    @Nullable
    public Team getTeamBy(String name){
        for (Team team: teams){
            if (team.getName().equals(name)){
                return team;
            }
        }
        return null;
    }

    @Nullable
    public CapturablePoint getPointBy(EnderCrystal entity){
        for (Team team: teams){
            for (CapturablePoint point: team.getPointsToCapture()){
                if(point.getEntity() == entity){
                    return point;
                }
            }

        }
        return null;
    }

    public  void addPoint(String teamName, CapturablePoint capturablePoint){
        for (Team team: teams){
            if (team.getName().equals(teamName)){
                team.addPoint(capturablePoint);

                team.getPointsToCapture().sort((point1, point2) -> {
                    double distance1 = point1.getLocation().distance(center);
                    double distance2 = point2.getLocation().distance(center);

                    return(int) (distance1 - distance2);


                });
            }
        }
    }

    public void startGame(){
        game = new SiegeGame(this);
    }

    public void reset(){}

    public void addTeam(Team team){
        teams.add(team);
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Location getCenter() {
        return center;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public SiegeGame getGame() {
        return game;
    }
}
