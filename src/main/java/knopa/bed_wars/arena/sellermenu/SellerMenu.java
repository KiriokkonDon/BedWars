package knopa.bed_wars.arena.sellermenu;

import knopa.bed_wars.arena.ArenaManager;
import knopa.bed_wars.arena.SiegeArena;
import knopa.bed_wars.arena.abilities.Ability;
import knopa.bed_wars.arena.abilities.impls.ArtyAbility;
import knopa.bed_wars.arena.abilities.impls.BerserkAbility;
import knopa.bed_wars.arena.abilities.impls.SoilderAbility;
import knopa.bed_wars.arena.abilities.impls.TankAbility;
import knopa.bed_wars.arena.player.SiegePlayer;
import knopa.bed_wars.arena.points.capturable.PointResource;
import knopa.bed_wars.util.ChatUtil;
import knopa.bed_wars.util.ConfigManager;
import knopa.bed_wars.util.ItemBuilder;
import knopa.bed_wars.util.gui.Button;
import knopa.bed_wars.util.gui.Menu;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class SellerMenu {

    public  static  final SellerMenu instance = new SellerMenu();

    private Menu category, blocks, food, weapons, armor, bows, abilities, others;

    private  SellerMenu(){
        category = parsetCategories();

        blocks = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("blocks"));
        food = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("food"));
        weapons = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("weapons"));
        bows = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("bows"));
        armor = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("armor"));
        abilities = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("abilities"));
        others = parseCategoryMenu(ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("others"));
    }

    private Menu parsetCategories(){
        ConfigurationSection section = ConfigManager.instance.configs.get("seller-menu.yml").getConfigurationSection("categories");

        Menu menu =  new Menu(section.getInt("size"), section.getString("title"));

        ConfigurationSection itemsSection = section.getConfigurationSection("items");

        for (String key : itemsSection.getKeys(false)){
            ConfigurationSection categorySection = itemsSection.getConfigurationSection(key);

            menu.addButton(
                    new Button(
                        categorySection.getInt("slot"),
                            new ItemBuilder(
                                    Material.valueOf(categorySection.getString("material")),
                                    1
                            )
                                    .displayName(categorySection.getString("name"))
                            .build()
                    ){
                        @Override
                        public void onClick(Player player, ClickType clickType){
                            if (getSlot() == 10){
                                blocks.ShowMenu(player);
                            }
                            else if (getSlot() == 11){
                                food.ShowMenu(player);
                            }
                            else if (getSlot() == 12){
                                weapons.ShowMenu(player);
                            }
                            else if (getSlot() == 13){
                                bows.ShowMenu(player);
                            }
                            else if (getSlot() == 14){
                                armor.ShowMenu(player);
                            }
                            else if (getSlot() == 15){
                                abilities.ShowMenu(player);
                            }
                            else if (getSlot() == 16){
                                others.ShowMenu(player);
                            }
                        }
                    }
            );

        }

        return menu;
    }

    public Menu parseCategoryMenu(ConfigurationSection section){
        Menu menu = new Menu(section.getInt("size"), section.getString("title"));

        ConfigurationSection itemsSection = section.getConfigurationSection("items");

        if (itemsSection == null){
            return menu;
        }

        for (String key : itemsSection.getKeys(false)){
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection.getString("material") != null) {
                ItemBuilder item =                                 new ItemBuilder(
                        Material.valueOf(itemSection.getString("material")),
                        itemSection.getInt("amount")
                )
                        .setLore(
                                ConfigManager.instance.configs.get("seller-menu.yml").getStringList("price_lore"),
                                Map.of(
                                        "%price%", String.valueOf(itemSection.getInt("price")),
                                        "%resource%", itemSection.getString("vault")
                                )
                        );
                ConfigurationSection enchantmentSection = itemSection.getConfigurationSection("enchantments");

                if (enchantmentSection != null){
                for (String enchantmentKey: enchantmentSection.getKeys(false)){
                    item.addEnchantment(Enchantment.getByName(enchantmentKey), enchantmentSection.getInt(enchantmentKey));
                }
                }

                menu.addButton(
                        new Button(
                                Integer.parseInt(key),
                                item.build()
                        ) {
                            @Override
                            public void onClick(Player player, ClickType clickType) {
                                PointResource pointResource = PointResource.valueOf(itemSection.getString("vault"));
                                int price = itemSection.getInt("price");

                                if (hasMoney(player, price, pointResource )){
                                    ItemStack itemStack =                                             new ItemStack(
                                            Material.valueOf(itemSection.getString("material")),
                                            itemSection.getInt("amount"));


                                    if (enchantmentSection != null){
                                        for (String enchantmentKey: enchantmentSection.getKeys(false)){
                                            itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantmentKey), enchantmentSection.getInt(enchantmentKey));
                                        }
                                    }

                                    player.getInventory().addItem(
                                            itemStack
                                    );
                                    player.getInventory().removeItem(new ItemStack(pointResource.getSpawnItem(), price));
                                }
                                else {
                                    ChatUtil.sendConfigMessage(player, "error.not_enough_money");
                                    player.closeInventory();
                                }
                            }
                        }
                );
            }
            else{
                int slot = Integer.parseInt(key);
                Ability ability;

                if (slot == ArtyAbility.instance.getSlot()){
                    ability = ArtyAbility.instance;
                }
                else if (slot == BerserkAbility.instance.getSlot()){
                    ability = BerserkAbility.instance;
                }
                else if (slot == SoilderAbility.instance.getSlot()){
                    ability = SoilderAbility.instance;
                }
                else  {
                    ability = TankAbility.instance;
                }

                menu.addButton(
                        new Button(
                                ability.getSlot(),
                                new ItemBuilder(
                                        ability.getIcon().getType(),
                                        ability.getIcon().getAmount()
                                )
                                        .setLore(
                                                ConfigManager.instance.configs.get("seller-menu.yml").getStringList("price_lore"),
                                                Map.of(
                                                        "%price%", String.valueOf(itemSection.getInt("price")),
                                                        "%resource%", itemSection.getString("vault")
                                                )
                                        )
                                        .build()
                        ) {
                            @Override
                            public void onClick(Player player, ClickType clickType) {
                                PointResource pointResource = PointResource.valueOf(itemSection.getString("vault"));
                                int price = itemSection.getInt("price");

                                if (hasMoney(player, price, pointResource )){
                                    SiegeArena arena = ArenaManager.instance.getArenaOf(player);
                                    SiegePlayer siegePlayer = arena.getGame().getSiegePlayer(player);

                                    if (siegePlayer.getAbility() != ability){

                                    siegePlayer.setAbility(ability);

                                    player.getInventory().removeItem(new ItemStack(pointResource.getSpawnItem(), price));
                                    }
                                }
                                else {
                                    ChatUtil.sendConfigMessage(player, "error.not_enough_money");
                                    player.closeInventory();
                                }
                            }
                        }
                );

            }


        }
        return menu;
    }

    public  void showMenu(Player player){
        category.ShowMenu(player);
    }

    public  boolean hasMoney(Player player, int amount, PointResource pointResource){
        return player.getInventory().contains(pointResource.getSpawnItem(), amount);
    }
}
