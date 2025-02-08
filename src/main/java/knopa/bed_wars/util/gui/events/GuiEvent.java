package knopa.bed_wars.util.gui.events;

import knopa.bed_wars.util.gui.Button;
import knopa.bed_wars.util.gui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiEvent implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event){
        Menu menu = Menu.getMenuOf((Player) event.getWhoClicked());

        if (menu != null){
            Button button = menu.getButtonIn(event.getSlot());

            if (button != null){
                button.onClick((Player) event.getWhoClicked(), event.getClick());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Menu menu = Menu.getMenuOf((Player) event.getPlayer());

        if (menu != menu){
            menu.onClose((Player) event.getPlayer());
        }
    }
}
