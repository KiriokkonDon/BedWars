package knopa.bed_wars.arena.game;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.GameStatus;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.player.SiegePlayer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiegeGame {

    private  final List<SiegePlayer> players = new ArrayList<>();

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
        players.add(new SiegePlayer(player));
        Map<String, String> args = new HashMap<>();
        args.put("%player%", player.getName());
        sendGameConfigMessage("joined", args);

        if (players.size() >= arena.getMinPlayers() && status == GameStatus.WAITING_FOR_PLAYERS){
            startCount();
        }
    }

    public void leavePlayer(Player player){
        players.remove(getSiegePlayer(player));
        sendGameConfigMessage("left");

        preparePlayer(player, true);
    }

    @Nullable
    public SiegePlayer getSiegePlayer(Player player){
        for (SiegePlayer siegePlayer: players){
            if (siegePlayer.getBukkitPlayer() == player){
                return siegePlayer;
            }
        }

        return null;
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

                Map<String, String> args1 = new HashMap<>();
                args1.put("%time%", String.valueOf(timer));
                sendGameConfigTitle("game_start_in", "", args1);

                if (timer-- <= 0){
                    start();
                    cancel();
                }
            }
        }.runTaskTimer(Bed_wars.getInstance(), 0L, 20L);
    }

    private void start(){
        status = GameStatus.PLAYING;
        for (SiegePlayer player: players){
            preparePlayer(player.getBukkitPlayer(), false);

            player.getBukkitPlayer().teleport(getTeamOf(player.getBukkitPlayer()).getSpawn());
            ChatUtil.sendConfigTitle(player.getBukkitPlayer(), "game_status", "");
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

                    Map<String, String> args2 = new HashMap<>();
                    args2.put("%time%", String.valueOf(timer));
                    ChatUtil.sendConfigTitle(player, "death_message", "", args2);
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

        for (SiegePlayer player: players){
            if (getTeamOf(player.getBukkitPlayer()) == winner){
                ChatUtil.sendConfigTitle(
                        player.getBukkitPlayer(),
                        "winner_message",
                        ""
                );
            }

            leavePlayer(player.getBukkitPlayer());
        }
    }

    private void respawnPlayer(Player player) {
        player.teleport(getTeamOf(player).getSpawn());
        preparePlayer(player, false);
    }

    public void sendGameConfigMessage(String path){
        for (SiegePlayer player: players){
            ChatUtil.sendConfigMessage(player.getBukkitPlayer(), path);
        }
    }

    public void sendGameConfigMessage(String path, Map<String, String> args){
        for (SiegePlayer player: players){
            ChatUtil.sendConfigMessage(player.getBukkitPlayer(), path, args);
        }
    }

    public void sendGameConfigTitle(String path, String subPath, Map<String, String> args){
        for (SiegePlayer player: players){
            ChatUtil.sendConfigTitle(player.getBukkitPlayer(), path, subPath, args);
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

    public List<SiegePlayer> getPlayers() {
        return players;
    }

    public SiegeArena getArena() {
        return arena;
    }

    public GameStatus getStatus() {
        return status;
    }
}
