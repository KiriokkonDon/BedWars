package knopa.bed_wars.arena.commands;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerCMD implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            ChatUtil.sendConfigMessage(sender, "only_for_player");
            return true;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("join")){
            SiegeArena arena = ArenaManager.instance.getArenaBy(args[1]);

            arena.getGame().joinPlayer(player);
        }else if (args[0].equalsIgnoreCase("leave")){
            SiegeArena arena = ArenaManager.instance.getArenaOf(player);

            arena.getGame().leavePlayer(player);
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            return List.of("join", "leave");
        }

        return null;
    }
}
