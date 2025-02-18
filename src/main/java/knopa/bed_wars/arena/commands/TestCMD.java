package knopa.bed_wars.arena.commands;

import knopa.bed_wars.arena.abilities.impls.ArtyAbility;
import knopa.bed_wars.arena.abilities.impls.BerserkAbility;
import knopa.bed_wars.arena.abilities.impls.SoilderAbility;
import knopa.bed_wars.arena.abilities.impls.TankAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class TestCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("arty")) {
            ArtyAbility.instance.callAirStrike(player.getTargetBlockExact(100).getLocation(), player);
        }else if (args[0].equalsIgnoreCase("berserk")) {
            BerserkAbility.instance.apply(player);
        }else if (args[0].equalsIgnoreCase("tank")) {
            TankAbility.instance.apply(player);
        }else if (args[0].equalsIgnoreCase("soldier")) {
            Arrow arrow= player.launchProjectile(Arrow.class);
            SoilderAbility.instance.onFire(arrow);
        }
        return true;
    }
}
