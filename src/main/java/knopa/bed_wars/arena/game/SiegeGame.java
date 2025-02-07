package knopa.bed_wars.arena.game;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.GameStatus;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.points.PointStatus;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import knopa.bed_wars.arena.team.Team;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SiegeGame {

    private  final List<Player> players = new ArrayList<>();

    private final SiegeArena arena;

    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;

    public SiegeGame(SiegeArena arena) {
        this.arena = arena;
    }

    private  void gameCycle(){
        new BukkitRunnable(){
            @Override
            public void run(){
                for (Team team: arena.getTeams()){
                    for (CapturablePoint capturablePoint: team.getPointsToCapture()){
                        capturablePoint.dropResource();
                    }
                }

                arena.getCenter().getWorld().dropItem(arena.getCenter(), new ItemStack(Material.EMERALD));
            }
        }.runTaskTimer(Bed_wars.getInstance(), 0L, 20L);
    }

    public void  joinPlayer(Player player){
        if (ArenaManager.instance.getArenaOf(player) != null){
            ChatUtil.sendConfigMessage(player, "error.already_on_arena");
            return;
        }

        if (players.size() >= arena.getMaxPlayers()){
            ChatUtil.sendConfigMessage(player, "error.arena_is_full");
            return;
        }

        for (Team team: arena.getTeams()){
            if (team.getPlayers().size() < team.getSize()){
                team.addPlayer(player);
                break;
            }
        }

        preparePlayer(player, false);
        players.add(player);
        sendGameConfigMessage("joined", Map.of("%player%", player.getName()));

        if (players.size() >= arena.getMinPlayers() && status == GameStatus.WAITING_FOR_PLAYERS){
            startCount();
        }
    }

    public void leavePlayer(Player player){
        players.remove(player);
        sendGameConfigMessage("left");

        preparePlayer(player, true);
    }

    public void startCount(){
        status = GameStatus.COUNTDOWN;

        new BukkitRunnable(){
            int timer = Bed_wars.getInstance().getConfig().getInt("time_to_start");

            @Override
            public void run(){
                if (players.size() < arena.getMinPlayers()){
                    status = GameStatus.WAITING_FOR_PLAYERS;
                    cancel();
                    return;
                }

                sendGameConfigTitle("game_start_in", "", Map.of("%time%", String.valueOf(timer)));

                if (timer-- <= 0){
                    start();
                    cancel();
                }
            }
        }.runTaskTimer(Bed_wars.getInstance(), 0L, 20L);
    }

    private void start(){
        status = GameStatus.PLAYING;
        for (Player player: players){
            preparePlayer(player, false);

            player.teleport(getTeamOf(player).getSpawn());
            ChatUtil.sendConfigTitle(player, "game_status", "");
        }

        for (Team team: arena.getTeams()){
            for (CapturablePoint point: team.getPointsToCapture()){
                point.spawn();
            }

            activateNextPoint(team);
        }
        gameCycle();
    }

    public  void  activateNextPoint(Team team){
        for (int i=0; 1< team.getPointsToCapture().size(); i++){
            if (team.getPointsToCapture().get(i).getStatus() == PointStatus.BLOCKED){
                if(i == 0 || team.getPointsToCapture().get(i - 1).getStatus() == PointStatus.DESTROYED){
                    team.getPointsToCapture().get(i).active();
                    return;
                }
            }
        }
    }

    private void preparePlayer(Player player, boolean leaving){
        player.setHealth(player.getHealthScale());
        player.setGameMode(GameMode.SURVIVAL);

        for (PotionEffect potionEffect: player.getActivePotionEffects()){
            player.removePotionEffect(potionEffect.getType());
        }

        player.getInventory().clear();

        if (!leaving){
            addTeamItems(player);
        }
    }

    public boolean onBedBreak(Location location, Player breaker){
        for (Team team: arena.getTeams()){
            if (team.getBedPoint().getLocation().distance(location) < 1){
                if (getTeamOf(breaker) == team){
                    return  false;
                }

                if (team.getBedPoint().getStatus() != PointStatus.ACTIVE){
                    return false;
                }

                team.getBedPoint().onBreak();
                return true;
            }
        }
        return true;
    }

    public void onPlayerDeath(Player player){
        player.setGameMode(GameMode.SPECTATOR);
        Team team = getTeamOf(player);

        if (team.getBedPoint().getStatus() != PointStatus.DESTROYED){
            new BukkitRunnable(){
                int timer = Bed_wars.getInstance().getConfig().getInt("respawn_time");

                @Override
                public void run(){
                    if (timer-- <= 0){
                        respawnPlayer(player);
                        cancel();
                    }

                    ChatUtil.sendConfigTitle(player, "death_message","", Map.of("%time%", String.valueOf(timer)));
                }
            }.runTaskTimer(Bed_wars.getInstance(), 0L, 20L);
        }
        else{
            ChatUtil.sendConfigMessage(player, "loser_message");
            team.getPlayers().remove(player);

            int aliveTeams = 0;
            Team aliveTeam = null;

            for (Team t: arena.getTeams()){
                if (t.getPlayers().size() > 0){
                    aliveTeams +=1;
                    aliveTeam = t;
                }
            }

            if (aliveTeams == 1){
                onGameEnd(aliveTeam);
            }

        }
    }

    public void setTeamOf(Player player, Team team){
        for (Team t: arena.getTeams()){
            t.getPlayers().remove(player);
        }

        team.addPlayer(player);
    }

    public  void  onGameEnd(Team winner){
        arena.reset();

        for (Player player: players){
            if (getTeamOf(player) == winner){
                ChatUtil.sendConfigTitle(
                        player,
                        "winner_message",
                        ""
                );
            }

            leavePlayer(player);
        }
    }

    private void respawnPlayer(Player player) {
        player.teleport(getTeamOf(player).getSpawn());
        preparePlayer(player, false);
    }

    public void sendGameConfigMessage(String path){
        for (Player player: players){
            ChatUtil.sendConfigMessage(player, path);
        }
    }

    public void sendGameConfigMessage(String path, Map<String, String> args){
        for (Player player: players){
            ChatUtil.sendConfigMessage(player, path, args);
        }
    }

    public void sendGameConfigTitle(String path, String subPath, Map<String, String> args){
        for (Player player: players){
            ChatUtil.sendConfigTitle(player, path, subPath, args);
        }
    }

    private void addTeamItems(Player player){
        for (Team team: arena.getTeams()){
            player.getInventory().addItem(team.getTeamItem());
        }
    }


    public Team getTeamOf(Player player){
        for (Team team: arena.getTeams()){
            if (team.getPlayers().contains(player)){
                return team;
            }
        }
        return null;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public SiegeArena getArena() {
        return arena;
    }

    public GameStatus getStatus() {
        return status;
    }
}
