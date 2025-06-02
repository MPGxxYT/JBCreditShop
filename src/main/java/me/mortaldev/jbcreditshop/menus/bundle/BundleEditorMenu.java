package me.mortaldev.jbcreditshop.menus.bundle;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.listeners.ChatListener;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.modules.bundles.Bundle;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class BundleEditorMenu extends InventoryGUI {
  private final Bundle bundle;

  public BundleEditorMenu(Bundle bundle) {
    this.bundle = bundle;
    allowBottomInventoryClick(true);
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 6 * 9, TextUtil.format("Edit Bundle: " + bundle.getID()));
  }

  @Override
  public void decorate(Player player) {
    ItemStack whiteGlass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 45; i < 54; i++) {
      getInventory().setItem(i, whiteGlass);
    }
    int slot = 0;
    for (ItemStack item : bundle.getItems()) {
      addButton(slot, BundleItemButton(item));
      slot++;
    }
    addButton(45, BackButton());
    addButton(49, AddButton());
    addButton(51, DescriptionButton());
    addButton(52, NameButton());
    super.decorate(player);
  }

  private InventoryButton DescriptionButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.PAPER)
                    .name("&3Edit Description")
                    .addLore("&7Click to change the description.")
                    .addLore("")
                    .addLore("&3Current:")
                    .addLore("&7" + (bundle.getDescription().isBlank() ? "&f( NONE )" : bundle.getDescription()))
                    .addLore("")
                    .addLore("&7( click to change )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Edit Description")
                  .itemLeft(
                      ItemStackHelper.builder(Material.PAPER)
                          .name(bundle.getDescription().isEmpty() ? " " : bundle.getDescription())
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText();
                          textEntry = textEntry.trim();
                          bundle.setDescription(textEntry);
                          BundleManager.getInstance().update(bundle, true);
                          GUIManager.getInstance().openGUI(new BundleEditorMenu(bundle), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton NameButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.NAME_TAG)
                    .name("&3Edit Name")
                    .addLore("&7Rename this bundle to anything!")
                    .addLore("")
                    .addLore("&3Current:")
                    .addLore(bundle.getName())
                    .addLore()
                    .addLore("&7( click to change )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.closeInventory();
              Component message = TextUtil.format("&eEnter the new name of this bundle!## &7[OLD VALUE]##ttp:&7Click to Use##sgt:" + bundle.getName());
              player.sendMessage(message);
              ChatListener.makeRequest(
                  player,
                  400L,
                  component -> {
                    String text = TextUtil.chatComponentToString(component).trim();
                    if (text.isBlank()) {
                      player.sendMessage(TextUtil.format("&cName cannot be empty!"));
                      Main.playDenySound(player);
                      return;
                    }
                    bundle.setName(text);
                    BundleManager.getInstance().update(bundle, true);
                    GUIManager.getInstance().openGUI(new BundleEditorMenu(bundle), player);
                  });
            });
  }

  private InventoryButton AddButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.BUCKET)
                    .name("&2&lAdd Item")
                    .addLore("&7Click with item to add to bundle.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ItemStack cursor = event.getCursor();
              if (cursor == null) {
                return;
              }
              if (cursor.getType().isAir()) {
                return;
              }
              bundle.addItem(cursor.clone());
              BundleManager.getInstance().update(bundle, true);
              GUIManager.getInstance().openGUI(new BundleEditorMenu(bundle), player);
            });
  }

  private InventoryButton BundleItemButton(ItemStack item) {
    return new InventoryButton()
        .creator(player -> BundleManager.getInstance().getDisplayOfBundleItem(item.clone()))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.isLeftClick()) {
                player.getInventory().addItem(item);
              } else if (event.isRightClick()) {
                bundle.removeItem(item);
                BundleManager.getInstance().update(bundle, true);
                GUIManager.getInstance().openGUI(new BundleEditorMenu(bundle), player);
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
              GUIManager.getInstance().openGUI(new BundleMenu(new MenuData()), player);
            });
  }
}
