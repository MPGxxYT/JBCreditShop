package me.mortaldev.jbcreditshop.modules.bundles;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.NBTAPI;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BundleManager extends CRUDManager<Bundle> {
  private static class Singleton {
    private static final BundleManager INSTANCE = new BundleManager();
  }

  public static synchronized BundleManager getInstance() {
    return Singleton.INSTANCE;
  }

  private BundleManager() {}

  @Override
  public CRUD<Bundle> getCRUD() {
    return BundleCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }

  public boolean giveBundleItems(Player player, Bundle bundle) {
    PlayerInventory inventory = player.getInventory();
    for (ItemStack item : bundle.getItems()) {
      if (!Utils.canInventoryHold(inventory, item)) {
        player.sendMessage(TextUtil.format("&cYou do not have enough space for this."));
        return false;
      }
    }
    int airCount = 0;
    for (ItemStack item : inventory.getStorageContents()) {
      if (item == null || item.getType().isAir()) {
        airCount++;
      }
    }
    if (airCount < bundle.getItems().size()) {
      player.sendMessage(TextUtil.format("&cYou do not have enough space for this."));
      return false;
    }
    for (ItemStack item : bundle.getItems()) {
      inventory.addItem(item);
    }
    return true;
  }

  public ItemStack getDisplayOfBundleItem(ItemStack itemStack) {
    return ItemStackHelper.builder(itemStack.clone())
        .addLore()
        .addLore("&7( left-click to get )")
        .addLore("&7( right-click to remove )")
        .build();
  }

  public ItemStack getBundleItemStack(Bundle bundle) {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.BARREL).name("&7" + bundle.getName());
    if (!bundle.getDescription().isBlank()) {
      builder.addLore("&7" + bundle.getDescription()).addLore();
    }
    builder
        .addLore("&3Click Anywhere&7 to collect")
        .addLore("&7the items inside this bundle.")
        .addLore()
        .addLore("&3Contents:");
    for (ItemStack item : bundle.getItems()) {
      if (item.hasItemMeta()) {
        Component displayName = item.getItemMeta().displayName();
        if (item.getItemMeta().hasDisplayName() && displayName != null) {
          Component append = TextUtil.format("&7 - " + item.getAmount() + "x ").append(displayName);
          builder.addLore(append);
        } else {
          builder.addLore("&7 - " + item.getAmount() + "x " + Utils.itemName(item));
        }
      } else {
        builder.addLore("&7 - " + item.getAmount() + "x " + Utils.itemName(item));
      }
    }
    ItemStack build = builder.addLore().build();
    NBTAPI.addNBT(build, "bundleID", bundle.getID());
    return build;
  }

  public ItemStack getBundleMenuDisplay(Bundle bundle) {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.BARREL)
            .name("&7" + bundle.getName())
            .addLore("&3ID: &7" + bundle.getID())
            .addLore("&3Description:")
            .addLore("&7" + (bundle.getDescription().isBlank() ? "&f( NONE )" : bundle.getDescription()))
            .addLore()
            .addLore("&3Contents:");
    for (ItemStack item : bundle.getItems()) {
      if (item.hasItemMeta()) {
        Component displayName = item.getItemMeta().displayName();
        if (item.getItemMeta().hasDisplayName() && displayName != null) {
          Component append = TextUtil.format("&7 - " + item.getAmount() + "x ").append(displayName);
          builder.addLore(append);
        } else {
          builder.addLore("&7 - " + item.getAmount() + "x " + Utils.itemName(item));
        }
      } else {
        builder.addLore("&7 - " + item.getAmount() + "x " + Utils.itemName(item));
      }
    }
    return builder
        .addLore()
        .addLore("&7( left-click to edit )")
        .addLore("&7( middle-click to get )")
        .addLore("&7( right-click to delete )")
        .build();
  }
}
