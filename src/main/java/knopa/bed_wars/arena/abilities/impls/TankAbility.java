package knopa.bed_wars.arena.abilities.impls;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.arena.abilities.Ability;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TankAbility extends Ability {

    public static final  TankAbility instance = new TankAbility();

    private TankAbility() {
        super(
                ChatUtil.format(ConfigManager.instance.configs.get("abilities.yml").getString("tank.name")),
                        ConfigManager.instance.configs.get("abilities.yml").getInt("tank.cooldown"),
                        new ItemStack(Material.valueOf(ConfigManager.instance.configs.get("abilities.yml").getString("tank.icon")))
        );
    }

    public void apply(Player player) {
        NamespacedKey cooldownKey = new NamespacedKey(Bed_wars.getInstance(), "cooldown");
        if (!player.getPersistentDataContainer().has(cooldownKey, PersistentDataType.INTEGER)) {
            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.INCREASE_DAMAGE,
                            ConfigManager.instance.configs.get("abilities.yml").getInt("tank.duration") * 20,
                            3
                    )
            );

            setOnCooldown(player, "tank");
        }
    }

    @Override
    public int getSlot(){
        return 14;
    }
}
