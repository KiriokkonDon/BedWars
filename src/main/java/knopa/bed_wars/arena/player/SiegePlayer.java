package knopa.bed_wars.arena.player;

import knopa.bed_wars.arena.abilities.Ability;
import org.bukkit.entity.Player;

public class SiegePlayer {

    private  final Player bukkitPlayer;
    private Ability ability = null;

    public SiegePlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public Ability getAbility() {
        return ability;
    }

    public void setAbility(Ability ability) {
        this.ability = ability;
    }
}
