package knopa.bed_wars.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hologram {

    private final List<ArmorStand> entites = new ArrayList<>();
    private List<String> text;
    private final Location location;

    public Hologram(List<String> text, Location location) {
        this.text = text;
        this.location = location.add(new Vector(0, -2, 0));

        overrideHologram();
    }

    public void setText(List<String> text) {
        this.text = text;

        overrideHologram();
    }

    private void overrideHologram() {
        for (int i = 0; i < text.size(); i++) {
            if (entites.size() > i) {
                entites.get(i).setCustomName(ChatUtil.format(text.get(i)));
            }
            else {
                ArmorStand armorStand = location.getWorld().spawn(location.add(new Vector(0, i * 0.3, 0)), ArmorStand.class);

                armorStand.setCustomName(ChatUtil.format(text.get(i)));
                armorStand.setCustomNameVisible(true);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStand.setInvulnerable(true);

                entites.add(armorStand);
            }
        }


        Iterator<ArmorStand> iterator = entites.subList(text.size(), entites.size()).stream().iterator();

        while (iterator.hasNext()) {
            iterator.next().remove();
        }
    }

    public void destroy() {
        for (ArmorStand armorStand: entites) {
            armorStand.remove();
        }
    }
}
