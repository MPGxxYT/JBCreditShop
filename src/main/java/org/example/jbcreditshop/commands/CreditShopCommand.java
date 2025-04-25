package org.example.jbcreditshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import java.util.Optional;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.entity.Player;
import org.example.jbcreditshop.Main;
import org.example.jbcreditshop.menus.MainMenu;
import org.example.jbcreditshop.modules.Shop;
import org.example.jbcreditshop.modules.ShopItemsManager;
import org.example.jbcreditshop.modules.ShopManager;
import org.example.jbcreditshop.utils.TextUtil;

@CommandAlias("crshop")
public class CreditShopCommand extends BaseCommand {

  @Default
  public void open(Player player) {
    GUIManager.getInstance().openGUI(new MainMenu(), player);
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
    ShopManager.getInstance().openShop(shop, player, false);
  }

  @CommandAlias("reload")
  public void reload(Player player) {
    ShopManager.getInstance().loadShops();
    ShopItemsManager.getInstance().loadShopItems();
    player.sendMessage(TextUtil.format("&aReloaded!"));
  }

}
