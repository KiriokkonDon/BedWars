package knopa.bed_wars.arena.abilities;

import knopa.bed_wars.Bed_wars;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Ability {

    private final  String name;
    private  final  int cooldownTick;
    private final ItemStack icon;


    public Ability(String name, int cooldownTick, ItemStack icon) {
        this.name = name;
        this.cooldownTick = cooldownTick;
        this.icon = icon;
    }

    public  void  setOnCooldown(Player player, String pathToConfigSection){
        player.getPersistentDataContainer().set(NamespacedKey.fromString("cooldown"), PersistentDataType.INTEGER, 1);

        new BukkitRunnable(){
            int timer = ConfigManager.instance.configs.get("abilities.yml").getInt(pathToConfigSection + ".cooldown") +
                    ConfigManager.instance.configs.get("abilities.yml").getInt(pathToConfigSection + ".duration");

            BossBar bossBar = Bukkit.createBossBar(
                    ConfigManager.instance.configs.get("abilities.yml").getString("cooldown.title"),
                    BarColor.valueOf(ConfigManager.instance.configs.get("abilities.yml").getString("cooldown.bar_color")),
                    BarStyle.valueOf(ConfigManager.instance.configs.get("abilities.yml").getString("cooldown.bar_style"))
            );

            @Override
            public void run(){
                bossBar.addPlayer(player);
                bossBar.setProgress((double) timer / (ConfigManager.instance.configs.get("abilities.yml").getInt(pathToConfigSection + ".cooldown") +
                        ConfigManager.instance.configs.get("abilities.yml").getInt(pathToConfigSection + ".duration")));

                timer -= 1;
                if (timer == 0){
                    cancel();
                    bossBar.removeAll();
                    player.getPersistentDataContainer().remove(NamespacedKey.fromString("cooldown"));
                }
            }
        }.runTaskTimer(Bed_wars.getInstance(), 0L, 20L);
    }

    public abstract int getSlot();

    public String getName() {
        return name;
    }

    public int getCooldownTick() {
        return cooldownTick;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
