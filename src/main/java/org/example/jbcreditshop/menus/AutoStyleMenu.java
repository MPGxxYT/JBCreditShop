package org.example.jbcreditshop.menus;

import java.util.Set;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.example.jbcreditshop.modules.MenuData;
import org.example.jbcreditshop.modules.Shop;
import org.example.jbcreditshop.modules.ShopItem;
import org.example.jbcreditshop.modules.ShopItemsManager;
import org.example.jbcreditshop.utils.TextUtil;
import org.example.jbcreditshop.utils.Utils;

public class AutoStyleMenu extends InventoryGUI {

  private final Shop shop;
  private final Set<ShopItem> shopItem;
  private final boolean adminMode;
  private MenuData menuData;

  public AutoStyleMenu(Shop shop, boolean adminMode, MenuData menuData) {
    this.adminMode = adminMode;
    this.shop = shop;
    this.shopItem = ShopItemsManager.getInstance().getByShopID(shop.getShopID(), false);
    this.menuData = menuData;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(shop.getShopDisplay()));
  }

  private int getSize() {
    return Utils.clamp((int) Math.ceil(shopItem.size() / 9.0), 1, 6);
  }

  @Override
  public void decorate(Player player) {

  super.decorate(player);
  }

}
