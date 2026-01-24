package me.mortaldev.jbcreditshop;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.logging.Logger;

import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.crudapi.loading.CRUDRegistry;
import me.mortaldev.jbcreditshop.commands.BundleCommand;
import me.mortaldev.jbcreditshop.commands.CreditShopCommand;
import me.mortaldev.jbcreditshop.listeners.BundleListener;
import me.mortaldev.jbcreditshop.listeners.ChatListener;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItemsManager;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.modules.bundles.Bundle;
import me.mortaldev.jbcreditshop.modules.bundles.BundleCRUD;
import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataCRUD;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbcreditshop.modules.shopstats.ShopStatsCRUD;
import me.mortaldev.jbcreditshop.modules.transaction.TransactionLog;
import me.mortaldev.jbcreditshop.modules.transaction.TransactionLogCRUD;
import me.mortaldev.jbcreditshop.modules.transaction.TransactionLogManager;
import me.mortaldev.jbcreditshop.serializers.ItemStackDeserializer;
import me.mortaldev.jbcreditshop.serializers.ItemStackSerializer;
import me.mortaldev.menuapi.GUIListener;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

  private Jackson jackson;
  private CRUDRegistry crudRegistry;
  private PluginServices pluginServices;

  // ========== PATHS ==========
  private final String PLAYER_DATA_PATH = getDataFolder() + "/playerdata/";
  private final String BUNDLES_DATA_PATH = getDataFolder() + "/bundles/";
  private final String SHOP_STATS_DATA_PATH = getDataFolder() + "/plugin/";
  private final String TRANSACTION_LOG_DATA_PATH = getDataFolder() + "/transactions/";

  // ========== CRUD ==========

  private PlayerDataCRUD playerDataCRUD;
  private BundleCRUD bundleCRUD;
  private TransactionLogCRUD transactionLogCRUD;
  private ShopStatsCRUD shopStatsCRUD;

  // ========== Registries & Managers ==========

  private PaperCommandManager commandManager;
  private GUIManager guiManager;

  //
  private PlayerDataManager playerDataManager;
  private TransactionLogManager transactionLogManager;
  private BundleManager bundleManager;

  private static final String LABEL = "JBCreditShop";
  private static final HashSet<String> dependencies =
      new HashSet<>() {
        {
          add("EcoBits");
        }
      };
  private static Main instance;
  private static final Logger logger = Logger.getLogger(LABEL);
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
            () ->
                player.playSound(
                    player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.059463f),
            5);
  }

  @Deprecated(since = "Use info() instead.")
  public static void log(String message) {
    logger.info(message);
  }

  public static void info(String str) {
    log(str);
  }

  public static void warn(String message) {
    logger.warning(message);
  }

  public static void severe(String message) {
    logger.severe(message);
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

  // Timezone adjusted local date
  public static LocalDateTime getLocalDateTime() {
    return ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDateTime();
  }

  public PluginServices getPluginServices() {
    return pluginServices;
  }

  @Override
  public void onEnable() {
    instance = this;
    this.pluginServices = new PluginServices();

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
    // -- NONE ATM

    // Managers (Loading data)
    registerAdapters();

    //  - YAML BASED
    ShopManager.getInstance().loadShops();
    ShopItemsManager.getInstance().loadShopItems();

    //  - CRUD BASED
    instantiateCrudManagers();
    instantiateOtherManagers();

    // Events
    registerBukkitEventListeners();

    // COMMANDS
    registerCommandCompletions();
    registerCommands();

    getLogger().info(LABEL + " Enabled");
  }

  private void registerCommands() {
    commandManager.registerCommand(new CreditShopCommand());
    commandManager.registerCommand(new BundleCommand(bundleManager));
  }

  private void registerCommandCompletions() {
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
                bundleManager.getSet().stream()
                    .map(Bundle::getID)
                    .collect(ImmutableList.toImmutableList()));
  }

  private void registerBukkitEventListeners() {
    getServer().getPluginManager().registerEvents(new GUIListener(guiManager), this);
    getServer().getPluginManager().registerEvents(new ChatListener(), this);
    getServer().getPluginManager().registerEvents(new BundleListener(bundleManager), this);
  }

  private void instantiateOtherManagers() {
    this.commandManager = new PaperCommandManager(this);
    this.guiManager = GUIManager.getInstance();
  }

  private void instantiateCrudManagers() {
    this.jackson = new Jackson();
    this.crudRegistry = new CRUDRegistry();

    // ========== CRUD ==========
    this.playerDataCRUD = new PlayerDataCRUD(PLAYER_DATA_PATH, jackson);
    this.bundleCRUD = new BundleCRUD(BUNDLES_DATA_PATH, jackson);
    this.transactionLogCRUD = new TransactionLogCRUD(TRANSACTION_LOG_DATA_PATH, jackson);
    this.shopStatsCRUD = new ShopStatsCRUD(jackson, SHOP_STATS_DATA_PATH);

    // ========== Registries & Managers ==========
    this.playerDataManager = new PlayerDataManager(playerDataCRUD, crudRegistry, logger);
    this.bundleManager = new BundleManager(bundleCRUD, crudRegistry, logger);
    this.transactionLogManager = new TransactionLogManager(transactionLogCRUD, crudRegistry, logger);

    // ========== Service Registry ==========
    pluginServices.register(PlayerDataManager.class, playerDataManager);
    pluginServices.register(TransactionLogManager.class, transactionLogManager);
    pluginServices.register(BundleManager.class, bundleManager);
    pluginServices.register(ShopStatsCRUD.class, shopStatsCRUD);
  }

  private void registerAdapters() {
    crudRegistry
        .getGlobalAdapters()
        .addSerializer(ItemStack.class, new ItemStackSerializer())
        .addDeserializer(ItemStack.class, new ItemStackDeserializer());
  }

  @Override
  public void onDisable() {
    getLogger().info(LABEL + " Disabled");
  }
}
