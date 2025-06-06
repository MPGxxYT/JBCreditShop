package me.mortaldev.jbcreditshop;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;

import me.mortaldev.YAML;
import me.mortaldev.jbcreditshop.commands.BundleCommand;
import me.mortaldev.jbcreditshop.commands.CreditShopCommand;
import me.mortaldev.jbcreditshop.listeners.BundleListener;
import me.mortaldev.jbcreditshop.listeners.ChatListener;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.modules.bundles.Bundle;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbcreditshop.modules.shopstats.ShopStatsCRUD;
import me.mortaldev.jbcreditshop.modules.transaction.TransactionLogManager;
import me.mortaldev.menuapi.GUIListener;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

  private static final String LABEL = "JBCreditShop";
  private static final HashSet<String> dependencies =
      new HashSet<>() {
        {
          add("EcoBits");
        }
      };
  private static Main instance;
  private static boolean debug = false;

  public static void playDenySound(Player player) {
    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
  }

  public static void playAcceptSound(Player player) {
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            instance,
            () ->
                player.playSound(
                    player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.707107f),
            1);
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            instance,
            () ->
                player.playSound(
                    player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.890899f),
            3);
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            instance,
            () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.059463f),
            5);
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

  public static void error(String message) {
    Bukkit.getLogger().severe("[" + Main.getLabel() + "] " + message);
  }

  // Timezone adjusted local date
  public static LocalDateTime getLocalDateTime() {
    return ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDateTime();
  }

  public static YAML getYAML() {
    YAML instance1 = YAML.getInstance();
    instance1.setMain(getInstance());
    return instance1;
  }

  @Override
  public void onEnable() {
    instance = this;
    PaperCommandManager commandManager = new PaperCommandManager(this); // AikarsCommands

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

    //  - YAML BASED
    ShopManager.getInstance().loadShops();
    ShopItemsManager.getInstance().loadShopItems();

    //  - CRUD BASED
    PlayerDataManager.getInstance().load();
    ShopStatsCRUD.getInstance().load();
    TransactionLogManager.getInstance().load();
    BundleManager.getInstance().load();

    // GUI Manager
    GUIListener guiListener = new GUIListener(GUIManager.getInstance()); // MenuAPI
    Bukkit.getPluginManager().registerEvents(guiListener, this);

    // Events

    getServer().getPluginManager().registerEvents(new ChatListener(), this);
    getServer().getPluginManager().registerEvents(new BundleListener(), this);

    // COMMANDS
    commandManager
        .getCommandCompletions()
        .registerCompletion(
            "shops",
            c ->
                ShopManager.getInstance().getShops().stream()
                    .map(Shop::getShopID)
                    .collect(ImmutableList.toImmutableList()));
    commandManager
        .getCommandCompletions()
        .registerCompletion(
            "bundles",
            c ->
                BundleManager.getInstance().getSet().stream()
                    .map(Bundle::getID)
                    .collect(ImmutableList.toImmutableList()));

    commandManager.registerCommand(new CreditShopCommand());
    commandManager.registerCommand(new BundleCommand());

    getLogger().info(LABEL + " Enabled");
  }

  // TODO: ADD README.md for instructions

  @Override
  public void onDisable() {
    getLogger().info(LABEL + " Disabled");
  }
}
