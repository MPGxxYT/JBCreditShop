package org.example.jbcreditshop.modules;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.jbcreditshop.ecobits.EcoBitsCurrency;
import org.example.jbcreditshop.utils.ItemStackHelper;
import org.example.jbcreditshop.utils.TextUtil;
import org.example.jbcreditshop.utils.Utils;
import org.example.jbcreditshop.yaml.ShopItemsYaml;

public class ShopItemsManager {

  public static final String NOT_ENOUGH_SPACE_MESSAGE = "&cNot enough space in your inventory.";
  Set<ShopItem> shopItems = new HashSet<>();
  HashMap<String, Set<ShopItem>> shopItemsByShopID = new HashMap<>();

  public ItemStack getShopMenuStack(ShopItem shopItem, boolean adminMode, Player player) {
    Material display = shopItem.getDisplayMaterial();
    if (display == null || display.isAir()) {
      display =
          ShopManager.getInstance().getShop(shopItem.getShopID()).get().getDefaultDisplayMaterial();
    }
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(display).name(shopItem.getDisplayName());
    if (adminMode) {
      builder
          .addLore("&3&lID: &f" + shopItem.getItemID())
          .addLore("&3&lShop ID: &f" + shopItem.getShopID());
    }
    if (!shopItem.getDescription().isEmpty()) {
      builder.addLore(shopItem.getDescription()).addLore("");
    }
    if (adminMode) {
      builder
          .addLore("&3&lLocked: &f" + shopItem.isLocked())
          .addLore("&3&lVisible: &f" + shopItem.canBeDisplayed());
    } else {
      String purchasedPermission = shopItem.getPurchasedPermission();
      if (!purchasedPermission.isBlank()) {
        if (player.hasPermission(purchasedPermission)) {
          builder.addLore("&cYou already own this. (one time purchase)");
        }
        return builder.build();
      }
      builder
          .addLore("&3&lPrice: &f" + shopItem.getPrice())
          .addLore("")
          .addLore("&7 ( click to purchase )");
    }
    return builder.build();
  }

  private static class Singleton {
    private static final ShopItemsManager INSTANCE = new ShopItemsManager();
  }

  public static synchronized ShopItemsManager getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopItemsManager() {}

  public void loadShopItems() {
    shopItems = ShopItemsYaml.getInstance().read();
    sortByShop();
  }

  public void sortByShop() {
    for (ShopItem shopItem : shopItems) {
      if (!shopItemsByShopID.containsKey(shopItem.getShopID())) {
        shopItemsByShopID.put(shopItem.getShopID(), new HashSet<>(){{add(shopItem);}});
      }
      shopItemsByShopID.get(shopItem.getShopID()).add(shopItem);
    }
  }

  public Set<ShopItem> getShopItems() {
    return shopItems;
  }

  public ShopItem getShopItem(String name) {
    for (ShopItem shopItem : shopItems) {
      if (shopItem.getItemID().equalsIgnoreCase(name)) {
        return shopItem;
      }
    }
    return null;
  }

  public Set<ShopItem> getByShopID(String shopID, boolean sortByShop) {
    if (sortByShop) {
      sortByShop();
    }
    Set<ShopItem> items = new HashSet<>();
    for (ShopItem shopItem : shopItems) {
      if (shopItem.getShopID().equalsIgnoreCase(shopID)) {
        items.add(shopItem);
      }
    }
    return items;
  }

  public void addShopItem(ShopItem shopItem) {
    shopItems.add(shopItem);
    ShopItemsYaml.getInstance().add(shopItem);
  }

  public void purchaseShopItem(ShopItem shopItem, Player player) {
    if (!shopItem.canBeDisplayed()) {
      player.sendMessage(TextUtil.format("&cThis item is not for sale."));
      return;
    }
    if (!shopItem.getPurchasedCommand().isBlank()) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), shopItem.getPurchasedCommand().replaceFirst("/", "").replace("%player%", player.getName()));
    }
    if (shopItem.getPurchasedItem() != null && !shopItem.getPurchasedItem().getType().isAir()) {
      if (!Utils.canInventoryHold(player.getInventory(), shopItem.getPurchasedItem())) {
        player.sendMessage(TextUtil.format(NOT_ENOUGH_SPACE_MESSAGE));
        return;
      } else {
        player.getInventory().addItem(shopItem.getPurchasedItem());
      }
    }
    if (!shopItem.getPurchasedPermission().isBlank()) {
      // TODO: Use luckperms API to give permission
      player.sendMessage(TextUtil.format("&7 Would give permission " + shopItem.getPurchasedPermission().replace("%player%", player.getName())));
    }
    player.sendMessage(TextUtil.format("&3Purchased " + shopItem.getDisplayName() + " &3for &l" + shopItem.getPrice() + "&3 credits."));
    new EcoBitsCurrency(player).remove(BigDecimal.valueOf(shopItem.getPrice()));
  }
}
