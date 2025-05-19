package me.mortaldev.jbcreditshop.menus;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.listeners.ChatListener;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.modules.ShopManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemDisplaySettingsMenu extends InventoryGUI {
  private final Shop shop;
  private final ShopItem shopItem;

  public ItemDisplaySettingsMenu(Shop shop, ShopItem shopItem) {
    this.shop = shop;
    this.shopItem = shopItem;
    allowBottomInventoryClick(true);
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("Display Settings"));
  }

  @Override
  public void decorate(Player player) {
    addButton(0, BackButton());
    addButton(10, DisplayMaterialButton());
    addButton(12, DescriptionButton());
    addButton(14, DisplayNameButton());
    addButton(16, UseItemStackButton());
    super.decorate(player);
  }

  private InventoryButton UseItemStackButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.BOWL)
                      .name("&3&lUse Item Stack")
                      .addLore("&7Click with item")
                      .addLore("&7to match this to it.")
                      .addLore();
              ItemStack displayItemStack = shopItem.getDisplayItemStack();
              builder.addLore("&3Current: " + (displayItemStack == null ? "&fNone" : "&aSet:&f " + Utils.itemName(displayItemStack)));
              if (displayItemStack != null) {
                builder.addLore(
                    "&3Display As Item Stack: "
                        + (shopItem.isUseDisplayItemStack() ? "&aYes" : "&cNo"));
              }
              return builder
                  .addLore()
                  .addLore("&7( click with item to match/add )")
                  .addLore("&7( right-click to toggle display as item stack )")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ItemStack cursor = event.getCursor();
              if (cursor != null) {
                cursor = cursor.clone();
              }
              if (cursor == null || cursor.getType().isAir()) {
                if (event.isRightClick()) {
                  if (shopItem.getDisplayItemStack() == null) {
                    return;
                  }
                  ShopItem build =
                      shopItem.toBuilder()
                          .setUseDisplayItemStack(!shopItem.isUseDisplayItemStack())
                          .build();
                  ShopItemsManager.getInstance().updateShopItem(build);
                  GUIManager.getInstance()
                      .openGUI(new ItemDisplaySettingsMenu(shop, build), player);
                }
                return;
              }
              ShopItem.Builder builder = ShopItem.builder(shopItem);
              if (cursor.getItemMeta() == null || !cursor.hasItemMeta()) {
                builder.setDisplayName(Utils.itemName(cursor)).setDescription(new ArrayList<>());
              } else {
                builder =
                    shopItem.toBuilder()
                        .setDisplayName(TextUtil.deformat(cursor.getItemMeta().displayName()));
                if (cursor.getItemMeta().lore() != null && !cursor.getItemMeta().lore().isEmpty()) {
                  List<String> collect =
                      cursor.getItemMeta().lore().stream().map(TextUtil::deformat).toList();
                  builder.setDescription(collect);
                } else {
                  builder.setDescription(new ArrayList<>());
                }
              }
              ShopItem newShopItem =
                  builder.setDisplayMaterial(cursor.getType()).setDisplayItemStack(cursor).build();
              ShopItemsManager.getInstance().updateShopItem(newShopItem);
              GUIManager.getInstance()
                  .openGUI(new ItemDisplaySettingsMenu(shop, newShopItem), player);
            });
  }

  private InventoryButton DisplayNameButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.NAME_TAG)
                    .name("&3&lDisplay Name")
                    .addLore("&7The unique display name this item has.")
                    .addLore()
                    .addLore("&3Current:")
                    .addLore(shopItem.getDisplayName())
                    .addLore()
                    .addLore("&7( click to change )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.sendMessage(TextUtil.format("&3Enter a display name for this item: &f[30 seconds]"));
              player.closeInventory();
              int task =
                  Bukkit.getScheduler()
                      .scheduleSyncDelayedTask(
                          Main.getInstance(),
                          () -> {
                            if (ChatListener.hasConsumer(player.getUniqueId())) {
                              ChatListener.removeConsumer(player.getUniqueId());
                              player.sendMessage(TextUtil.format("&cTimed out!"));
                              Main.playDenySound(player);
                              GUIManager.getInstance()
                                  .openGUI(new ItemDisplaySettingsMenu(shop, shopItem), player);
                            }
                          },
                          20L * 30);
              ChatListener.addConsumer(
                  player.getUniqueId(),
                  component -> {
                    String string = TextUtil.componentToString(component);
                    ShopItem build = shopItem.toBuilder().setDisplayName(string).build();
                    ShopItemsManager.getInstance().updateShopItem(build);
                    GUIManager.getInstance()
                        .openGUI(new ItemDisplaySettingsMenu(shop, build), player);
                    Bukkit.getScheduler().cancelTask(task);
                  });
            });
  }

  private InventoryButton DescriptionButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.PAPER)
                      .name("&3&lDescription")
                      .addLore("&7The text/lore below the item.")
                      .addLore()
                      .addLore("&3Current:");
              if (shopItem.getDescription().isEmpty()) {
                builder.addLore("&f( NONE )");
              } else {
                shopItem.getDescription().forEach(builder::addLore);
              }
              builder.addLore().addLore("&7( click to change )");
              return builder.build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.sendMessage(TextUtil.format("&3Enter a description for this item (use ';;' between lines): &f[30 seconds]"));
              player.closeInventory();
              int task =
                  Bukkit.getScheduler()
                      .scheduleSyncDelayedTask(
                          Main.getInstance(),
                          () -> {
                            if (ChatListener.hasConsumer(player.getUniqueId())) {
                              ChatListener.removeConsumer(player.getUniqueId());
                              player.sendMessage(TextUtil.format("&cTimed out!"));
                              Main.playDenySound(player);
                              GUIManager.getInstance()
                                  .openGUI(new ItemDisplaySettingsMenu(shop, shopItem), player);
                            }
                          },
                          20L * 30);
              ChatListener.addConsumer(
                  player.getUniqueId(),
                  component -> {
                    String string = TextUtil.componentToString(component);
                    String[] split = string.split(";;");
                    List<String> collect = Arrays.stream(split).toList();
                    ShopItem build = shopItem.toBuilder().setDescription(collect).build();
                    ShopItemsManager.getInstance().updateShopItem(build);
                    GUIManager.getInstance()
                        .openGUI(new ItemDisplaySettingsMenu(shop, build), player);
                    Bukkit.getScheduler().cancelTask(task);
                  });
            });
  }

  private InventoryButton DisplayMaterialButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ITEM_FRAME)
                    .name("&3&lDisplay Material")
                    .addLore("&7The minecraft material this item will be.")
                    .addLore()
                    .addLore("&3Current Material: &f" + shopItem.getDisplayMaterial().name())
                    .addLore()
                    .addLore("&7( click to change )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Display Material")
                  .itemLeft(
                      ItemStackHelper.builder(Material.ITEM_FRAME)
                          .name(shopItem.getDisplayMaterial().name())
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText();
                          textEntry = textEntry.trim();
                          try {
                            Material material = Material.valueOf(textEntry);
                            ShopItem newShopItem =
                                shopItem.toBuilder().setDisplayMaterial(material).build();
                            ShopItemsManager.getInstance().updateShopItem(newShopItem);
                            GUIManager.getInstance()
                                .openGUI(new ItemDisplaySettingsMenu(shop, newShopItem), player);
                          } catch (IllegalArgumentException e) {
                            Main.playDenySound(player);
                            player.sendMessage(TextUtil.format("&cInvalid material!"));
                            GUIManager.getInstance()
                                .openGUI(new ItemDisplaySettingsMenu(shop, shopItem), player);
                          }
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
