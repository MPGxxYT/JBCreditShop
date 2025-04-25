package org.example.jbcreditshop.menus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.example.jbcreditshop.Main;
import org.example.jbcreditshop.ecobits.EcoBitsCurrency;
import org.example.jbcreditshop.modules.Shop;
import org.example.jbcreditshop.modules.ShopItem;
import org.example.jbcreditshop.modules.ShopItemsManager;
import org.example.jbcreditshop.utils.TextUtil;
import org.example.jbcreditshop.utils.Utils;

public class CustomStyleMenu extends InventoryGUI {

  private final Shop shop;
  private final Set<ShopItem> shopItems;
  private final HashMap<Integer, ShopItem> slotted = new HashMap<>();
  boolean adminMode;

  public CustomStyleMenu(Shop shop, boolean adminMode) {
    this.adminMode = adminMode;
    this.shop = shop;
    shopItems = ShopItemsManager.getInstance().getByShopID(shop.getShopID(), false);
    sortSlots();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(shop.getShopDisplay()));
  }

  private int getSize() {
    int size = shop.getSize();
    if (size < 1 || size > 6) {
      return autoGenSize();
    }
    return size;
  }

  private int sortSlots() {
    Set<ShopItem> nonSlotted = new HashSet<>();
    for (ShopItem shopItem : shopItems) {
      if (!shopItem.canBeDisplayed()) {
        if (!adminMode) {
          continue;
        }
      }
      int shopSlot = shopItem.getShopSlot();
      if (slotted.containsKey(shopSlot)) {
        nonSlotted.add(shopItem);
      }
      slotted.put(shopSlot, shopItem);
    }
    nonSlotted.forEach(
        shopItem -> {
          System.out.println("shopItem.getItemID() = " + shopItem.getItemID());
        });
    int slot = 0;
    for (ShopItem shopItem : nonSlotted) {
      while (slotted.containsKey(slot)) {
        slot++;
      }
      slotted.put(slot, shopItem);
    }
    slotted.forEach(
        (shopSlot, shopItem) -> {
          System.out.println("shopSlot = " + shopSlot);
          System.out.println("shopItem.getItemID() = " + shopItem.getItemID());
        });
    return slot;
  }

  private int autoGenSize() {
    return Utils.clamp((int) Math.ceil(slotted.size() / 9.0), 1, 6);
  }

  @Override
  public void decorate(Player player) {
    for (Map.Entry<Integer, ShopItem> entry : slotted.entrySet()) {
      int slot = entry.getKey();
      ShopItem shopItem = entry.getValue();
      if (!shopItem.canBeDisplayed()) {
        if (!adminMode) {
          continue;
        }
      }
      addButton(slot, ShopItemButton(shopItem));
    }
    super.decorate(player);
  }

  private InventoryButton ShopItemButton(ShopItem shopItem) {
    return new InventoryButton()
        .creator(
            player ->
                ShopItemsManager.getInstance().getShopMenuStack(shopItem, adminMode, player))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              String purchasedPermission = shopItem.getPurchasedPermission();
              if (!purchasedPermission.isBlank()) {
                if (player.hasPermission(purchasedPermission)) {
                  player.sendMessage(TextUtil.format("&cYou already own this!"));
                  Main.playDenySound(player);
                  return;
                }
              }
              BigDecimal currentBalance = new EcoBitsCurrency(player).getCurrentBalance();
              if (currentBalance.compareTo(BigDecimal.valueOf(shopItem.getPrice())) < 0) {
                player.sendMessage(
                    TextUtil.format("&cYou cannot afford this. Not enough credits!"));
                Main.playDenySound(player);
                return;
              }
              ShopItemsManager.getInstance().purchaseShopItem(shopItem, player);
              player.closeInventory();
            });
  }
}
