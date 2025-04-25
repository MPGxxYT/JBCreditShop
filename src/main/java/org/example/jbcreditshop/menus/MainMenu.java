package org.example.jbcreditshop.menus;

import java.util.Set;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.example.jbcreditshop.modules.Shop;
import org.example.jbcreditshop.modules.ShopManager;
import org.example.jbcreditshop.utils.TextUtil;
import org.example.jbcreditshop.utils.Utils;

public class MainMenu extends InventoryGUI {

  private final Set<Shop> shops;

  public MainMenu() {
    this.shops = ShopManager.getInstance().getShops();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format("Main Menu"));
  }

  private int getSize() {
    return Utils.clamp((int) Math.ceil(shops.size() / 9.0), 1, 6);
  }

  @Override
  public void decorate(Player player) {
    int slot = 0;
    for (Shop shop : shops) {
      addButton(slot, ShopButton(shop));
      if (slot == 9 * getSize()) {
        break;
      }
      slot++;
    }
    super.decorate(player);
  }

  private InventoryButton ShopButton(Shop shop) {
    return new InventoryButton()
        .creator(
            player -> ShopManager.getInstance().getShopMenuStack(shop))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ShopManager.getInstance().openShop(shop, player, false);
            });
  }


}
