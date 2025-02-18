package knopa.bed_wars.arena.commands;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.points.capturable.PointResource;
import knopa.bed_wars.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

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
                    player.getLocation(),
                    player.getInventory().getItemInMainHand());

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

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            return List.of("create", "addTeam", "addTrader", "addPoint", "launch");
        }

        return null;
    }
}