package knopa.bed_wars.arena.events;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.GameStatus;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.points.PointStatus;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import knopa.bed_wars.arena.team.Team;
import knopa.bed_wars.util.ChatUtil;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class ArenaEvents implements Listener {
    @EventHandler
    public void  onTeamChange(PlayerInteractEvent event){
        if (event.hasItem()){
            SiegeArena arena = ArenaManager.instance.getArenaOf(event.getPlayer());

            if (arena != null && arena.getGame() != null){
                Team team = arena.getTeamBy(event.getItem());

                if (team != null){
                    arena.getGame().setTeamOf(event.getPlayer(), team);
                    ChatUtil.sendConfigMessage(event.getPlayer(), "team_svitch_message", Map.of("%team%", team.getName()));
                }
            }
        }
    }

    @EventHandler
    public void  onBlockBreak(BlockBreakEvent event){
        SiegeArena arena = ArenaManager.instance.getArenaOf(event.getPlayer());

        if (arena != null){
            if (arena.getGame() != null){
                if (!event.getBlock().getType().name().contains("_BED")){
                   if (!arena.getGame().onBedBreak(event.getBlock().getLocation(), event.getPlayer())){
                       event.setCancelled(true);
                   }
                }
            }
        }
    }

    @EventHandler
    public void onAtack(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player){
            Player damager = (Player) event.getDamager();

            SiegeArena arena = ArenaManager.instance.getArenaOf(damager);

            if (arena != null){
                if (event.getEntity() instanceof Player){
                    Player damaged = (Player) event.getEntity();

                    if (arena.getGame().getStatus() != GameStatus.PLAYING){
                        event.setCancelled(true);
                        return;
                    }

                    if (arena.getGame().getTeamOf(damaged) == arena.getGame().getTeamOf(damager)){
                        event.setCancelled(true);
                        return;
                    }

                    if (damaged.getHealth() <= event.getDamage()){
                        arena.getGame().onPlayerDeath(damaged);
                        event.setCancelled(true);
                    }
                }
                else if (event.getEntity() instanceof EnderCrystal){

                    CapturablePoint point = arena.getPointBy((EnderCrystal) event.getEntity());

                    if (point != null){
                        if (arena.getGame().getTeamOf(damager) == point.getTeam()){
                            event.setCancelled(true);
                            return;
                        }

                        point.damage(event.getDamage());
                        event.setCancelled(true);

                        if (point.getStatus() == PointStatus.DESTROYED){
                            arena.getGame().activateNextPoint(point.getTeam());
                        }
                    }

                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if (!(event instanceof  EntityDamageByEntityEvent)){
            if (event.getEntity() instanceof  Player){
                Player player = (Player) event.getEntity();

                SiegeArena arena = ArenaManager.instance.getArenaOf(player);

                if (arena != null){
                    if (event.getDamage() >= player.getHealth()){
                        event.setCancelled(true);
                        arena.getGame().onPlayerDeath(player);
                    }
                }
            }
        }
    }

}
