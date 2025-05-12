package me.mortaldev.jbcreditshop.menus;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class ShopsMenu extends InventoryGUI {

  private final Set<Shop> shops;
  private final MenuData menuData;

  public ShopsMenu(MenuData menuData) {
    this.menuData = menuData;
    this.shops = ShopManager.getInstance().getShops();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format("Shops"));
  }

  private int getMaxPage() {
    return (int) Math.ceil(shops.size() / 45.0);
  }

  private int getSize() { // Paginated
    int maxPage = getMaxPage();
    if (menuData.getPage() >= maxPage) {
      int mod = (maxPage - 1) * 45;
      int size = shops.size();
      if (mod > 0) {
        size = size % mod;
      }
      return Utils.clamp((int) Math.ceil(size / 9.0), 2, 6);
    }
    return 6;
  }

  private Set<Shop> applySearch(Set<Shop> shops) {
    String search = menuData.getSearchQuery();
    if (search == null || search.isEmpty()) {
      return shops;
    }
    Set<Shop> filtered = new HashSet<>();
    for (Shop shop : shops) {
      if (shop.getShopID().toLowerCase().contains(search.toLowerCase())) {
        filtered.add(shop);
      }
    }
    return filtered;
  }

  private Set<Shop> applyPage(Set<Shop> shops) {
    int page = menuData.getPage();
    if (page > getMaxPage()) {
      page = getMaxPage();
    }
    return shops.stream()
        .skip((page - 1) * 45L)
        .limit(45)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public void decorate(Player player) {
    ItemStack whiteGlass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, whiteGlass);
    }
    if (getMaxPage() > 1) {
      addButton(8, NextButton());
    }
    if (menuData.getPage() > 1) {
      addButton(0, BackButton());
    }
    Set<Shop> filtered = applySearch(shops);
    Set<Shop> pageAdjusted = applyPage(filtered);
    int slot = 0;
    for (Shop shop : pageAdjusted) {
      addButton(slot + 9, ShopButton(shop));
      if (slot == (getSize() - 1) * 9) {
        break;
      }
      slot++;
    }
    super.decorate(player);
  }

  private InventoryButton ShopButton(Shop shop) {
    return new InventoryButton()
        .creator(player -> ShopManager.getInstance().getShopMenuStack(shop))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ShopManager.getInstance().openShop(shop, player, false);
            });
  }

  private InventoryButton BackButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&7&lBack")
                    .addLore("&7Click to return to previous page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page > 1) {
                menuData.setPage(page - 1);
              }
              GUIManager.getInstance().openGUI(new ShopsMenu(menuData), player);
            });
  }

  private InventoryButton NextButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&7&lBack")
                    .addLore("&7Click to go to next page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page < getMaxPage()) {
                menuData.setPage(page + 1);
              }
              GUIManager.getInstance().openGUI(new ShopsMenu(menuData), player);
            });
  }
}
