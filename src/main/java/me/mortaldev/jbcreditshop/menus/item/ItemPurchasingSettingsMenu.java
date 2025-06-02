package me.mortaldev.jbcreditshop.menus.item;

import java.util.Collections;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemPurchasingSettingsMenu extends InventoryGUI {
  private final Shop shop;
  private final ShopItem shopItem;

  public ItemPurchasingSettingsMenu(Shop shop, ShopItem shopItem) {
    this.shop = shop;
    this.shopItem = shopItem;
    allowBottomInventoryClick(true);
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("Purchasing Settings"));
  }

  @Override
  public void decorate(Player player) {
    addButton(0, BackButton());
    addButton(10, PurchasedItemButton());
    addButton(12, PurchasedCommandButton());
    addButton(14, PurchasedPermissionButton());
    addButton(16, OneTimePurchaseButton());
    super.decorate(player);
  }

  private InventoryButton OneTimePurchaseButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.SNOWBALL)
                    .name("&3&lOne-Time Purchase")
                    .addLore("&7Choose if you want this to")
                    .addLore("&7be only purchasable once.")
                    .addLore()
                    .addLore("&7Status: " + (shopItem.isOneTimePurchase() ? "&aTrue" : "&cFalse"))
                    .addLore("")
                    .addLore("&7( left-click to toggle )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ShopItem build =
                  shopItem.toBuilder().setOneTimePurchase(!shopItem.isOneTimePurchase()).build();
              ShopItemsManager.getInstance().updateShopItem(build);
              GUIManager.getInstance().openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
            });
  }

  private InventoryButton PurchasedPermissionButton() {
    String purchasedPermission = shopItem.getPurchasedPermission();
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.CONDUIT)
                    .name("&3&lPurchased Permission")
                    .addLore("&7Gives this permission to")
                    .addLore("&7the player when purchased.")
                    .addLore()
                    .addLore("&3Current:")
                    .addLore(
                        purchasedPermission == null || purchasedPermission.isBlank()
                            ? "&f( NONE )"
                            : "&f" + purchasedPermission)
                    .addLore("")
                    .addLore("&7( left-click to set )")
                    .addLore("&7( right-click to clear )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case RIGHT -> {
                  ShopItem build = shopItem.toBuilder().setPurchasedPermission("").build();
                  if (build.cannotBeDisplayed()) {
                    build = shopItem.toBuilder().setVisible(false).build();
                  }
                  ShopItemsManager.getInstance().updateShopItem(build);
                  GUIManager.getInstance()
                      .openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
                }
                case LEFT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Purchased Permission")
                        .itemLeft(
                            ItemStackHelper.builder(Material.CONDUIT)
                                .name(
                                    purchasedPermission == null || purchasedPermission.isBlank()
                                        ? " "
                                        : purchasedPermission)
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                ShopItem build =
                                    shopItem.toBuilder().setPurchasedPermission(textEntry).build();
                                ShopItemsManager.getInstance().updateShopItem(build);
                                GUIManager.getInstance()
                                    .openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
                                return Collections.emptyList();
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
            });
  }

  private InventoryButton PurchasedCommandButton() {
    String purchasedCommand = shopItem.getPurchasedCommand();
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.PAPER)
                    .name("&3&lPurchased Command")
                    .addLore("&7This command will run when")
                    .addLore("&7a player purchases this item.")
                    .addLore()
                    .addLore("&7&o[Use %player% for placeholder]")
                    .addLore()
                    .addLore("&3Current:")
                    .addLore(
                        purchasedCommand == null || purchasedCommand.isBlank()
                            ? "&f( NONE )"
                            : "&f" + purchasedCommand)
                    .addLore("")
                    .addLore("&7( left-click to set )")
                    .addLore("&7( right-click to clear )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case RIGHT -> {
                  ShopItem build = shopItem.toBuilder().setPurchasedCommand("").build();
                  if (build.cannotBeDisplayed()) {
                    build = shopItem.toBuilder().setVisible(false).build();
                  }
                  ShopItemsManager.getInstance().updateShopItem(build);
                  GUIManager.getInstance()
                      .openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
                }
                case LEFT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Purchased Command")
                        .itemLeft(
                            ItemStackHelper.builder(Material.PAPER)
                                .name(
                                    purchasedCommand == null || purchasedCommand.isBlank()
                                        ? " "
                                        : purchasedCommand)
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                ShopItem build =
                                    shopItem.toBuilder().setPurchasedCommand(textEntry).build();
                                ShopItemsManager.getInstance().updateShopItem(build);
                                GUIManager.getInstance()
                                    .openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
                                return Collections.emptyList();
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
            });
  }

  private InventoryButton PurchasedItemButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStack purchasedItem = shopItem.getPurchasedItem();
              return ItemStackHelper.builder(Material.GLOW_ITEM_FRAME)
                  .name("&3&lPurchased Item")
                  .addLore("&7The item to receive when purchased.")
                  .addLore()
                  .addLore("&3Current:")
                  .addLore(
                      purchasedItem == null || purchasedItem.getType().isAir()
                          ? "&f( NONE )"
                          : "&f"
                              + purchasedItem.getAmount()
                              + " of "
                              + purchasedItem.getType().name()
                              + " named "
                              + (purchasedItem.hasItemMeta()
                                      && purchasedItem.getItemMeta().hasDisplayName()
                                  ? TextUtil.deformat(purchasedItem.getItemMeta().displayName())
                                  : Utils.itemName(purchasedItem)))
                  .addLore("")
                  .addLore("&7( left-click to set )")
                  .addLore("&7( right-click to clear )")
                  .addLore("&7( middle-click to get item )")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ItemStack cursor = event.getCursor();
              if (cursor == null || cursor.getType().isAir()) {
                switch (event.getClick()) {
                  case RIGHT -> {
                    ShopItem build = shopItem.toBuilder().setPurchasedItem("").build();
                    if (build.cannotBeDisplayed()) {
                      build = shopItem.toBuilder().setVisible(false).build();
                    }
                    ShopItemsManager.getInstance().updateShopItem(build);
                    GUIManager.getInstance()
                        .openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
                  }
                  case MIDDLE -> {
                    ItemStack purchasedItem = shopItem.getPurchasedItem();
                    if (purchasedItem == null || purchasedItem.getType().isAir()) {
                      return;
                    }
                    player.getInventory().addItem(purchasedItem);
                    GUIManager.getInstance()
                        .openGUI(new ItemPurchasingSettingsMenu(shop, shopItem), player);
                  }
                }
                return;
              }
              cursor = cursor.clone();
              ShopItem build = shopItem.toBuilder().setPurchasedItem(cursor).build();
              ShopItemsManager.getInstance().updateShopItem(build);
              GUIManager.getInstance().openGUI(new ItemPurchasingSettingsMenu(shop, build), player);
            });
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
              GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, shopItem), player);
            });
  }
}
