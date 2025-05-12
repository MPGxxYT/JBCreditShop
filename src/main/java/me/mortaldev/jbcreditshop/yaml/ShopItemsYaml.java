package me.mortaldev.jbcreditshop.yaml;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.mortaldev.YAML;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.records.Pair;
import org.jetbrains.annotations.NotNull;

public class ShopItemsYaml {

  private static final String PATH = "/shopitems/";

  private static class Singleton {
    private static final ShopItemsYaml INSTANCE = new ShopItemsYaml();
  }

  public static synchronized ShopItemsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopItemsYaml() {}

  public void create(ShopItem shopItem) {
    File dir = new File(Main.getInstance().getDataFolder() + PATH + "shopitems.yml");
    if (!dir.exists()) {
      dir.getParentFile().mkdirs();
      try {
        dir.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    FileConfiguration config = YAML.getInstance().getConfig(PATH + "shopitems.yml");
    shopItem.setSource(new Pair<>(config, PATH + "shopitems.yml"));
    Set<String> strings = config.getValues(false).keySet();
    int size = strings.size();
    ConfigurationSection section = config.createSection(size+"");
    shopItem.setSection(section);
    save(shopItem);
  }

  public void save(ShopItem shopItem) {
    Pair<FileConfiguration, String> source = shopItem.getSource();
    if (source == null) {
      return;
    }
    write(shopItem.getSection(), shopItem);
    YAML.getInstance().saveConfig(source.first(), source.second());
  }

  public Set<ShopItem> read() {
    Set<ShopItem> shopItems = new HashSet<>();
    HashMap<FileConfiguration, String> configs = YamlReader.getInstance().getConfigs(PATH);
    for (Map.Entry<FileConfiguration, String> entry : configs.entrySet()) {
      FileConfiguration config = entry.getKey();
      String path = entry.getValue();
      for (String string : config.getValues(false).keySet()) {
        ConfigurationSection section = config.getConfigurationSection(string);
        ShopItem shopItem = toShopItem(section);
        shopItem.setSource(new Pair<>(config, path));
        shopItem.setSection(section);
        shopItems.add(shopItem);
      }
    }
    Main.log("Loaded " + shopItems.size() + " shopitems");
    return shopItems;
  }

  private void missingValueMessage(String key, String configName) {
    Main.warn("Missing REQUIRED " + key + " for shopitem " + configName);
  }

  private void write(ConfigurationSection config, ShopItem shopItem) {
    Shop shop = ShopManager.getInstance().getShop(shopItem.getShopID()).orElse(new Shop());
    Map<String, Class<?>> defaultFormat = getDefaultFormat(shop);
    HashMap<String, Supplier<Boolean>> booleanSuppliers = getBooleanSuppliers(shopItem);
    HashMap<String, Supplier<Integer>> integerSuppliers = getIntegerSuppliers(shopItem);
    HashMap<String, Supplier<String>> stringSuppliers = getStringSuppliers(shopItem);
    for (Map.Entry<String, Class<?>> entry : defaultFormat.entrySet()) {
      String key = entry.getKey();
      Class<?> clazz = entry.getValue();
      if (clazz == Boolean.class) {
        config.set(key, booleanSuppliers.get(key).get());
      } else if (clazz == Integer.class) {
        config.set(key, integerSuppliers.get(key));
      } else if (clazz == List.class) {
        if (Objects.equals(key, "description")) {
          config.set(key, shopItem.getDescription());
        }
      } else {
        if (key.isBlank()) {
          continue;
        }
        config.set(key, stringSuppliers.get(key));
      }
    }
  }

  private Map<String, Class<?>> getDefaultFormat(Shop shop) {
    HashMap<String, Class<?>> defaultMap = new HashMap<>(){{
    put("item_id", String.class);
      put("shop_id", String.class);
      put("display_material", String.class);
      put("display_name", String.class);
      put("description", List.class);
      put("shop_slot", Integer.class);
      put("group", String.class);
      put("permission", String.class);
      put("purchased_item", String.class);
      put("purchased_command", String.class);
      put("purchased_permission", String.class);
      put("one_time_purchase", Boolean.class);
      put("price", Integer.class);
      put("discount", Integer.class);
      put("allow_discount_stacking", Boolean.class);
      put("locked", Boolean.class);
      put("locked_reason", String.class);
      put("visible", Boolean.class);
      put("archived", Boolean.class);
    }};
    if (shop.getStyle() != Shop.Style.CUSTOM) {
      defaultMap.
          remove("shop_slot");
    }
    return defaultMap;
  }

  private HashMap<String, Supplier<String>> getStringSuppliers(ShopItem shopItem) {
    return new HashMap<>(){{
      put("item_id", shopItem::getItemID);
      put("shop_id", shopItem::getShopID);
      put("display_material", shopItem::getDisplayMaterialAsString);
      put("display_name", shopItem::getDisplayName);
      put("group", shopItem::getGroup);
      put("permission", shopItem::getPermission);
      put("purchased_item", shopItem::getPurchasedItemSerialized);
      put("purchased_command", shopItem::getPurchasedCommand);
      put("purchased_permission", shopItem::getPurchasedPermission);
      put("locked_reason", shopItem::getLockedReason);
    }};
  }

  private HashMap<String, Supplier<Integer>> getIntegerSuppliers(ShopItem shopItem) {
    return new HashMap<>() {
      {
        put("shop_slot", shopItem::getShopSlot);
        put("price", shopItem::getPrice);
        put("discount", shopItem::getDiscount);
      }
    };
  }

  private HashMap<String, Supplier<Boolean>> getBooleanSuppliers(ShopItem shopItem) {
    return new HashMap<>(){{
      put("locked", shopItem::isLocked);
      put("visible", shopItem::isVisible);
      put("archived", shopItem::isArchived);
      put("one_time_purchase", shopItem::isOneTimePurchase);
      put("allow_discount_stacking", shopItem::isAllowDiscountStacking);
    }};
  }

  private static @NotNull HashMap<String, Consumer<Boolean>> getBooleanConsumers(ShopItem.Builder shopItemBuilder) {
    return new HashMap<>(){{
      put("locked", shopItemBuilder::setLocked);
      put("visible", shopItemBuilder::setVisible);
      put("archived", shopItemBuilder::setArchived);
      put("one_time_purchase", shopItemBuilder::setOneTimePurchase);
      put("allow_discount_stacking", shopItemBuilder::setAllowDiscountStacking);
    }};
  }

  private ShopItem.Builder getBooleanValues(ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("locked", false);
      put("visible", false);
      put("archived", false);
      put("one_time_purchase", false);
      put("allow_discount_stacking", false);
    }};
    HashMap<String, Consumer<Boolean>> consumer = getBooleanConsumers(shopItemBuilder);
    for (String key : keys.keySet()) {
      boolean value = config.getBoolean(key);
      consumer.get(key).accept(value);
    }
    return shopItemBuilder;
  }

  private ShopItem.Builder getDigitValues(ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("shop_slot", false);
      put("price", true);
      put("discount", true);
    }};
    HashMap<String, Consumer<Integer>> consumer = new HashMap<>(){{
      put("shop_slot", shopItemBuilder::setShopSlot);
      put("price", shopItemBuilder::setPrice);
      put("discount", shopItemBuilder::setDiscount);
    }};
    for (String key : keys.keySet()) {
      int value = config.getInt(key);
      consumer.get(key).accept(value);
    }
    return shopItemBuilder;
  }

  private ShopItem.Builder getSingleStringValues(ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("item_id", true);
      put("shop_id", true);
      put("display_material", false);
      put("display_name", false);
      put("group", false);
      put("permission", false);
      put("purchased_item", false);
      put("purchased_command", false);
      put("purchased_permission", false);
      put("locked_reason", false);
    }};
    HashMap<String, Consumer<String>> consumer = new HashMap<>(){{
      put("item_id", shopItemBuilder::setItemID);
      put("shop_id", shopItemBuilder::setShopID);
      put("display_material", shopItemBuilder::setDisplayMaterial);
      put("display_name", shopItemBuilder::setDisplayName);
      put("group", shopItemBuilder::setGroup);
      put("permission", shopItemBuilder::setPermission);
      put("purchased_item", shopItemBuilder::setPurchasedItem);
      put("purchased_command", shopItemBuilder::setPurchasedCommand);
      put("purchased_permission", shopItemBuilder::setPurchasedPermission);
      put("locked_reason", shopItemBuilder::setLockedReason);
    }};


    for (String key : keys.keySet()) {
      String value = config.getString(key);
      if (value == null || value.isBlank()) {
        if (keys.get(key)) {
          missingValueMessage(key, config.getName());
          return shopItemBuilder;
        }
        continue;
      }
      consumer.get(key).accept(value);
    }
    return shopItemBuilder;
  }

  private ShopItem toShopItem(ConfigurationSection config) {
    ShopItem.Builder shopItemBuilder = ShopItem.builder();
    getSingleStringValues(config, shopItemBuilder);
    getBooleanValues(config, shopItemBuilder);
    getDigitValues(config, shopItemBuilder);
    List<String> description = config.getStringList("description");
    if (!description.isEmpty()) {
      shopItemBuilder.setDescription(description);
    }
    return shopItemBuilder.build();
  }
}
