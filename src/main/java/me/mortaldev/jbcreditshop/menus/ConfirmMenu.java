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

  private ConfirmMenu(Builder builder) {
    this.confirmConsumer = builder.confirmConsumer;
    this.cancelConsumer = builder.cancelConsumer;
    this.centerItem = builder.centerItem;
    this.prompt = builder.prompt;
  }

  @Deprecated
  public ConfirmMenu(
      String prompt,
      ItemStack centerItem,
      Consumer<Player> confirmConsumer,
      Consumer<Player> cancelConsumer) {
    this.prompt = prompt;
    this.centerItem = centerItem;
    this.confirmConsumer = confirmConsumer;
    this.cancelConsumer = cancelConsumer;
  }

  public static class Builder {
    private Consumer<Player> confirmConsumer;
    private Consumer<Player> cancelConsumer;
    private ItemStack centerItem;
    private String prompt;

    public Builder confirm(Consumer<Player> confirmConsumer) {
      this.confirmConsumer = confirmConsumer;
      return this;
    }

    public Builder cancel(Consumer<Player> cancelConsumer) {
      this.cancelConsumer = cancelConsumer;
      return this;
    }

    public Builder centerItem(ItemStack centerItem) {
      this.centerItem = centerItem;
      return this;
    }

    public Builder prompt(String prompt) {
      this.prompt = prompt;
      return this;
    }

    public ConfirmMenu build() {
      return new ConfirmMenu(this);
    }
  }

  public static Builder builder() {
    return new Builder();
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