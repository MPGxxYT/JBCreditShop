package me.mortaldev.jbcreditshop.menus;

import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EditShopMenu extends InventoryGUI {

  private final MenuData menuData;

  public EditShopMenu(MenuData menuData) {
    this.menuData = menuData;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("Edit Shop"));
  }

  @Override
  public void decorate(Player player) {
    addButton(0, BackButton());
    super.decorate(player);
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
              GUIManager.getInstance().openGUI(new ShopsMenu(menuData), player);
            });
  }
}
