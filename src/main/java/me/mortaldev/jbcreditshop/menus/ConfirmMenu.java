package me.mortaldev.jbcreditshop.menus;

import java.util.function.Consumer;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ConfirmMenu extends InventoryGUI {

  private final Consumer<Player> confirmConsumer;
  private final Consumer<Player> cancelConsumer;
  private final ItemStack centerItem;
  private final String prompt;

  public ConfirmMenu(
      String prompt,
      ItemStack centerItem,
      Consumer<Player> confirmConsumer,
      Consumer<Player> cancelConsumer) {
    this.confirmConsumer = confirmConsumer;
    this.cancelConsumer = cancelConsumer;
    this.centerItem = centerItem;
    this.prompt = prompt;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&8" + prompt));
  }

  @Override
  public void decorate(Player player) {
    for (int i = 9; i <= 11; i++) {
      addButton(i, CancelButton());
    }
    for (int i = 15; i <= 17; i++) {
      addButton(i, ConfirmButton());
    }
    getInventory().setItem(13, centerItem);
    super.decorate(player);
  }

  private InventoryButton ConfirmButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.GREEN_STAINED_GLASS_PANE)
                    .name("&2&lConfirm")
                    .addLore("&7Click to confirm.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              confirmConsumer.accept(player);
            });
  }

  private InventoryButton CancelButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_STAINED_GLASS_PANE)
                    .name("&c&lCancel")
                    .addLore("&7Click to cancel.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              cancelConsumer.accept(player);
            });
  }
}
