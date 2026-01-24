package me.mortaldev.jbcreditshop.menus.styled;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.menus.ConfirmMenu;
import me.mortaldev.jbcreditshop.menus.basicmenu.BasicMenu;
import me.mortaldev.jbcreditshop.menus.basicmenu.MenuData;
import me.mortaldev.jbcreditshop.menus.basicmenu.OrderOption;
import me.mortaldev.jbcreditshop.menus.basicmenu.PredicateFilter;
import me.mortaldev.jbcreditshop.menus.item.ItemSettingsMenu;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerData;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataManager;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AutoStyleMenu extends BasicMenu<ShopItem> {
  private final Shop shop;
  private final boolean adminMode;

  /** Filter options for shop item ownership/availability status. */
  public enum OwnershipFilter implements MenuData.IDisplayName {
    OWNED("Owned"),
    UNOWNED("Unowned"),
    UNLOCKED("Unlocked");

    private final String displayName;

    OwnershipFilter(String displayName) {
      this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }
  }

  public AutoStyleMenu(Shop shop, boolean adminMode, MenuData menuData) {
    super(menuData, ShopItemsManager.getInstance().getItemsByShop(shop));
    this.shop = shop;
    this.adminMode = adminMode;
  }

  @Override
  protected Set<ShopItem> preprocessData(Set<ShopItem> data) {
    if (adminMode) {
      return data;
    }
    Set<ShopItem> visibleItems = new HashSet<>();
    for (ShopItem item : data) {
      if (!item.cannotBeDisplayed() && item.isVisible()) {
        visibleItems.add(item);
      }
    }
    return visibleItems;
  }

  @Override
  public Predicate<ShopItem> getSearchPredicate(String searchQuery) {
    return (shopItem ->
        shopItem.getItemID().toLowerCase().contains(searchQuery.toLowerCase())
            || shopItem.getPlainDisplayName().contains(searchQuery.toLowerCase()));
  }

  @Override
  public List<OrderOption<ShopItem>> getOrderOptions() {
    return List.of(
        new OrderOption<>(
            "name",
            "Name",
            Comparator.comparing(ShopItem::getPlainDisplayName, String.CASE_INSENSITIVE_ORDER)),
        new OrderOption<>("group", "Group", Comparator.comparing(ShopItem::getGroup)),
        new OrderOption<>(
            "price",
            "Price",
            Comparator.comparingDouble(ShopItemsManager.getInstance()::getDiscountPrice)));
  }

  @Override
  public List<PredicateFilter<ShopItem, ?>> getPredicateFilters() {
    return List.of(
        new PredicateFilter<>(
            "ownership",
            "Filter",
            OwnershipFilter.class,
            option ->
                switch (option) {
                  case OWNED -> this::isOwnedByPlayer;
                  case UNOWNED -> (item, player) -> !isOwnedByPlayer(item, player);
                  case UNLOCKED -> (item, player) -> !item.isLocked();
                }));
  }

  /**
   * Checks if a shop item is owned by the player (via permission or purchase history).
   *
   * @param item The shop item to check.
   * @param player The player to check ownership for.
   * @return true if the player owns the item, false otherwise.
   */
  private boolean isOwnedByPlayer(ShopItem item, Player player) {
    // Check purchased permission
    if (!item.getPurchasedPermission().isBlank()) {
      if (player.hasPermission(item.getPurchasedPermission())) {
        return true;
      }
    }
    // Check item permission
    if (!item.getPermission().isBlank()) {
      if (player.hasPermission(item.getPermission())) {
        return true;
      }
    }
    // Check purchase history
    PlayerDataManager playerDataManager =
        Main.getInstance().getPluginServices().playerDataManager();
    PlayerData playerData =
        playerDataManager
            .getByID(player.getUniqueId().toString())
            .orElse(PlayerData.create(player.getUniqueId().toString()));
    return playerData.hasPurchasedItem(item);
  }

  @Override
  public BasicMenu<ShopItem> getNewMenuInstance(MenuData menuData, Set<ShopItem> dataSet) {
    return new AutoStyleMenu(shop, adminMode, menuData);
  }

  @Override
  public String getInventoryName() {
    return shop.getShopDisplay();
  }

  @Override
  public ItemStack getDataButtonDisplayStack(ShopItem data, Player player) {
    return ShopItemsManager.getInstance().getShopMenuStack(data, adminMode, player);
  }

  @Override
  public Consumer<InventoryClickEvent> dataButtonClickConsumer(
      ShopItem shopItem, MenuData menuData) {
    return (event -> {
      Player player = (Player) event.getWhoClicked();
      if (adminMode) {
        if (event.isRightClick()) {
          GUIManager.getInstance().openGUI(new ItemSettingsMenu(shop, shopItem), player);
        }
        return;
      }
      if (ShopItemsManager.getInstance().canAllowPurchase(shopItem, player)) {
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.5f);
        ConfirmMenu confirmMenu =
            ConfirmMenu.builder()
                .prompt("&e&lPurchase " + shopItem.getDisplayName() + "?")
                .centerItem(
                    ShopItemsManager.getInstance().getShopMenuStack(shopItem, false, player, false))
                .confirm(
                    player1 -> {
                      ShopItemsManager.getInstance().purchaseShopItem(shopItem, player1);
                      GUIManager.getInstance()
                          .openGUI(new AutoStyleMenuOld(shop, false, menuData), player1);
                    })
                .cancel(
                    (player1) -> {
                      player.playSound(
                          player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.5f);
                      GUIManager.getInstance()
                          .openGUI(new AutoStyleMenuOld(shop, false, menuData), player);
                    })
                .build();
        GUIManager.getInstance().openGUI(confirmMenu, player);
      }
    });
  }
}
