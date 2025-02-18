package knopa.bed_wars.arena.abilities.impls;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.abilities.Ability;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SoilderAbility extends Ability {

    public static final  SoilderAbility instance = new SoilderAbility();

    private SoilderAbility() {
        super(
                ChatUtil.format(ConfigManager.instance.configs.get("abilities.yml").getString("soldier.name")),
                        ConfigManager.instance.configs.get("abilities.yml").getInt("soldier.cooldown"),
                        new ItemStack(Material.valueOf(ConfigManager.instance.configs.get("abilities.yml").getString("soldier.icon")))
                );
    }

    public void onFire(Projectile projectile){
        projectile.setVelocity(projectile.getVelocity().multiply(5));
        projectile.setGravity(false);
        projectile.setVisualFire(true);
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

            // Use getInt with a default value, in case the config value is missing.
            int duration = ConfigManager.instance.configs.get("abilities.yml").getInt("soldier.duration", 10) * 20;
            int level = ConfigManager.instance.configs.get("abilities.yml").getInt("soldier.effect_level", 0); // Default to level 0 (Potion level 1)

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, level));

            setOnCooldown(player, "soldier");
        }
    }

    @Override
    public int getSlot(){
        return 13;
    }
}
