package me.mortaldev.jbcreditshop.menus.bundle;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.menus.BasicMenu;
import me.mortaldev.jbcreditshop.menus.ConfirmMenu;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.modules.bundles.Bundle;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.function.Consumer;

public class BundleMenu extends BasicMenu<Bundle> {

  public BundleMenu(MenuData menuData) {
    super(menuData, BundleManager.getInstance().getSet());
  }

  @Override
  public BasicMenu<Bundle> getNewInstance(MenuData menuData, Set<Bundle> dataSet) {
    return new BundleMenu(menuData);
  }

  @Override
  public String getInventoryName() {
    return "Bundles";
  }

  @Override
  public Consumer<InventoryClickEvent> backButton() {
    return null;
  }

  @Override
  public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
    return () -> {
      Bundle bundle = Bundle.create(textEntry);
      Player player = (Player) event.getWhoClicked();
      if (BundleManager.getInstance().contains(bundle)) {
        player.sendMessage(TextUtil.format("&cBundle by ID already exists!"));
        Main.playDenySound(player);
        return;
      }
      BundleManager.getInstance().add(bundle, true);
      GUIManager.getInstance().openGUI(new BundleEditorMenu(bundle), player);
    };
  }

  @Override
  public ItemStack getDataButtonDisplayStack(Bundle data, Player player) {
    return BundleManager.getInstance().getBundleMenuDisplay(data);
  }

  @Override
  public Consumer<InventoryClickEvent> dataButtonClickConsumer(Bundle data) {
    ItemStack bundleMenuDisplay = BundleManager.getInstance().getBundleMenuDisplay(data);
    ConfirmMenu confirmDelete =
        new ConfirmMenu(
            "Delete this bundle?",
            bundleMenuDisplay,
            player -> {
              BundleManager.getInstance().remove(data, true);
              GUIManager.getInstance().openGUI(new BundleMenu(new MenuData()), player);
            },
            player -> GUIManager.getInstance().openGUI(new BundleMenu(new MenuData()), player));

    return event -> {
      Player player = (Player) event.getWhoClicked();
      if (event.isRightClick()) {
        GUIManager.getInstance().openGUI(confirmDelete, player);
        return;
      } else if (event.getClick() == ClickType.MIDDLE) {
        player.getInventory().addItem(BundleManager.getInstance().getBundleItemStack(data));
        return;
      }
      GUIManager.getInstance().openGUI(new BundleEditorMenu(data), player);
    };
  }
}
