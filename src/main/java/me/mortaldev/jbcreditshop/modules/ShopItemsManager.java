package me.mortaldev.jbcreditshop.modules;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import me.mortaldev.jbcreditshop.modules.shopstats.ShopStatsCRUD;
import me.mortaldev.jbcreditshop.modules.transaction.TransactionLog;
import me.mortaldev.jbcreditshop.modules.transaction.TransactionLogManager;
import me.mortaldev.jbcreditshop.modules.transaction.data.Transaction;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.ecobits.EcoBitsAccount;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerData;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import me.mortaldev.jbcreditshop.yaml.ShopItemsYaml;

public class ShopItemsManager {

  public static final String NOT_ENOUGH_SPACE_MESSAGE = "&cNot enough space in your inventory.";
  Set<ShopItem> shopItems = new HashSet<>();
  HashMap<String, Set<ShopItem>> shopItemsByShopID = new HashMap<>();

  private static class Singleton {
    private static final ShopItemsManager INSTANCE = new ShopItemsManager();
  }

  public static synchronized ShopItemsManager getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopItemsManager() {}

  public ItemStack getShopMenuStack(ShopItem shopItem, boolean adminMode, Player player) {
    Material display = shopItem.getDisplayMaterial();
    if (display == null || display.isAir()) {
      display =
          ShopManager.getInstance().getShop(shopItem.getShopID()).get().getDefaultDisplayMaterial();
    }
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(display).name(shopItem.getDisplayName());
    if (!shopItem.getDescription().isEmpty()) {
      builder.addLore(shopItem.getDescription()).addLore("");
    }
    if (adminMode) {
      builder
          .addLore("&3&lID: &f" + shopItem.getItemID())
          .addLore("&3&lShop ID: &f" + shopItem.getShopID());
    }
    if (adminMode) {
      builder
          .addLore("&3&lLocked: &f" + shopItem.isLocked())
          .addLore("&3&lVisible: &f" + shopItem.canBeDisplayed())
          .addLore("&3&lPrice: &f" + shopItem.getPrice())
          .addLore("")
          .addLore("&7 ( click to modify )");
    } else {
      String purchasedPermission = shopItem.getPurchasedPermission();
      if (!purchasedPermission.isBlank()) {
        if (player.hasPermission(purchasedPermission)) {
          builder.addLore("&cYou already own this. (one time purchase)");
          return builder.build();
        }
      }
      builder
          .addLore("&3&lPrice: &f" + shopItem.getPrice())
          .addLore("")
          .addLore("&7 ( click to purchase )");
    }
    return builder.build();
  }

  public void loadShopItems() {
    shopItems = ShopItemsYaml.getInstance().read();
    sortByShop();
  }

  public void sortByShop() {
    for (ShopItem shopItem : shopItems) {
      if (!shopItemsByShopID.containsKey(shopItem.getShopID())) {
        shopItemsByShopID.put(shopItem.getShopID(), new HashSet<>(){{add(shopItem);}});
      }
      shopItemsByShopID.get(shopItem.getShopID()).add(shopItem);
    }
  }

  public Set<ShopItem> getShopItems() {
    return shopItems;
  }

  public ShopItem getShopItem(String name) {
    for (ShopItem shopItem : shopItems) {
      if (shopItem.getItemID().equalsIgnoreCase(name)) {
        return shopItem;
      }
    }
    return null;
  }

  public Set<ShopItem> getByShopID(String shopID, boolean sortByShop) {
    if (sortByShop) {
      sortByShop();
    }
    Set<ShopItem> items = new HashSet<>();
    for (ShopItem shopItem : shopItems) {
      if (shopItem.getShopID().equalsIgnoreCase(shopID)) {
        items.add(shopItem);
      }
    }
    return items;
  }

  public void addShopItem(ShopItem shopItem) {
    shopItems.add(shopItem);
    ShopItemsYaml.getInstance().create(shopItem);
  }

  public boolean canAllowPurchase(ShopItem shopItem, Player player) {
    String purchasedPermission = shopItem.getPurchasedPermission();
    if (!purchasedPermission.isBlank()) {
      if (player.hasPermission(purchasedPermission)) {
        player.sendMessage(TextUtil.format("&cYou already own this!"));
        Main.playDenySound(player);
        return false;
      }
    }
    BigDecimal currentBalance = new EcoBitsAccount(player).getCurrentBalance();
    if (currentBalance.compareTo(BigDecimal.valueOf(shopItem.getPrice())) < 0) {
      player.sendMessage(
          TextUtil.format("&cYou cannot afford this. Not enough credits!"));
      Main.playDenySound(player);
      return false;
    }
    if (shopItem.isOneTimePurchase()) {
      PlayerData playerData = PlayerDataManager.getInstance().getByID(player.getUniqueId().toString()).orElse(PlayerData.create(player.getUniqueId().toString()));
      if (playerData.hasPurchasedItem(shopItem)) {
        player.sendMessage(TextUtil.format("&cYou can only purchase this once!"));
        Main.playDenySound(player);
        return false;
      }
    }
    return true;
  }

  public void purchaseShopItem(ShopItem shopItem, Player player) {
    if (!shopItem.canBeDisplayed()) {
      player.sendMessage(TextUtil.format("&cThis item is not for sale."));
      Main.playDenySound(player);
      return;
    }
    PlayerData playerData = PlayerDataManager.getInstance().getByID(player.getUniqueId().toString()).orElse(PlayerData.create(player.getUniqueId().toString()));
    if (shopItem.isOneTimePurchase()) {
      if (playerData.hasPurchasedItem(shopItem)) {
        player.sendMessage(TextUtil.format("&cYou can only purchase this once!"));
        Main.playDenySound(player);
        return;
      }
    }
    if (!shopItem.getPurchasedCommand().isBlank()) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), shopItem.getPurchasedCommand().replaceFirst("/", "").replace("%player%", player.getName()));
    }
    if (shopItem.getPurchasedItem() != null && !shopItem.getPurchasedItem().getType().isAir()) {
      if (!Utils.canInventoryHold(player.getInventory(), shopItem.getPurchasedItem())) {
        player.sendMessage(TextUtil.format(NOT_ENOUGH_SPACE_MESSAGE));
        Main.playDenySound(player);
        return;
      } else {
        player.getInventory().addItem(shopItem.getPurchasedItem());
      }
    }
    if (!shopItem.getPurchasedPermission().isBlank()) {
      LuckPerms luckPerms = LuckPermsProvider.get();
      User user = luckPerms.getUserManager().getUser(player.getUniqueId());
      if (user == null) {
        player.sendMessage(TextUtil.format("&cFailed to process."));
        return;
      }
      user.data().add(Node.builder(shopItem.getPurchasedPermission()).build());
      luckPerms.getUserManager().saveUser(user);
    }
    // Transaction Logging
    Transaction transaction = new Transaction(player.getUniqueId().toString(), shopItem.getItemID(), shopItem.getPrice());
    TransactionLogManager.getInstance().addTransaction(transaction);
    // **************************
    ShopStatsCRUD.getInstance().get().addPurchase(shopItem.getItemID());
    playerData.addPurchasedItem(shopItem, 1);
    PlayerDataManager.getInstance().update(playerData);
    player.sendMessage(TextUtil.format("&3Purchased " + shopItem.getDisplayName() + " &3for &l" + shopItem.getPrice() + "&3 credits."));
    new EcoBitsAccount(player).remove(BigDecimal.valueOf(shopItem.getPrice()));
  }
}
