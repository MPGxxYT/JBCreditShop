package me.mortaldev.jbcreditshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import java.util.Optional;

import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.entity.Player;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.menus.ShopsMenu;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;

@CommandAlias("crshop")
public class CreditShopCommand extends BaseCommand {

  @Default
  @CommandPermission("jbcreditshop.admin")
  public void shopsMenu(Player player) {
    GUIManager.getInstance().openGUI(new ShopsMenu(new MenuData()), player);
  }

  @CommandAlias("open")
  @CommandCompletion("@shops")
  public void openShop(Player player, String shopID) {
    Optional<Shop> shopOptional = ShopManager.getInstance().getShop(shopID);
    if (shopOptional.isEmpty()) {
      player.sendMessage(TextUtil.format("&cShop not found by that id."));
      Main.playDenySound(player);
      return;
    }
    Shop shop = shopOptional.get();
    if (!player.hasPermission("jbcreditshop.admin") || shop.isLocked()) {
      if (shop.getLockedBypassPermission().isBlank() || !player.hasPermission(shop.getLockedBypassPermission())) {
        player.sendMessage(TextUtil.format("&cYou cannot access this shop."));
        Main.playDenySound(player);
        return;
      }
    }
    ShopManager.getInstance().openShop(shop, player, false);
  }

  @CommandAlias("reload")
  @CommandPermission("jbcreditshop.admin")
  public void reload(Player player) {
    ShopManager.getInstance().loadShops();
    ShopItemsManager.getInstance().loadShopItems();
    player.sendMessage(TextUtil.format("&aReloaded! Check console for any errors if present."));
  }

}
