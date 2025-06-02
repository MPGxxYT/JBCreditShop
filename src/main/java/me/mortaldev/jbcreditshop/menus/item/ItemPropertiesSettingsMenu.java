package me.mortaldev.jbcreditshop.menus.item;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ItemPropertiesSettingsMenu extends InventoryGUI {
  private final Shop shop;
  private final ShopItem shopItem;

  public ItemPropertiesSettingsMenu(Shop shop, ShopItem shopItem) {
    this.shop = shop;
    this.shopItem = shopItem;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("Property Settings"));
  }

  @Override
  public void decorate(Player player) {
    addButton(0, BackButton());
    addButton(10, ShopSlotButton());
    addButton(13, OwnedPermissionButton());
    addButton(16, GroupButton());
    super.decorate(player);
  }

  private InventoryButton GroupButton() {
    String group = shopItem.getGroup();
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.CHEST)
                    .name("&3&lGroup")
                    .addLore("&7The group this item is in.")
                    .addLore("&7Used for discounts, sorting, ect.")
                    .addLore()
                    .addLore("&3Current:&f " + (group.isBlank() ? "( NONE )" : group))
                    .addLore()
                    .addLore("&7( left-click to set )")
                    .addLore("&7( right-click to clear )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case RIGHT -> {
                  ShopItem newShopItem = shopItem.toBuilder().setGroup("").build();
                  ShopItemsManager.getInstance().updateShopItem(newShopItem);
                  GUIManager.getInstance()
                      .openGUI(new ItemPropertiesSettingsMenu(shop, newShopItem), player);
                }
                case LEFT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Group")
                        .itemLeft(
                            ItemStackHelper.builder(Material.CHEST)
                                .name(group.isBlank() ? " " : group)
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                if (textEntry.isBlank()) {
                                  GUIManager.getInstance()
                                      .openGUI(
                                          new ItemPropertiesSettingsMenu(shop, shopItem), player);
                                  return Collections.emptyList();
                                }
                                ShopItem newShopItem =
                                    shopItem.toBuilder().setGroup(textEntry).build();
                                ShopItemsManager.getInstance().updateShopItem(newShopItem);
                                GUIManager.getInstance()
                                    .openGUI(
                                        new ItemPropertiesSettingsMenu(shop, newShopItem), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
            });
  }

  private InventoryButton OwnedPermissionButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.PAPER)
                    .name("&3&lOwned Permission")
                    .addLore("&7The permission a player would")
                    .addLore("&7have that shows they own this item.")
                    .addLore("&7This is used for the OWNED sorting option.")
                    .addLore()
                    .addLore("&3Current:")
                    .addLore(
                        shopItem.getPermission().isBlank()
                            ? "&f( NONE )"
                            : "&f" + shopItem.getPermission())
                    .addLore()
                    .addLore("&7( left-click to set )")
                    .addLore("&7( right-click to clear )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case RIGHT -> {
                  ShopItem newShopItem = shopItem.toBuilder().setPermission("").build();
                  ShopItemsManager.getInstance().updateShopItem(newShopItem);
                  GUIManager.getInstance()
                      .openGUI(new ItemPropertiesSettingsMenu(shop, newShopItem), player);
                }
                case LEFT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Owned Permission")
                        .itemLeft(
                            ItemStackHelper.builder(Material.PAPER)
                                .name(
                                    shopItem.getPermission().isBlank()
                                        ? " "
                                        : shopItem.getPermission())
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                if (textEntry.isBlank()) {
                                  GUIManager.getInstance()
                                      .openGUI(
                                          new ItemPropertiesSettingsMenu(shop, shopItem), player);
                                  return Collections.emptyList();
                                }
                                ShopItem newShopItem =
                                    shopItem.toBuilder().setPermission(textEntry).build();
                                ShopItemsManager.getInstance().updateShopItem(newShopItem);
                                GUIManager.getInstance()
                                    .openGUI(
                                        new ItemPropertiesSettingsMenu(shop, newShopItem), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
            });
  }

  private InventoryButton ShopSlotButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.FLOWER_POT)
                    .name("&3&lShop Slot")
                    .addLore("&7The slot this item will be")
                    .addLore("&7in when in a CUSTOM menu.")
                    .addLore("")
                    .addLore("&3Current: &f" + shopItem.getShopSlot())
                    .addLore()
                    .addLore("&7( click to change )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();

              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Shop Slot")
                  .itemLeft(
                      ItemStackHelper.builder(Material.FLOWER_POT)
                          .name(shopItem.getShopSlot() + "")
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText();
                          textEntry = textEntry.trim();
                          Pattern pattern = Pattern.compile("^\\d+$");
                          Matcher matcher = pattern.matcher(textEntry);
                          if (!matcher.matches()) {
                            player.sendMessage(TextUtil.format("&cInvalid Number!"));
                            Main.playDenySound(player);
                            GUIManager.getInstance()
                                .openGUI(new ItemPropertiesSettingsMenu(shop, shopItem), player);
                            return Collections.emptyList();
                          }
                          int shopSlot = Integer.parseInt(textEntry);
                          if (shopSlot < 0 || shopSlot > 53) {
                            player.sendMessage(TextUtil.format("&cInvalid Number!"));
                            Main.playDenySound(player);
                            GUIManager.getInstance()
                                .openGUI(new ItemPropertiesSettingsMenu(shop, shopItem), player);
                            return Collections.emptyList();
                          }
                          ShopItem newShopItem = shopItem.toBuilder().setShopSlot(shopSlot).build();
                          ShopItemsManager.getInstance().updateShopItem(newShopItem);
                          GUIManager.getInstance()
                              .openGUI(new ItemPropertiesSettingsMenu(shop, newShopItem), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
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
