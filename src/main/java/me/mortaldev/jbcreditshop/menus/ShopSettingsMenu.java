package me.mortaldev.jbcreditshop.menus;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.listeners.ChatListener;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopSettingsMenu extends InventoryGUI {

  private final Shop shop;
  private final boolean adminMode;

  public ShopSettingsMenu(Shop shop, boolean adminMode) {
    this.shop = shop;
    this.adminMode = adminMode;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 5 * 9, TextUtil.format("Edit Shop"));
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
    addButton(10, ShopDisplayButton());
    addButton(13, DisplayMaterialButton());
    addButton(16, DefaultPriceButton());
    addButton(28, LockButton());
    addButton(31, StyleButton());
    addButton(34, DeleteButton());
    super.decorate(player);
  }

  private InventoryButton
      DeleteButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_DYE)
                    .name("&c&lDelete")
                    .addLore("&7Delete the shop forever.")
                    .addLore()
                    .addLore("&7( click to delete )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ConfirmMenu confirmMenu =
                  new ConfirmMenu(
                      "Delete Shop? &l" + shop.getShopID(),
                      ShopManager.getInstance().getShopMenuStack(shop, false),
                      player1 -> {
                        ShopManager.getInstance().deleteShop(shop);
                        GUIManager.getInstance().openGUI(new ShopsMenu(new MenuData()), player);
                      },
                      player1 -> {
                        GUIManager.getInstance()
                            .openGUI(new ShopSettingsMenu(shop, adminMode), player);
                      });
              GUIManager.getInstance().openGUI(confirmMenu, player);
            });
  }

  private InventoryButton StyleButton() {
    Shop.Style style = shop.getStyle();
    return new InventoryButton()
        .creator(
            player -> {
              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.TURTLE_HELMET)
                      .name("&3&lStyle")
                      .addLore("&7Change the style of the shop.")
                      .addLore()
                      .addLore("&3Current: &f" + style.name());
              if (style == Shop.Style.CUSTOM) {
                builder
                    .addLore("&3Size: &f" + shop.getSize())
                    .addLore()
                    .addLore("&7( left-click to change style )")
                    .addLore("&7( right-click to change size )");
              } else {
                builder.addLore().addLore("&7( click to change style )");
              }
              ItemStack build = builder.build();
              build.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
              return build;
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.isLeftClick()) {
                Shop newShop = shop;
                switch (style) {
                  case CUSTOM -> newShop = shop.toBuilder().setStyleAsEnum(Shop.Style.AUTO).build();
                  case AUTO -> {
                    newShop = shop.toBuilder().setStyleAsEnum(Shop.Style.CUSTOM).setSize(1).build();
                  }
                }
                ShopManager.getInstance().updateShop(newShop);
                GUIManager.getInstance().openGUI(new ShopSettingsMenu(newShop, adminMode), player);
              } else {
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Change Size")
                    .itemLeft(
                        ItemStackHelper.builder(Material.TURTLE_HELMET)
                            .name(shop.getSize() + "")
                            .build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            String textEntry = stateSnapshot.getText();
                            textEntry = textEntry.trim();
                            Pattern pattern = Pattern.compile("^[1-6]\\d*$");
                            Matcher matcher = pattern.matcher(textEntry);
                            if (!matcher.matches()) {
                              Main.playDenySound(player);
                              player.sendMessage(
                                  TextUtil.format("&cInvalid inventory size! [1-6]"));
                              GUIManager.getInstance()
                                  .openGUI(new ShopSettingsMenu(shop, adminMode), player);
                              return Collections.emptyList();
                            }
                            int size = Integer.parseInt(textEntry);
                            Shop newShop = shop.toBuilder().setSize(size).build();
                            ShopManager.getInstance().updateShop(newShop);
                            GUIManager.getInstance()
                                .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                            return Collections.emptyList();
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              }
            });
  }

  private InventoryButton LockButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.TRIPWIRE_HOOK)
                    .name("&3&lLock")
                    .addLore("&7Locks the shop from being opened.")
                    .addLore()
                    .addLore("&3Status: " + (shop.isLocked() ? "&cLocked" : "&2Unlocked"))
                    .addLore(
                        "&3Bypass Perm: &f"
                            + (shop.getLockedBypassPermission() == null
                                    || shop.getLockedBypassPermission().isBlank()
                                ? "( NONE )"
                                : shop.getLockedBypassPermission()))
                    .addLore()
                    .addLore("&7( left-click to toggle lock )")
                    .addLore("&7( right-click to change permission )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case LEFT -> {
                  Shop newShop = shop.toBuilder().setLocked(!shop.isLocked()).build();
                  ShopManager.getInstance().updateShop(newShop);
                  GUIManager.getInstance()
                      .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                }
                case RIGHT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Bypass Perm")
                        .itemLeft(
                            ItemStackHelper.builder(Material.TRIPWIRE_HOOK)
                                .name(
                                    (shop.getLockedBypassPermission() == null
                                        ? " "
                                        : shop.getLockedBypassPermission()))
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                Shop newShop =
                                    shop.toBuilder().setLockedBypassPermission(textEntry).build();
                                ShopManager.getInstance().updateShop(newShop);
                                GUIManager.getInstance()
                                    .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
            });
  }

  private InventoryButton DefaultPriceButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.GOLD_NUGGET)
                    .name("&3&lDefault Price")
                    .addLore("&7The default price of items in this shop.")
                    .addLore()
                    .addLore("&3Current:&f " + shop.getDefaultPrice())
                    .addLore("&3Discount: &f" + shop.getDiscount() + "%")
                    .addLore(
                        "&3Discount Group: &f"
                            + (shop.getDiscountGroup() == null || shop.getDiscountGroup().isBlank()
                                ? "( NONE )"
                                : shop.getDiscountGroup()))
                    .addLore()
                    .addLore("&7( left-click to change price )")
                    .addLore("&7( right-click to change discount )")
                    .addLore("&7( middle-click to change discount group )")
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ClickType click = event.getClick();
              switch (click) {
                case LEFT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Default Price")
                        .itemLeft(
                            ItemStackHelper.builder(Material.GOLD_NUGGET)
                                .name(shop.getDefaultPrice() + "")
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                Pattern pattern = Pattern.compile("^\\d+$");
                                Matcher matcher = pattern.matcher(textEntry);
                                if (!matcher.matches()) {
                                  Main.playDenySound(player);
                                  player.sendMessage(TextUtil.format("&cInvalid price!"));
                                  GUIManager.getInstance()
                                      .openGUI(new ShopSettingsMenu(shop, adminMode), player);
                                  return Collections.emptyList();
                                }
                                int price;
                                try {
                                  price = Integer.parseInt(textEntry);
                                } catch (NumberFormatException e) {
                                  Main.warn(e.getMessage());
                                  return Collections.emptyList();
                                }
                                Shop newShop = shop.toBuilder().setDefaultPrice(price).build();
                                ShopManager.getInstance().updateShop(newShop);
                                GUIManager.getInstance()
                                    .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
                case RIGHT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Discount")
                        .itemLeft(
                            ItemStackHelper.builder(Material.BONE_MEAL)
                                .name(shop.getDiscount() + "")
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                Pattern pattern = Pattern.compile("^(?:100|[1-9]?\\d|0)$");
                                Matcher matcher = pattern.matcher(textEntry);
                                if (!matcher.matches()) {
                                  Main.playDenySound(player);
                                  player.sendMessage(TextUtil.format("&cInvalid discount!"));
                                  GUIManager.getInstance()
                                      .openGUI(new ShopSettingsMenu(shop, adminMode), player);
                                  return Collections.emptyList();
                                }
                                int discount;
                                try {
                                  discount = Integer.parseInt(textEntry);
                                } catch (NumberFormatException e) {
                                  Main.warn(e.getMessage());
                                  return Collections.emptyList();
                                }
                                Shop newShop = shop.toBuilder().setDiscount(discount).build();
                                ShopManager.getInstance().updateShop(newShop);
                                GUIManager.getInstance()
                                    .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
                case MIDDLE ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Discount Group")
                        .itemLeft(
                            ItemStackHelper.builder(Material.BONE_MEAL)
                                .name(
                                    (shop.getDiscountGroup() == null
                                        ? " "
                                        : shop.getDiscountGroup()))
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                Shop newShop = shop.toBuilder().setDiscountGroup(textEntry).build();
                                ShopManager.getInstance().updateShop(newShop);
                                GUIManager.getInstance()
                                    .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
            });
  }

  private InventoryButton DisplayMaterialButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ITEM_FRAME)
                    .name("&3&lDisplay Material")
                    .addLore("&7The default minecraft material the")
                    .addLore("&7items in this shop will be if not set.")
                    .addLore()
                    .addLore("&3Current:&f " + shop.getDefaultDisplayMaterial().name())
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
                          .name(shop.getDefaultDisplayMaterial().name())
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText();
                          textEntry = textEntry.trim();
                          try {
                            Material material = Material.valueOf(textEntry);
                            Shop newShop =
                                shop.toBuilder().setDefaultDisplayMaterial(material).build();
                            ShopManager.getInstance().updateShop(newShop);
                            GUIManager.getInstance()
                                .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                          } catch (IllegalArgumentException e) {
                            Main.playDenySound(player);
                            player.sendMessage(TextUtil.format("&cInvalid material!"));
                            GUIManager.getInstance()
                                .openGUI(new ShopSettingsMenu(shop, adminMode), player);
                          }
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton ShopDisplayButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.NAME_TAG)
                    .name("&3&lShop Display")
                    .addLore("&7The text that displays as")
                    .addLore("&7the title of the shop.")
                    .addLore()
                    .addLore("&3Current:")
                    .addLore("&f" + shop.getShopDisplay())
                    .addLore()
                    .addLore("&7( click to change )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (ChatListener.hasConsumer(player.getUniqueId())) {
                ChatListener.removeConsumer(player.getUniqueId());
              }
              player.sendMessage(TextUtil.format("&3Enter the &fnew shop display&3: [30 seconds]"));
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
                                  .openGUI(new ShopSettingsMenu(shop, adminMode), player);
                            }
                          },
                          20L * 30);
              ChatListener.addConsumer(
                  player.getUniqueId(),
                  (message) -> {
                    String string = TextUtil.componentToString(message);
                    Shop newShop = shop.toBuilder().setShopDisplay(string).build();
                    ShopManager.getInstance().updateShop(newShop);
                    GUIManager.getInstance()
                        .openGUI(new ShopSettingsMenu(newShop, adminMode), player);
                    Bukkit.getScheduler().cancelTask(task);
                  });
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
              ShopManager.getInstance().openShop(shop, player, adminMode);
            });
  }
}
