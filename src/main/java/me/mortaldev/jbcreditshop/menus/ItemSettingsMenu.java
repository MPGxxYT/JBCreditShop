package me.mortaldev.jbcreditshop.menus;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    addButton(10, LockButton());
    addButton(12, PriceButton());
    addButton(14, VisiblityButton());
    addButton(16, ArchiveButton());
    addButton(28, DisplayButton());
    addButton(30, PurchasingButton());
    addButton(32, PropertiesButton());
    addButton(34, DeleteButton());
    super.decorate(player);
  }

  private InventoryButton DeleteButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_DYE)
                    .name("&c&lDELETE")
                    .addLore("&7Delete this item.")
                    .addLore()
                    .addLore("&7( click to delete )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ConfirmMenu confirmMenu =
                  new ConfirmMenu(
                      "Delete Item? &l" + shopItem.getItemID(),
                      ShopItemsManager.getInstance().getBasicShopMenuStack(shopItem),
                      player1 -> {
                        ShopItemsManager.getInstance().deleteShopItem(shopItem);
                        ShopManager.getInstance().openShop(shop, player, true);
                      },
                      player1 -> {
                        GUIManager.getInstance()
                            .openGUI(new ItemSettingsMenu(shop, shopItem), player);
                      });
              GUIManager.getInstance().openGUI(confirmMenu, player);
            });
  }

  private InventoryButton PropertiesButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStack build =
                  ItemStackHelper.builder(Material.FLOWER_BANNER_PATTERN)
                      .name("&3&lProperties")
                      .addLore("&7Adjust the different")
                      .addLore("&7properties of this item.")
                      .addLore()
                      .addLore("&7( click to manage )")
                      .build();
              build.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
              return build;
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              GUIManager.getInstance()
                  .openGUI(new ItemPropertiesSettingsMenu(shop, shopItem), player);
            });
  }

  private InventoryButton PurchasingButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.GOLD_INGOT)
                    .name("&3&lPurchasing")
                    .addLore("&7Setup the item/perm/command")
                    .addLore("&7purchasing features of this item.")
                    .addLore()
                    .addLore("&7( click to manage )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              GUIManager.getInstance()
                  .openGUI(new ItemPurchasingSettingsMenu(shop, shopItem), player);
            });
  }

  private InventoryButton DisplayButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.OAK_SIGN)
                    .name("&3&lDisplay")
                    .addLore("&7Change the different")
                    .addLore("&7display features of this item.")
                    .addLore()
                    .addLore("&7( click to manage )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              GUIManager.getInstance().openGUI(new ItemDisplaySettingsMenu(shop, shopItem), player);
            });
  }

  private InventoryButton ArchiveButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.CHEST)
                    .name("&3&lArchive")
                    .addLore("&7Add this item to an \"archive\"")
                    .addLore("&7which will hide it from the shop")
                    .addLore("&7and put it into a seperate menu.")
                    .addLore()
                    .addLore("&7This is to allow you to keep")
                    .addLore("&7relevant items in the editor.")
                    .addLore()
                    .addLore("&3Status: " + (shopItem.isArchived() ? "&fArchived" : "&fUnarchived"))
                    .addLore()
                    .addLore("&7( click to toggle archive )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ShopItem newShopItem =
                  shopItem.toBuilder()
                      .setArchived(!shopItem.isArchived())
                      .setVisible(false)
                      .build();
              ShopItemsManager.getInstance().updateShopItem(newShopItem);
              GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, newShopItem), player);
            });
  }

  private InventoryButton VisiblityButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ENDER_EYE)
                    .name("&3&lVisiblity")
                    .addLore("&7Toggle the visiblity of")
                    .addLore("&7this item to non-admins.")
                    .addLore()
                    .addLore("&cYou &lMUST&c have at least &c&l1")
                    .addLore("&cpurchasing field set to enable.")
                    .addLore()
                    .addLore("&3Status: " + (shopItem.isVisible() ? "&aVisible" : "&cHidden"))
                    .addLore()
                    .addLore("&7( left-click to toggle )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (!shopItem.isVisible()) {
                if (shopItem.cannotBeDisplayed()) {
                  player.sendMessage(TextUtil.format("&cThis item cannot be displayed."));
                  Main.playDenySound(player);
                  return;
                }
              }
              ShopItem newShopItem = shopItem.toBuilder().setVisible(!shopItem.isVisible()).build();
              ShopItemsManager.getInstance().updateShopItem(newShopItem);
              GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, newShopItem), player);
            });
  }

  private InventoryButton PriceButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.GOLD_NUGGET)
                    .name("&3&lPrice")
                    .addLore("&7Change the price of this item.")
                    .addLore()
                    .addLore(
                        "&3Price: &f$"
                            + (shopItem.getRawPrice() < 0
                                ? shopItem.getRawPrice() + " : " + shopItem.getPrice()
                                : shopItem.getRawPrice()))
                    .addLore(
                        "&3Discount: &f"
                            + (shopItem.isAllowDiscountStacking()
                                ? ShopItemsManager.getInstance().getDiscountPercent(shopItem)
                                    + "% : "
                                    + shopItem.getDiscount()
                                    + "%"
                                : shopItem.getDiscount() + "%"))
                    .addLore(
                        "&3Discount Stacking: "
                            + (shopItem.isAllowDiscountStacking() ? "&aALLOW" : "&cDISALLOW"))
                    .addLore()
                    .addLore("&7( left-click to change price )")
                    .addLore("&7( right-click to change discount )")
                    .addLore("&7( middle-click to toggle stacking )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case MIDDLE -> {
                  ShopItem newShopItem =
                      shopItem.toBuilder()
                          .setAllowDiscountStacking(!shopItem.isAllowDiscountStacking())
                          .build();
                  ShopItemsManager.getInstance().updateShopItem(newShopItem);
                  GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, newShopItem), player);
                }
                case LEFT -> {
                  new AnvilGUI.Builder()
                      .plugin(Main.getInstance())
                      .title("")
                      .itemLeft(
                          ItemStackHelper.builder(Material.GOLD_NUGGET)
                              .name(shopItem.getRawPrice() + "")
                              .build())
                      .onClick(
                          (slot, stateSnapshot) -> {
                            if (slot == 2) {
                              String textEntry = stateSnapshot.getText();
                              textEntry = textEntry.trim();
                              Pattern pattern = Pattern.compile("^-?\\d+$");
                              Matcher matcher = pattern.matcher(textEntry);
                              if (!matcher.matches()) {
                                Main.playDenySound(player);
                                player.sendMessage(TextUtil.format("&cInvalid price!"));
                                GUIManager.getInstance()
                                    .openGUI(new ItemSettingsMenu(shop, shopItem), player);
                                return Collections.emptyList();
                              }
                              int price;
                              try {
                                price = Integer.parseInt(textEntry);
                              } catch (NumberFormatException e) {
                                Main.warn(e.getMessage());
                                return Collections.emptyList();
                              }
                              ShopItem newShopItem = shopItem.toBuilder().setPrice(price).build();
                              ShopItemsManager.getInstance().updateShopItem(newShopItem);
                              GUIManager.getInstance()
                                  .openGUI(new ItemSettingsMenu(shop, newShopItem), player);
                            }
                            return Collections.emptyList();
                          })
                      .open(player);
                }
                case RIGHT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Discount")
                        .itemLeft(
                            ItemStackHelper.builder(Material.BONE_MEAL)
                                .name(shopItem.getDiscount() + "")
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
                                      .openGUI(new ItemSettingsMenu(shop, shopItem), player);
                                  return Collections.emptyList();
                                }
                                int discount;
                                try {
                                  discount = Integer.parseInt(textEntry);
                                } catch (NumberFormatException e) {
                                  Main.warn(e.getMessage());
                                  return Collections.emptyList();
                                }
                                ShopItem newShopItem =
                                    shopItem.toBuilder().setDiscount(discount).build();
                                ShopItemsManager.getInstance().updateShopItem(newShopItem);
                                GUIManager.getInstance()
                                    .openGUI(new ItemSettingsMenu(shop, newShopItem), player);
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
                    .addLore("&7Toggle the lock status and")
                    .addLore("&7add a custom reason.")
                    .addLore()
                    .addLore("&3Status: " + (shopItem.isLocked() ? "&cLocked" : "&2Unlocked"))
                    .addLore(
                        "&3Reason: &f"
                            + (shopItem.getLockedReason() == null
                                    || shopItem.getLockedReason().isBlank()
                                ? "( NONE )"
                                : shopItem.getLockedReason()))
                    .addLore()
                    .addLore("&7( left-click to toggle lock )")
                    .addLore("&7( right-click to change reason )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              switch (event.getClick()) {
                case LEFT -> {
                  ShopItem newShopItem =
                      shopItem.toBuilder().setLocked(!shopItem.isLocked()).build();
                  ShopItemsManager.getInstance().updateShopItem(newShopItem);
                  GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, newShopItem), player);
                }
                case RIGHT ->
                    new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title("Lock Reason")
                        .itemLeft(
                            ItemStackHelper.builder(Material.TRIPWIRE_HOOK)
                                .name(
                                    (shopItem.getLockedReason() == null
                                        ? " "
                                        : shopItem.getLockedReason()))
                                .build())
                        .onClick(
                            (slot, stateSnapshot) -> {
                              if (slot == 2) {
                                String textEntry = stateSnapshot.getText();
                                textEntry = textEntry.trim();
                                ShopItem newShopItem =
                                    shopItem.toBuilder().setLockedReason(textEntry).build();
                                ShopItemsManager.getInstance().updateShopItem(newShopItem);
                                GUIManager.getInstance()
                                    .openGUI(new ItemSettingsMenu(shop, newShopItem), player);
                              }
                              return Collections.emptyList();
                            })
                        .open(player);
              }
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
              ShopManager.getInstance().openShop(shop, player, true);
            });
  }
}
