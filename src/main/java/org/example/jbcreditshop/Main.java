package org.example.jbcreditshop;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import me.mortaldev.menuapi.GUIListener;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.jbcreditshop.commands.CreditShopCommand;
import org.example.jbcreditshop.modules.Shop;
import org.example.jbcreditshop.modules.ShopItemsManager;
import org.example.jbcreditshop.modules.ShopManager;

public final class Main extends JavaPlugin {

  private static final String LABEL = "JBCreditShop";
  private static final HashSet<String> dependencies = new HashSet<>(){{
    add("EcoBits");
  }};
  private static Main instance;
  private static boolean debug = false;

  public static void playDenySound(Player player) {
    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
  }

  public static Main getInstance() {
    return instance;
  }

  public static String getLabel() {
    return LABEL;
  }

  public static boolean toggleDebug() {
    debug = !debug;
    return debug;
  }

  public static boolean isDebug() {
    return debug;
  }

  public static void debugLog(String message) {
    if (debug) {
      log(message);
    }
  }

  public static void log(String message) {
    Bukkit.getLogger().info("[" + Main.getLabel() + "] " + message);
  }

  public static void warn(String message) {
    Bukkit.getLogger().warning("[" + Main.getLabel() + "] " + message);
  }


  @Override
  public void onEnable() {
    instance = this;
    PaperCommandManager commandManager = new PaperCommandManager(this); //AikarsCommands



    // DATA FOLDER

    if (!getDataFolder().exists()) getDataFolder().mkdir();

    // DEPENDENCIES

    boolean disable = false;
    for (String plugin : dependencies) {
      if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
        getLogger().warning("Could not find " + plugin + "! This plugin is required.");
        disable = true;
      }
    }
    if (disable) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    // CONFIGS

    // Managers (Loading data)
    Bukkit.getScheduler().runTask(this, () -> {
      ShopManager.getInstance().loadShops();
      ShopItemsManager.getInstance().loadShopItems();
    });

    // GUI Manager
    GUIListener guiListener = new GUIListener(GUIManager.getInstance()); //MenuAPI
    Bukkit.getPluginManager().registerEvents(guiListener, this);

    // Events

    // getServer().getPluginManager().registerEvents(new EditingClickEvent(), this);

    // COMMANDS
    commandManager.getCommandCompletions().registerCompletion("shops", c -> {
      return ShopManager.getInstance().getShops().stream().map(Shop::getShopID).collect(ImmutableList.toImmutableList());
    });

    commandManager.registerCommand(new CreditShopCommand());

    getLogger().info(LABEL + " Enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info(LABEL + " Disabled");
  }
}
