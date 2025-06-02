package me.mortaldev.jbcreditshop.listeners;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.utils.NBTAPI;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BundleListener implements Listener {

  @EventHandler
  public void onRightClick(PlayerInteractEvent event) {
    EquipmentSlot hand = event.getHand();
    if (hand == null) {
      return;
    }
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItem(hand);
    if (item.getType().isAir()) {
      return;
    }
    if (!NBTAPI.hasNBT(item, "bundleID")) {
      return;
    }
    String bundleID = NBTAPI.getNBT(item, "bundleID");
    if (bundleID == null) {
      return;
    }
    event.setCancelled(true);
    BundleManager.getInstance()
        .getByID(bundleID)
        .ifPresentOrElse(
            (bundle) -> {
              boolean b = BundleManager.getInstance().giveBundleItems(player, bundle);
              if (!b) {
                return;
              }
              player.sendMessage(
                  TextUtil.format(
                      "&3You have collected the items of the &7"
                          + bundle.getName()
                          + "&3 bundle!"));
              if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(hand, item);
              } else {
                player.getInventory().setItem(hand, new ItemStack(Material.AIR));
              }
            },
            () -> {
              player.sendMessage(TextUtil.format("&cError has occurred!"));
              Main.warn("Bundle with ID " + bundleID + " not found!");
            });
  }
}
