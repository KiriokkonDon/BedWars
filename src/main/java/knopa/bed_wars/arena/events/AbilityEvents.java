package knopa.bed_wars.arena.events;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.abilities.Ability;
import knopa.bed_wars.arena.abilities.impls.ArtyAbility;
import knopa.bed_wars.arena.abilities.impls.BerserkAbility;
import knopa.bed_wars.arena.abilities.impls.SoilderAbility;
import knopa.bed_wars.arena.abilities.impls.TankAbility;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataType;

public class AbilityEvents implements Listener {

    @EventHandler
    public void onAbilityCall(PlayerDropItemEvent event){
        SiegeArena arena = ArenaManager.instance.getArenaOf(event.getPlayer());

        if (arena != null){
            Ability ability = arena.getGame().getSiegePlayer(event.getPlayer()).getAbility();

            if (ability != null){
                if (ability instanceof ArtyAbility){
                    ((ArtyAbility) ability).callAirStrike(
                            event.getPlayer().getTargetBlockExact(100).getLocation(),
                            event.getPlayer()
                    );
                    event.setCancelled(true);
                }
                else if (ability instanceof BerserkAbility){
                    ((BerserkAbility) ability).apply(event.getPlayer());
                }
                else if (ability instanceof TankAbility){
                    ((TankAbility) ability).apply(event.getPlayer());
                }
            }
        }

    }

    @EventHandler
    public  void onShot(ProjectileLaunchEvent event){
        if (event.getEntity().getShooter() instanceof Player){
            Player player = (Player) event.getEntity().getShooter();

            SiegeArena arena = ArenaManager.instance.getArenaOf(player);

            if (arena != null){
                Ability ability =  arena.getGame().getSiegePlayer(player).getAbility();

                if (ability instanceof SoilderAbility){
                    ((SoilderAbility) ability).onFire(event.getEntity());
                }

            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event){
        if (event.getEntity().getPersistentDataContainer().has(NamespacedKey.fromString("shell"), PersistentDataType.INTEGER)){
            event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 3f, false, false);
        }
    }
}
