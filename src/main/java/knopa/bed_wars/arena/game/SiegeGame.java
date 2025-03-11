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
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;

public class SiegeGame {

    //для сохранения инвентаря перед игрой
    private final Map<UUID, ItemStack[]> playerInventories = new HashMap<>();

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

        //сохраняем инвентарь
        playerInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());

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
        Map<String, String> args = new HashMap<>();
        args.put("%player%", player.getName());
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
                sendGameConfigTitle("title", "game_start_in", args1);

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
            ChatUtil.sendConfigTitle(player.getBukkitPlayer(), "title", "game_status");
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
        for (int i=0; i < team.getPointsToCapture().size(); i++){
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

        if (!leaving) {
            player.getInventory().clear(); // Очищаем только при входе в игру
        }

    }

    //Проверяет, уничтожены ли все спавнеры команды.

    public boolean areAllPointsDestroyed(Team team) {
        for (CapturablePoint point : team.getPointsToCapture()) {
            if (point.getStatus() != PointStatus.DESTROYED) {
                return false;
            }
        }
        return true;
    }

    //Обрабатывает уничтожение спавнера и активирует кровать, если все спавнеры уничтожены.

    public void onPointDestroy(CapturablePoint point) {
        activateNextPoint(point.getTeam());
        if (areAllPointsDestroyed(point.getTeam())) {
            point.getTeam().getBedPoint().activate();
            // Сообщение о том, что кровать теперь уязвима (опционально)
            Map<String, String> args = new HashMap<>();
            args.put("%team%", point.getTeam().getColor() + point.getTeam().getName());
            sendGameConfigMessage("bed_vulnerable", args);


        }
    }

    public boolean onBedBreak(Location location, Player player) {
        // Find the team whose bed was broken
        Team brokenBedTeam = null;
        for (Team team : arena.getTeams()) {
            if (team.getBedPoint().getLocation() != null && team.getBedPoint().getLocation().equals(location)) {
                brokenBedTeam = team;
                break;
            }
        }

        if (brokenBedTeam == null) {
            return false; // Bed not found, or not a registered bed
        }

        // Handle bed destruction logic
        brokenBedTeam.getBedPoint().onBreak(); // Mark the bed as broken
        // Broadcast message, update game state, etc.


        Map<String, String> args = new HashMap<>();
        args.put("%team%", brokenBedTeam.getColor() + brokenBedTeam.getName());
        args.put("%player%", player.getName());
        sendGameConfigTitle("title", "bed_break", args);

        //Check for win
        checkForWin();
        return true; // Bed was successfully broken
    }

    private void checkForWin() {
        // Check if only one team has a bed remaining
        Team lastTeamWithBed = null;
        int teamsWithBeds = 0;
        for (Team team : arena.getTeams()) {
            if (team.getBedPoint().getStatus() != PointStatus.DESTROYED) {
                teamsWithBeds++;
                lastTeamWithBed = team;
            }
        }

        if (teamsWithBeds == 1) {
            // Announce winner and end the game
            onGameEnd(lastTeamWithBed);
        }
        if(teamsWithBeds == 0){
            onGameEnd(null);
        }
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
                    ChatUtil.sendConfigTitle(player, "title", "death_message", args2);
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
        //Reset players, inventories, and game
        arena.reset();

        if(winner != null){
            Map<String, String> winArgs = new HashMap<>();
            winArgs.put("%team%", winner.getColor() + winner.getName());
            sendGameConfigTitle("title", "team_win", winArgs);  // Announce the winner
        }
        else{
            sendGameConfigTitle("title", "draw",  new HashMap<>()); // Announce a draw
        }

        // Обрабатываем всех игроков
        for (SiegePlayer siegePlayer : players) {
            Player player = siegePlayer.getBukkitPlayer();

            // Телепортация на спавн
            player.teleport(getSpawnLocation());

            // Восстановление инвентаря
            ItemStack[] savedInventory = playerInventories.get(player.getUniqueId());
            if (savedInventory != null) {
                player.getInventory().clear();
                player.getInventory().setContents(savedInventory);
                playerInventories.remove(player.getUniqueId()); // Удаляем, чтобы избежать утечек памяти
            }

            // Выводим игрока из игры
            leavePlayer(player);
        }

        players.clear();
        status = GameStatus.WAITING_FOR_PLAYERS;
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

    // Метод для получения координат спавна
    private Location getSpawnLocation() {
        // Читаем значения из config.yml
        String worldName = Bed_wars.getInstance().getConfig().getString("spawn.world");
        double x = Bed_wars.getInstance().getConfig().getDouble("spawn.x");
        double y = Bed_wars.getInstance().getConfig().getDouble("spawn.y");
        double z = Bed_wars.getInstance().getConfig().getDouble("spawn.z");
        float yaw = (float) Bed_wars.getInstance().getConfig().getDouble("spawn.yaw");
        float pitch = (float) Bed_wars.getInstance().getConfig().getDouble("spawn.pitch");

        // Получаем объект мира
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // Если мир не найден, выводим предупреждение
            Bed_wars.getInstance().getLogger().warning("Мир '" + worldName + "' не найден!");
            return null; // Можно заменить на резервную локацию
        }

        // Создаем и возвращаем объект Location
        return new Location(world, x, y, z, yaw, pitch);
    }
}
