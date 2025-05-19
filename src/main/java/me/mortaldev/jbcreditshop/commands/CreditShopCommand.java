package me.mortaldev.jbcreditshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;

import java.util.Optional;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.menus.ShopsMenu;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("creditshop|crshop")
public class CreditShopCommand extends BaseCommand {

  @Default
  @CommandPermission("jbcreditshop.admin")
  public void shopsMenu(Player player) {
    GUIManager.getInstance().openGUI(new ShopsMenu(new MenuData()), player);
  }

  @Subcommand("open")
  @CommandCompletion("@shops")
  public void openShop(Player player, String shopID) {
    Optional<Shop> shopOptional = ShopManager.getInstance().getShop(shopID);
    if (shopOptional.isEmpty()) {
      player.sendMessage(TextUtil.format("&cShop not found by that id."));
      Main.playDenySound(player);
      return;
    }
    Shop shop = shopOptional.get();
    if (shop.isLocked() && !player.hasPermission("jbcreditshop.admin")) {
      if (shop.getLockedBypassPermission() == null
          || shop.getLockedBypassPermission().isBlank()
          || !player.hasPermission(shop.getLockedBypassPermission())) {
        player.sendMessage(TextUtil.format("&cYou cannot access this shop."));
        Main.playDenySound(player);
        return;
      }
    }
    ShopManager.getInstance().openShop(shop, player, false);
  }

  @Subcommand("reload")
  @CommandPermission("jbcreditshop.admin")
  public void reload(CommandSender sender) {
    ShopManager.getInstance().loadShops();
    ShopItemsManager.getInstance().loadShopItems();
    sender.sendMessage("Reloaded! Check console for any errors if present.");
  }
}
