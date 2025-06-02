package me.mortaldev.jbcreditshop.menus.shop;

import java.util.*;
import java.util.function.Consumer;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.menus.BasicMenu;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopsMenu extends BasicMenu<Shop> {

  public ShopsMenu(MenuData menuData) {
    super(menuData, ShopManager.getInstance().getShops());
  }

  @Override
  public BasicMenu<Shop> getNewInstance(MenuData menuData, Set<Shop> dataSet) {
    return new ShopsMenu(menuData);
  }

  @Override
  public String getInventoryName() {
    return "Shops";
  }

  @Override
  public Consumer<InventoryClickEvent> backButton() {
    return null;
  }

  @Override
  public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
    return () -> {
      Shop shop =
          Shop.builder()
              .setShopID(textEntry)
              .setShopDisplay("&7" + textEntry)
              .setDefaultDisplayMaterial(Material.GOLD_INGOT)
              .setLocked(true)
              .build();
      ShopManager shopManager = ShopManager.getInstance();
      Player player = (Player) event.getWhoClicked();
      if (!shopManager.addShop(shop)) {
        player.sendMessage(TextUtil.format("&cShop with ID " + textEntry + " already exists."));
        Main.playDenySound(player);
        GUIManager.getInstance().openGUI(new ShopsMenu(menuData), player);
      }
      shopManager.openShop(shop, player, true);
    };
  }

  @Override
  public ItemStack getDataButtonDisplayStack(Shop shop, Player player) {
    return ShopManager.getInstance().getShopMenuStack(shop);
  }

  @Override
  public Consumer<InventoryClickEvent> dataButtonClickConsumer(Shop shop) {
    return event -> {
      Player player = (Player) event.getWhoClicked();
      if (event.getClick() == ClickType.RIGHT) {
        ShopManager.getInstance().openShop(shop, player, true);
        return;
      } else if (event.getClick() == ClickType.MIDDLE) {
        GUIManager.getInstance().openGUI(new ShopSettingsMenu(shop, true), player);
        return;
      }
      ShopManager.getInstance().openShop(shop, player, false);
    };
  }
}
