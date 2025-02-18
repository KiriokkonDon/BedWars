package knopa.bed_wars.arena.abilities.impls;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.abilities.Ability;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ArtyAbility extends Ability {

    public static final ArtyAbility instance = new ArtyAbility();

    private ArtyAbility() {
        super(
                ChatUtil.format(ConfigManager.instance.configs.get("abilities.yml").getString("arty.name")),
                ConfigManager.instance.configs.get("abilities.yml").getInt("arty.cooldown"),
                new ItemStack(Material.valueOf(ConfigManager.instance.configs.get("abilities.yml").getString("arty.icon")))
        );
    }

    public void callAirStrike(Location location, Player player) {
        NamespacedKey cooldownKey = new NamespacedKey(Bed_wars.getInstance(), "cooldown");

        if (!player.getPersistentDataContainer().has(cooldownKey, PersistentDataType.INTEGER)) {
            int radius = ConfigManager.instance.configs.get("abilities.yml").getInt("arty.radius");

            new BukkitRunnable() {
                int timer = ConfigManager.instance.configs.get("abilities.yml").getInt("arty.duration") * 20;

                @Override
                public void run() {
                    for (int x = -radius; x <= radius; x += 2) {
                        for (int z = -radius; z <= radius; z += 2) {
                            if (new Random().nextInt(100) < 100 / Math.pow(radius * 2, 2)) {
                                spawnShell(new Location(
                                        location.getWorld(),
                                        location.getBlockX() + x,
                                        location.getWorld().getMaxHeight(),
                                        location.getBlockZ() + z
                                ));
                            }
                        }
                    }

                    if (timer-- <= 0) {
                        cancel();
                    }
                }
            }.runTaskTimer(Bed_wars.getInstance(), 0L, 1L);


            setOnCooldown(player, "arty");
        }
    }

    private void spawnShell(Location location) {
        Snowball snowball = location.getWorld().spawn(location, Snowball.class);
        snowball.setGravity(false);
        snowball.setVelocity(new Vector(0, -10, 0));
        snowball.setVisualFire(true);

        NamespacedKey shellKey = new NamespacedKey(Bed_wars.getInstance(), "shell");
        snowball.getPersistentDataContainer().set(shellKey, PersistentDataType.INTEGER, 1);

    }

    @Override
    public void apply(Player player) {
        NamespacedKey cooldownKey = new NamespacedKey(Bed_wars.getInstance(), "cooldown");
        if (!player.getPersistentDataContainer().has(cooldownKey, PersistentDataType.INTEGER)) {
            // Good practice:  Check config for null values!
            if (ConfigManager.instance.configs.get("abilities.yml") == null) {
                player.sendMessage(ChatUtil.format("&cError: abilities.yml configuration file not found!"));
                return; // Exit if config is missing.
            }

            setOnCooldown(player, "arty"); // Set the cooldown *first*.
            // Now, get the player's TARGET LOCATION.  This is what makes it an "air strike" ability.
            Location targetLocation = player.getTargetBlock(null, 100).getLocation(); // Get target block within 100 blocks
            if (targetLocation != null)
            {
                callAirStrike(targetLocation, player);  // Call airstrike at that location.
            }
        }
    }

    @Override
    public int getSlot(){
        return 12;
    }


}
