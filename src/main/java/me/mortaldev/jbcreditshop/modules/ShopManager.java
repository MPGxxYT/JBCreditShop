package me.mortaldev.jbcreditshop.modules;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import me.mortaldev.jbcreditshop.menus.styled.AutoStyleMenu;
import me.mortaldev.jbcreditshop.menus.styled.CustomStyleMenu;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.yaml.ShopsYaml;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopManager {

  private Set<Shop> shops = new HashSet<>();

  private static class Singleton {
    private static final ShopManager INSTANCE = new ShopManager();
  }

  public static synchronized ShopManager getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopManager() {}

  public void loadShops() {
    shops.clear();
    shops = ShopsYaml.getInstance().read();
  }

  public Set<Shop> getShops() {
    return shops;
  }

  public Optional<Shop> getShop(String shopID) {
    for (Shop shop : shops) {
      if (shop.getShopID().equals(shopID)) {
        return Optional.of(shop);
      }
    }
    return Optional.empty();
  }

  public void deleteShop(Shop shop) {
    shops.removeIf(entry -> entry.getShopID().equals(shop.getShopID()));
    ShopsYaml.getInstance().delete(shop);
  }

  public boolean addShop(Shop shop) {
    for (Shop entry : shops) {
      if (entry.getShopID().equals(shop.getShopID())) {
        return false;
      }
    }
    ShopsYaml.getInstance().create(shop);
    shops.add(shop);
    return true;
  }

  public void updateShop(Shop shop) {
    shops.removeIf(entry -> entry.getShopID().equals(shop.getShopID()));
    ShopsYaml.getInstance().save(shop);
    shops.add(shop);
  }

  public void openShop(Shop shop, Player player, boolean adminMode) {
    switch (shop.getStyle()) {
      case CUSTOM -> GUIManager.getInstance().openGUI(new CustomStyleMenu(shop, adminMode), player);
      case AUTO -> GUIManager.getInstance()
          .openGUI(new AutoStyleMenu(shop, adminMode, new MenuData()), player);
    }
  }

  public ItemStack getShopMenuStack(Shop shop) {
    return getShopMenuStack(shop, true);
  }

  public ItemStack getShopMenuStack(Shop shop, boolean clickPrompts) {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(shop.getDefaultDisplayMaterial()).name(shop.getShopDisplay());
    builder
        .addLore("&3&lID: &f" + shop.getShopID())
        .addLore("&3&lDefault Price: &f" + shop.getDefaultPrice())
        .addLore("&3&lLocked: &f" + shop.isLocked())
        .addLore("&3&lDiscount: &f" + shop.getDiscount() + "%");
    if (shop.getDiscount() > 0) {
      if (!shop.getDiscountGroup().isBlank()) {
        builder.addLore("&3&lDiscount Group: &f" + shop.getDiscountGroup());
      }
    }
    builder.addLore("&3&lStyle: &f" + shop.getStyle().name());
    if (shop.getStyle() == Shop.Style.CUSTOM) {
      builder.addLore("&3&lSize: &f" + shop.getSize());
    }
    if (clickPrompts) {
      builder
          .addLore("")
          .addLore("&7( left-click to view shop )")
          .addLore("&7( middle-click to edit settings )")
          .addLore("&7( right-click to edit )");
    }

    return builder.build();
  }
}
