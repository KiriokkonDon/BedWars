package knopa.bed_wars.util.gui;

import knopa.bed_wars.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    public static  List<Menu> menus = new ArrayList<>();

    public static Menu getMenuOf(Player player){
        for (Menu menu: menus){
            if (menu.viewers.contains(player)){
                return menu;
            }
        }
        return null;
    }

    private  final int size;
    private final  String title;

    private  final Inventory inventory;

    private  final List<Button> buttons = new ArrayList<>();
    private  final List<Player> viewers = new ArrayList<>();

    public Menu(int size, String title) {
        this.size = size;
        this.title = title;

        inventory = Bukkit.createInventory(null, size, ChatUtil.format(title));

        menus.add(this);
    }

    public  void showMenu(Player player){
        player.openInventory(inventory);
        viewers.add(player);
    }

    public  Menu  addButton(Button button){
        buttons.add(button);
        inventory.setItem(button.getSlot(), button.getItemStack());

        return this;
    }

    public Button getButtonIn(int slot){
        for (Button button: buttons){
            if (button.getSlot() == slot){
                return button;
            }
        }

        return null;
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public void onClose(Player player) {
        viewers.remove(player);
    }
}

