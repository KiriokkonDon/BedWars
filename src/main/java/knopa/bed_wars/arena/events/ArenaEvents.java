package knopa.bed_wars.arena.events;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.GameStatus;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.points.PointStatus;
import knopa.bed_wars.arena.points.capturable.CapturablePoint;
import knopa.bed_wars.arena.sellermenu.SellerMenu;
import knopa.bed_wars.arena.team.Team;
import knopa.bed_wars.util.ChatUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class ArenaEvents implements Listener {

    @EventHandler
    public void  onEntityInteract(PlayerInteractAtEntityEvent event){
        SiegeArena arena = ArenaManager.instance.getArenaOf(event.getPlayer());

        if (arena != null && arena.getGame() != null){
            if (event.getRightClicked().getPersistentDataContainer().has(NamespacedKey.fromString("siege_trader"), PersistentDataType.INTEGER)){
                SellerMenu.instance.showMenu(event.getPlayer());
            }
        }

    }



    @EventHandler
    public void  onBlockBreak(BlockBreakEvent event){
        SiegeArena arena = ArenaManager.instance.getArenaOf(event.getPlayer());
        Player player = event.getPlayer();

        if (arena != null){
            if (arena.getGame() != null){
                //Bed Breaking Check
                if(event.getBlock().getType().name().contains("_BED")){
                    Team team = arena.getGame().getTeamOf(player);
                    if(team != null){
                        if(team.getBedPoint().getLocation().equals(event.getBlock().getLocation())){
                            ChatUtil.sendMessage(player, "&cYou cannot break your own bed!");
                            event.setCancelled(true);
                            return;
                        }
                        else{
                            //It's an enemy bed
                            if (!arena.getGame().onBedBreak(event.getBlock().getLocation(), event.getPlayer())){
                                event.setCancelled(true);
                            }
                        }
                    }
                }
                // Other block breaking logic (if needed) can go here, after the bed check
                else if (!arena.getGame().onBedBreak(event.getBlock().getLocation(), event.getPlayer())){
                    event.setCancelled(true);
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
                else if (event.getEntity().getPersistentDataContainer().has(NamespacedKey.fromString("siege_trader"), PersistentDataType.INTEGER)){
                    event.setCancelled(true);
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
