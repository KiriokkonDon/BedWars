package knopa.bed_wars.arena.abilities.impls;

import knopa.bed_wars.arena.abilities.Ability;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

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
    public int getSlot(){
        return 13;
    }
}
