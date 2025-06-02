package me.mortaldev.jbcreditshop.menus.styled;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.ecobits.EcoBitsAccount;
import me.mortaldev.jbcreditshop.menus.ConfirmMenu;
import me.mortaldev.jbcreditshop.menus.shop.ShopSettingsMenu;
import me.mortaldev.jbcreditshop.menus.shop.ShopsMenu;
import me.mortaldev.jbcreditshop.menus.item.ItemSettingsMenu;
import me.mortaldev.jbcreditshop.modules.*;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerData;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AutoStyleMenu extends InventoryGUI {

  private final Shop shop;
  private Set<ShopItem> shopItems;
  private final boolean adminMode;
  private final MenuData menuData;
  private static final String CREDITS_LINK = "https://store.jailbreakmc.games/category/credits";

  public AutoStyleMenu(Shop shop, boolean adminMode, MenuData menuData) {
    this.adminMode = adminMode;
    this.shop = shop;
    this.menuData = menuData;
    this.shopItems = ShopItemsManager.getInstance().getItemsByShopID(shop.getShopID(), false);
  }

  @Override
  protected Inventory createInventory() {
    shopItems = applyFilterAndSearch(shopItems, getRegisteredPlayer());
    shopItems = applyVisible(shopItems);
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(shop.getShopDisplay()));
  }

  private int getMaxPage() {
    if (shopItems.isEmpty()) {
      return 1;
    }
    return (int) Math.ceil(shopItems.size() / 45.0);
  }

  private int getSize() { // Paginated
    int maxPage = getMaxPage();
    if (menuData.getPage() >= maxPage) {
      int mod = (maxPage - 1) * 45;
      int size = shopItems.size();
      if (mod > 0) {
        size = size % mod;
      }
      return Utils.clamp((int) Math.ceil(size / 9.0) + 1, 2, 6);
    }
    return 6;
  }

  private Set<ShopItem> applyVisible(Set<ShopItem> shopItems) {
    if (adminMode) {
      return shopItems;
    }
    Set<ShopItem> visibleItems = new HashSet<>();
    for (ShopItem item : shopItems) {
      if (!item.cannotBeDisplayed() && item.isVisible()) {
        visibleItems.add(item);
      }
    }
    return visibleItems;
  }

  private Set<ShopItem> applyFilterAndSearch(Set<ShopItem> shopItems, Player player) {
    if (shopItems.isEmpty()) {
      return new LinkedHashSet<>();
    }
    Set<ShopItem> result;
    if (!menuData.getSearchQuery().isBlank()) {
      result = new HashSet<>();
      for (ShopItem item : shopItems) {
        if (item.getItemID().toLowerCase().contains(menuData.getSearchQuery().toLowerCase())) {
          result.add(item);
        } else if (!item.getDisplayName().isBlank()) {
          String displayName = item.getDisplayName();
          displayName = TextUtil.removeColors(displayName);
          displayName = TextUtil.removeDecoration(displayName);
          if (displayName.toLowerCase().contains(menuData.getSearchQuery().toLowerCase())) {
            result.add(item);
          }
        }
      }
    } else {
      result = new HashSet<>(shopItems);
    }
    Set<ShopItem> remove = new HashSet<>();
    switch (menuData.getFilter()) {
      case NONE -> {
        return result;
      }
      case OWNED -> { // Show only owned items
        for (ShopItem item : result) {
          if (!item.getPurchasedPermission().isBlank()) {
            if (!player.hasPermission(item.getPurchasedPermission())) {
              remove.add(item);
              continue;
            }
          }
          if (!item.getPermission().isBlank()) {
            if (!player.hasPermission(item.getPermission())) {
              remove.add(item);
              continue;
            }
          }
          PlayerData playerData =
              PlayerDataManager.getInstance()
                  .getByID(player.getUniqueId().toString())
                  .orElse(PlayerData.create(player.getUniqueId().toString()));
          if (!playerData.hasPurchasedItem(item)) {
            remove.add(item);
          }
        }
      }
      case UNLOCKED -> {
        for (ShopItem item : result) {
          if (item.isLocked()) {
            remove.add(item);
          }
        }
      }
      case UNOWNED -> { // Show only unowned items
        for (ShopItem item : result) {
          if (!item.getPurchasedPermission().isBlank()) {
            if (player.hasPermission(item.getPurchasedPermission())) {
              remove.add(item);
              continue;
            }
          }
          if (!item.getPermission().isBlank()) {
            if (player.hasPermission(item.getPermission())) {
              remove.add(item);
              continue;
            }
          }
          PlayerData playerData =
              PlayerDataManager.getInstance()
                  .getByID(player.getUniqueId().toString())
                  .orElse(PlayerData.create(player.getUniqueId().toString()));
          if (playerData.hasPurchasedItem(item)) {
            remove.add(item);
          }
        }
      }
    }
    result.removeAll(remove);
    remove.clear();
    return result;
  }

  private LinkedHashSet<ShopItem> applyOrderAndDirection(Set<ShopItem> shopItems) {
    if (shopItems.isEmpty()) {
      return new LinkedHashSet<>();
    }
    LinkedHashSet<ShopItem> result = new LinkedHashSet<>(shopItems);
    switch (menuData.getOrderBy()) {
      case NAME ->
          result =
              result.stream()
                  .sorted(Comparator.comparing(ShopItem::getPlainDisplayName))
                  .collect(Collectors.toCollection(LinkedHashSet::new));
      case GROUP -> {
        LinkedHashSet<ShopItem> hasGroup = new LinkedHashSet<>();
        LinkedHashSet<ShopItem> doesntHaveGroup = new LinkedHashSet<>();
        result.forEach(
            item -> {
              if (!item.getGroup().isBlank()) {
                hasGroup.add(item);
              } else {
                doesntHaveGroup.add(item);
              }
            });
        result =
            hasGroup.stream()
                .sorted(Comparator.comparing(ShopItem::getGroup))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        result.addAll(doesntHaveGroup);
      }
      case PRICE ->
          result =
              result.stream()
                  .sorted(
                      Comparator.comparingDouble(ShopItemsManager.getInstance()::getDiscountPrice))
                  .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    switch (menuData.getDirection()) {
      case ASCENDING -> {
        return result;
      }
      case DESCENDING -> {
        List<ShopItem> reversedList = new ArrayList<>(result);
        Collections.reverse(reversedList);
        result = new LinkedHashSet<>(reversedList);
        return result;
      }
    }
    return result;
  }

  private LinkedHashSet<ShopItem> applyPage(LinkedHashSet<ShopItem> shopItems) {
    int page = menuData.getPage();
    if (page > getMaxPage()) {
      page = getMaxPage();
    }
    return shopItems.stream()
        .skip((page - 1) * 45L)
        .limit(45)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public void decorate(Player player) {
    LinkedHashSet<ShopItem> orderAndDirection = applyOrderAndDirection(shopItems);
    LinkedHashSet<ShopItem> pageAdjusted = applyPage(orderAndDirection);
    int slot = 0;
    for (ShopItem shopItem : pageAdjusted) {
      if (slot == 45) {
        break;
      }
      addButton(slot + 9, ShopItemButton(shopItem));
      slot++;
    }
    ItemStack glass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, glass);
    }
    if (menuData.getPage() > 1 || adminMode) {
      addButton(0, BackButton());
    }
    if (menuData.getPage() < getMaxPage()) {
      addButton(8, NextButton());
    }
    addButton(3, SearchButton());
    addButton(4, FilterButton());
    addButton(5, OrderButton());
    if (player.hasPermission("jbcreditshop.admin")) {
      addButton(2, ShopSettingsButton());
    }
    if (adminMode) {
      addButton(6, AddItemButton());
    } else {
      addButton(6, BalanceButton());
    }
    super.decorate(player);
  }

  private InventoryButton BalanceButton() {
    return new InventoryButton()
        .creator(
            player -> {
              BigDecimal currentBalance = new EcoBitsAccount(player).getCurrentBalance();
              return ItemStackHelper.builder(Material.SUNFLOWER)
                  .name("&e&lBalance")
                  .addLore(
                      "&f"
                          + currentBalance.setScale(0, java.math.RoundingMode.DOWN).toPlainString()
                          + " Credits")
                  .addLore()
                  .addLore("&e&oClick to purchase more credits.")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.sendMessage(TextUtil.format(""));
              player.sendMessage(
                  TextUtil.format("&e            Visit our store to purchase credits."));
              player.sendMessage(
                  TextUtil.format(
                      "&f > "
                          + CREDITS_LINK
                          + "##url:"
                          + CREDITS_LINK
                          + "##ttp:&7Click to open!## < "));
              player.sendMessage(TextUtil.format(""));
              player.closeInventory();
              player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.414214f);
            });
  }

  private InventoryButton AddItemButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.LIME_DYE)
                    .name("&2&lAdd Item")
                    .addLore("&7Add an item to this shop.")
                    .addLore()
                    .addLore("&7&o[Item hidden by default]")
                    .addLore()
                    .addLore("&7( click to add )")
                    .build())
        .consumer(
            event -> {
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
                            GUIManager.getInstance()
                                .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
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
                                  .build();
                          boolean b = ShopItemsManager.getInstance().addShopItem(shopItem);
                          if (!b) {
                            player.sendMessage(TextUtil.format("&cItem by ID already exists.!"));
                            Main.playDenySound(player);
                            GUIManager.getInstance()
                                .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
                            return Collections.emptyList();
                          }
                          GUIManager.getInstance()
                              .openGUI(new ItemSettingsMenu(shop, shopItem), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton ShopSettingsButton() {
    ItemStackHelper.Builder builder;
    if (adminMode) {
      builder =
          ItemStackHelper.builder(Material.HEART_OF_THE_SEA)
              .name("&3&lShop Settings")
              .addLore("&7Change the shop settings.")
              .addLore()
              .addLore("&7( left-click to change )")
              .addLore("&7( right-click to disable admin mode )");
    } else {
      builder =
          ItemStackHelper.builder(Material.PRISMARINE_CRYSTALS)
              .name("&3&lAdmin Mode")
              .addLore("&7Enable admin mode to begin editing.")
              .addLore()
              .addLore("&7( click to enable )");
    }
    return new InventoryButton()
        .creator(player -> builder.build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (adminMode) {
                if (event.getClick() == ClickType.LEFT) {
                  GUIManager.getInstance().openGUI(new ShopSettingsMenu(shop, true), player);
                } else {
                  GUIManager.getInstance()
                      .openGUI(new AutoStyleMenu(shop, false, menuData), player);
                }
              } else {
                GUIManager.getInstance().openGUI(new AutoStyleMenu(shop, true, menuData), player);
              }
            });
  }

  private InventoryButton OrderButton() {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.BOOK)
            .name("&3&lOrder")
            .addLore("&7Change the order in which the items are displayed.")
            .addLore("")
            .addLore("&7NAME")
            .addLore("&7GROUP")
            .addLore("&7PRICE")
            .addLore("")
            .addLore("&7ASCENDING")
            .addLore("&7DESCENDING")
            .addLore("")
            .addLore("&7( left-click to change order)")
            .addLore("&7( right-click to change direction)");
    switch (menuData.getDirection()) {
      case ASCENDING -> builder.setLore(6, " &f&lASCENDING");
      case DESCENDING -> builder.setLore(7, " &f&lDESCENDING");
    }
    switch (menuData.getOrderBy()) {
      case NAME -> builder.setLore(2, " &f&lNAME");
      case GROUP -> builder.setLore(3, " &f&lGROUP");
      case PRICE -> builder.setLore(4, " &f&lPRICE");
    }
    return new InventoryButton()
        .creator(player -> builder.build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.isRightClick()) {
                menuData.setDirection(MenuData.Direction.getNext(menuData.getDirection()));
                player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.75f);
              } else {
                menuData.setOrderBy(MenuData.OrderBy.getNext(menuData.getOrderBy()));
                player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.5f);
              }
              GUIManager.getInstance()
                  .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
            });
  }

  private InventoryButton FilterButton() {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.NAME_TAG)
            .name("&3&lFilter")
            .addLore("&7Will filter out the selected item.")
            .addLore("")
            .addLore("&7NONE")
            .addLore("&7UNLOCKED")
            .addLore("&7OWNED")
            .addLore("&7UNOWNED")
            .addLore("")
            .addLore("&7( click to change )");
    switch (menuData.getFilter()) {
      case NONE -> builder.setLore(2, " &f&lNONE");
      case UNLOCKED -> builder.setLore(3, " &f&lUNLOCKED");
      case OWNED -> builder.setLore(4, " &f&lOWNED");
      case UNOWNED -> builder.setLore(5, " &f&lUNOWNED");
    }
    return new InventoryButton()
        .creator(player -> builder.build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.playSound(
                  player.getLocation(), Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, 0.5f, 1);
              MenuData.Filter filter = menuData.getFilter();
              menuData.setFilter(filter.getNext(filter));
              GUIManager.getInstance()
                  .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
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
              int page = menuData.getPage();
              if (page > 1) {
                menuData.setPage(page - 1);
              } else {
                if (adminMode) {
                  GUIManager.getInstance().openGUI(new ShopsMenu(new MenuData()), player);
                  return;
                }
              }
              GUIManager.getInstance()
                  .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
            });
  }

  private InventoryButton NextButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&2&lNext Page")
                    .addLore("&7Click to go to next page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page < getMaxPage()) {
                menuData.setPage(page + 1);
              }
              GUIManager.getInstance()
                  .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
            });
  }

  private InventoryButton SearchButton() {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.ANVIL)
            .name("&3&lSearch")
            .addLore("&7Enter a search query to find something specific.")
            .addLore("");
    if (!menuData.getSearchQuery().isBlank()) {
      builder
          .addLore("&7Query: &f" + menuData.getSearchQuery())
          .addLore("")
          .addLore("&7( click to clear )");
    } else {
      builder.addLore("&7Query: &fNone").addLore("").addLore("&7( click to search )");
    }
    return new InventoryButton()
        .creator(player -> builder.build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.75f);
              if (!menuData.getSearchQuery().isBlank()) {
                menuData.setSearchQuery("");
                GUIManager.getInstance()
                    .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
                return;
              }
              if (menuData.getSearchQuery().isBlank() || event.isLeftClick()) {
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Search")
                    .itemLeft(ItemStackHelper.builder(Material.PAPER).name(" ").build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            String textEntry = stateSnapshot.getText();
                            textEntry = textEntry.trim();
                            menuData.setSearchQuery(textEntry);
                            GUIManager.getInstance()
                                .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
                            player.playSound(
                                player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 1f);
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              } else if (event.isRightClick()) {
                menuData.setSearchQuery("");
                GUIManager.getInstance()
                    .openGUI(new AutoStyleMenu(shop, adminMode, menuData), player);
              }
            });
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
                player.playSound(
                    player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.5f);
                ConfirmMenu confirmMenu =
                    new ConfirmMenu(
                        "&e&lPurchase " + shopItem.getDisplayName() + "?",
                        ShopItemsManager.getInstance()
                            .getShopMenuStack(shopItem, false, player, false),
                        (player1) -> {
                          ShopItemsManager.getInstance().purchaseShopItem(shopItem, player);
                          GUIManager.getInstance()
                              .openGUI(new AutoStyleMenu(shop, false, menuData), player);
                        },
                        (player1) -> {
                          player.playSound(
                              player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.5f);
                          GUIManager.getInstance()
                              .openGUI(new AutoStyleMenu(shop, false, menuData), player);
                        });
                GUIManager.getInstance().openGUI(confirmMenu, player);
              }
            });
  }
}
