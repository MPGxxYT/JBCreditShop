package me.mortaldev.jbcreditshop.menus;

import java.util.*;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.*;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class CustomStyleMenu extends InventoryGUI {

  private final Shop shop;
  private final Set<ShopItem> shopItems;
  private final HashMap<Integer, ShopItem> slotted = new HashMap<>();
  boolean adminMode;

  public CustomStyleMenu(Shop shop, boolean adminMode) {
    this.adminMode = adminMode;
    this.shop = shop;
    this.shopItems = ShopItemsManager.getInstance().getByShopID(shop.getShopID(), false);
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
      if (shopItem.cannotBeDisplayed() || !shopItem.isVisible()) {
        if (!adminMode) {
          continue;
        }
      }
      int shopSlot = shopItem.getShopSlot();
      if (slotted.containsKey(shopSlot)) {
        nonSlotted.add(shopItem);
        continue;
      }
      slotted.put(shopSlot, shopItem);
    }
    int slot = 0;
    for (ShopItem shopItem : nonSlotted) {
      while (slotted.containsKey(slot)) {
        slot++;
      }
      slotted.put(slot, shopItem);
    }
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
      if (shopItem.cannotBeDisplayed() || !shopItem.isVisible()) {
        if (!adminMode) {
          continue;
        }
      }
      addButton(slot, ShopItemButton(shopItem));
    }
    super.decorate(player);
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    super.onClick(event);
    if (!adminMode) {
      return;
    }
    if (event.getView().getTopInventory() == event.getClickedInventory()) {
      if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
        Player player = (Player) event.getWhoClicked();
        new AnvilGUI.Builder()
            .plugin(Main.getInstance())
            .title("Add Item")
            .itemLeft(ItemStackHelper.builder(Material.PAPER).name("id").build())
            .onClick(
                (slot, stateSnapshot) -> {
                  if (slot == 2) {
                    String textEntry = stateSnapshot.getText();
                    textEntry = textEntry.trim();
                    if (textEntry.isBlank()) {
                      player.sendMessage("&cMust enter an id for the item.");
                      Main.playDenySound(player);
                      ShopManager.getInstance().openShop(shop, player, true);
                      return Collections.emptyList();
                    }
                    textEntry = textEntry.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
                    ShopItem shopItem =
                        ShopItem.builder()
                            .setItemID(textEntry)
                            .setDisplayName("&7" + textEntry)
                            .setDisplayMaterial(shop.getDefaultDisplayMaterial())
                            .setPurchasedItem("")
                            .setVisible(false)
                            .setShopID(shop.getShopID())
                            .setShopSlot(event.getSlot())
                            .build();
                    boolean b = ShopItemsManager.getInstance().addShopItem(shopItem);
                    if (!b) {
                      player.sendMessage(TextUtil.format("&cItem by ID already exists.!"));
                      Main.playDenySound(player);
                      GUIManager.getInstance()
                          .openGUI(new CustomStyleMenu(shop, adminMode), player);
                      return Collections.emptyList();
                    }
                    GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, shopItem), player);
                  }
                  return Collections.emptyList();
                })
            .open(player);
      }
    }
  }

  private InventoryButton ShopItemButton(ShopItem shopItem) {
    return new InventoryButton()
        .creator(
            player -> ShopItemsManager.getInstance().getShopMenuStack(shopItem, adminMode, player))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (adminMode) {
                if (event.isRightClick()) {
                  GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, shopItem), player);
                }
                return;
              }
              if (ShopItemsManager.getInstance().canAllowPurchase(shopItem, player)) {
                ConfirmMenu confirmMenu =
                    new ConfirmMenu(
                        "&e&lPurchase " + shopItem.getDisplayName() + "?",
                        ShopItemsManager.getInstance()
                            .getShopMenuStack(shopItem, false, player, false),
                        (player1) -> {
                          ShopItemsManager.getInstance().purchaseShopItem(shopItem, player);
                          GUIManager.getInstance()
                              .openGUI(new CustomStyleMenu(shop, false), player);
                        },
                        (player1) -> {
                          GUIManager.getInstance()
                              .openGUI(new CustomStyleMenu(shop, false), player);
                        });
                GUIManager.getInstance().openGUI(confirmMenu, player);
              }
            });
  }
}
