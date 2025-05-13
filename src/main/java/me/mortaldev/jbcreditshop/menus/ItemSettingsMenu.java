package me.mortaldev.jbcreditshop.menus;

import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemSettingsMenu extends InventoryGUI {
  private final Shop shop;
  private final ShopItem shopItem;

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 5 * 9, TextUtil.format("Item Settings"));
  }

  public ItemSettingsMenu(Shop shop, ShopItem shopItem) {
    this.shop = shop;
    this.shopItem = shopItem;
  }

  @Override
  public void decorate(Player player) {
    ItemStack glass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, glass);
      getInventory().setItem(i + 36, glass);
      if (i < 5 && i > 0) {
        getInventory().setItem(i * 9, glass);
        getInventory().setItem((i * 9) - 1, glass);
      }
    }
    addButton(0, BackButton());
    super.decorate(player);
  }

  private InventoryButton BackButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ShopManager.getInstance().openShop(shop, player, true);
            });
  }
}
