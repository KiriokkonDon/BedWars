package knopa.bed_wars.arena.commands;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.points.capturable.PointResource;
import knopa.bed_wars.arena.team.Team;
import knopa.bed_wars.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameSettingCMD implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            ChatUtil.sendConfigMessage(sender, "only_for_player");
            return true;
        }

        if (!sender.isOp()){
            ChatUtil.sendConfigMessage(sender, "not_enough_permission");
            return true;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("create")){
            ArenaManager.instance.createArena(
                    args[1],
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    player.getLocation());

            ChatUtil.sendMessage(player, "DONE");
            return true;
        }
        else if (args[0].equalsIgnoreCase("addTeam")){
            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);

            ArenaManager.instance.addTeam(
                    arena,
                    args[2],
                    Integer.parseInt(args[3]),
                    ChatColor.valueOf(args[4]),
                    player.getLocation());
                    /*player.getInventory().getItemInMainHand());*/

            ChatUtil.sendMessage(player, "DONE");
            return true;
        }

        else if (args[0].equalsIgnoreCase("addPoint")){
            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);

            ArenaManager.instance.addPoint(
                    arena,
                    args[2],
                    player.getLocation(),
                    Double.parseDouble(args[3]),
                    PointResource.valueOf(args[4])
            );

            ChatUtil.sendMessage(player, "DONE");
            return true;
        }
        else if (args[0].equalsIgnoreCase("addTrader")){
            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);

            if (arena != null){
                arena.addTrader(player.getLocation());
            }

            ChatUtil.sendMessage(player, "DONE");
            return true;
        }
        else if (args[0].equalsIgnoreCase("launch")){
            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);

            arena.startGame();

            ChatUtil.sendMessage(player, "DONE");
            return true;
        }
        else if (args[0].equalsIgnoreCase("addteambed")) {
            if (args.length < 2) {
                ChatUtil.sendMessage(player, "Usage: /gamesetting addteambed <arenaName> <teamName>");
                return true;
            }

            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);
            if (arena == null) {
                ChatUtil.sendMessage(player, "Arena not found.");
                return true;
            }

            Team team = arena.getTeamBy(args[2]);
            if (team == null) {
                ChatUtil.sendMessage(player, "Team not found in this arena.");
                return true;
            }


            Block targetBlock = player.getTargetBlock(null, 5); // Get the block the player is looking at (max 5 blocks away)
            if (targetBlock.getType().name().contains("_BED")) {
                // Set the bed location for the team
                team.getBedPoint().setLocation(targetBlock.getLocation());
                ChatUtil.sendMessage(player, "Bed assigned to team " + team.getName() + " in arena " + arena.getName());
            } else {
                ChatUtil.sendMessage(player, "You must be looking at a bed.");
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            return List.of("create", "addTeam", "addTrader", "addPoint", "launch", "addteambed");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("addTeam") || args[0].equalsIgnoreCase("addteambed") || args[0].equalsIgnoreCase("addPoint") || args[0].equalsIgnoreCase("launch"))) {
            List<String> arenaNames = new ArrayList<>();
            for (SiegeArena arena : ArenaManager.instance.getArenas()) {
                arenaNames.add(arena.getName());
            }
            return arenaNames;
        }
        if(args.length == 3 && args[0].equalsIgnoreCase("addteambed")){
            List<String> teamNames = new ArrayList<>();
            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);
            if(arena!= null) {
                for (Team team : arena.getTeams()) {
                    teamNames.add(team.getName());
                }
                return teamNames;
            }
        }

        return null;
    }
}