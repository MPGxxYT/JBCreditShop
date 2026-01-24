package me.mortaldev.jbcreditshop.menus.bundle;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.menus.ConfirmMenu;
import me.mortaldev.jbcreditshop.menus.basicmenu.BasicMenu;
import me.mortaldev.jbcreditshop.menus.basicmenu.MenuConfig;
import me.mortaldev.jbcreditshop.menus.basicmenu.MenuData;
import me.mortaldev.jbcreditshop.modules.bundles.Bundle;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BundleMenu extends BasicMenu<Bundle> {

  private final BundleManager bundleManager;

  public BundleMenu(MenuData menuData) {
    super(
        menuData,
        new HashSet<>(Main.getInstance().getPluginServices().bundleManager().getSet()),
        MenuConfig.defaultConfig().toBuilder().enableBack(false).build());
    this.bundleManager = Main.getInstance().getPluginServices().bundleManager();
  }

  @Override
  public Predicate<Bundle> getSearchPredicate(String searchQuery) {
    return (bundle -> bundle.getName().toLowerCase().contains(searchQuery));
  }

  @Override
  public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
    return () -> {
      Bundle bundle = Bundle.create(textEntry);
      Player player = (Player) event.getWhoClicked();
      if (bundleManager.contains(bundle)) {
        player.sendMessage(TextUtil.format("&cBundle by ID already exists!"));
        Main.playDenySound(player);
        return;
      }
      bundleManager.add(bundle, true);
      GUIManager.getInstance().openGUI(new BundleEditorMenu(bundle), player);
    };
  }

  @Override
  public BasicMenu<Bundle> getNewMenuInstance(MenuData menuData, Set<Bundle> dataSet) {
    return new BundleMenu(menuData);
  }

  @Override
  public String getInventoryName() {
    return "Bundles";
  }

  @Override
  public ItemStack getDataButtonDisplayStack(Bundle data, Player player) {
    return bundleManager.getBundleMenuDisplay(data);
  }

  @Override
  public Consumer<InventoryClickEvent> dataButtonClickConsumer(Bundle data, MenuData menuData) {
    ItemStack bundleMenuDisplayItem = bundleManager.getBundleMenuDisplay(data);
    ConfirmMenu confirmDelete =
        ConfirmMenu.builder()
            .prompt("Delete this bundle?")
            .centerItem(bundleMenuDisplayItem)
            .confirm(
                player -> {
                  bundleManager.remove(data, true);
                  GUIManager.getInstance().openGUI(new BundleMenu(new MenuData()), player);
                })
            .cancel(
                player -> GUIManager.getInstance().openGUI(new BundleMenu(new MenuData()), player))
            .build();
    return event -> {
      Player player = (Player) event.getWhoClicked();
      if (event.isRightClick()) {
        GUIManager.getInstance().openGUI(confirmDelete, player);
        return;
      } else if (event.getClick() == ClickType.MIDDLE) {
        player.getInventory().addItem(bundleManager.getBundleItemStack(data));
        return;
      }
      GUIManager.getInstance().openGUI(new BundleEditorMenu(data), player);
    };
  }
}
