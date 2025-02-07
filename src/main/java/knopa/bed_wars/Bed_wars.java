package knopa.bed_wars;

import knopa.bed_wars.arena.commands.GameSettingCMD;
import knopa.bed_wars.arena.commands.PlayerCMD;
import knopa.bed_wars.arena.events.ArenaEvents;
import knopa.bed_wars.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bed_wars extends JavaPlugin {

    private static Bed_wars instance;

    public  static Bed_wars getInstance() {return  instance;}

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        ConfigManager.instance.init("messages");

        getServer().getPluginManager().registerEvents(new ArenaEvents(), this);

        getCommand("gameSetting").setExecutor(new GameSettingCMD());
        getCommand("gameSetting").setTabCompleter(new GameSettingCMD());

        getCommand("siege").setExecutor(new PlayerCMD());
        getCommand("siege").setTabCompleter(new PlayerCMD());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
