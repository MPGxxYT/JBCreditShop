package me.mortaldev.jbcreditshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.mortaldev.jbcreditshop.menus.bundle.BundleMenu;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.modules.bundles.Bundle;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@CommandAlias("bundle")
public class BundleCommand extends BaseCommand {

  @Default
  @CommandPermission("jbcreditshop.admin||jbcreditshop.bundle")
  public void defaultMethod(Player player) {
    GUIManager.getInstance().openGUI(new BundleMenu(new MenuData()), player);
  }

  private void sendMessage(CommandSender sender, String message) {
    if (sender instanceof Player player) {
      player.sendMessage(TextUtil.format(message));
      return;
    }
    String colorless = TextUtil.removeColors(message);
    String decorless = TextUtil.removeDecoration(colorless);
    sender.sendMessage(decorless);
  }

  @CommandAlias("give")
  @CommandPermission("jbcreditshop.admin||jbcreditshop.bundle")
  @CommandCompletion("@bundles @players")
  @Syntax("<bundle> <player>")
  public void giveBundle(CommandSender sender, String bundleString, String playerString) {
    Optional<Bundle> bundleOptional = BundleManager.getInstance().getByID(bundleString);
    if (bundleOptional.isEmpty()) {
      sendMessage(sender, "&cThat bundle does not exist!");
      return;
    }
    Player recievingPlayer = Bukkit.getPlayer(playerString);
    if (recievingPlayer == null) {
      sendMessage(sender, "&cThat player is not online or exists!");
      return;
    }
    if (!recievingPlayer.isOnline()) {
      sendMessage(sender, "&cThat player is not online!");
      return;
    }
    Bundle bundle = bundleOptional.get();
    ItemStack bundleItem = BundleManager.getInstance().getBundleItemStack(bundle);
    if (!Utils.canInventoryHold(recievingPlayer.getInventory(), bundleItem)) {
      sendMessage(sender, "&cThat player has no space for this bundle.");
      return;
    }
    recievingPlayer.getInventory().addItem(bundleItem);
    sendMessage(
        sender,
        "&3Successfully gave &f" + bundle.getID() + "&3 to &f" + recievingPlayer.getName() + "&3.");
    recievingPlayer.sendMessage(
        TextUtil.format("&3You have recieved the " + bundle.getName() + "&3 bundle!"));
  }
}
