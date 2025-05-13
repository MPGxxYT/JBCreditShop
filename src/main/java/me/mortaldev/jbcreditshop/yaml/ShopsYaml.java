package me.mortaldev.jbcreditshop.yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.mortaldev.YAML;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.records.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ShopsYaml {

  private static final String PATH = "/shops/";
  private static final String SHOPS_FILE_NAME =
      "shops.yml"; // Defines the primary configuration file for shops

  // Singleton pattern implementation
  private static class Singleton {
    private static final ShopsYaml INSTANCE = new ShopsYaml();
  }

  public static synchronized ShopsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopsYaml() {}

  private void missingValueMessage(String key, String configName) {
    Main.warn("Missing REQUIRED " + key + " for shop " + configName);
  }

  public Set<Shop> read() {
    Set<Shop> shops = new HashSet<>();
    HashMap<FileConfiguration, String> configs = YamlReader.getInstance().getConfigs(PATH);

    for (Map.Entry<FileConfiguration, String> entry : configs.entrySet()) {
      // Logic to potentially handle multiple shop files; currently focused on SHOPS_FILE_NAME
      if (!entry.getValue().endsWith(SHOPS_FILE_NAME)) {
        // If you have a multi-file setup and this file shouldn't be parsed here, uncomment:
        // continue;
      }
      FileConfiguration config = entry.getKey();
      String path = entry.getValue();
      for (String stringKey : config.getValues(false).keySet()) {
        ConfigurationSection section = config.getConfigurationSection(stringKey);
        if (section != null) {
          Shop shop = toShop(section);
          shop.setSource(new Pair<>(config, path));
          shop.setSection(section);
          shops.add(shop);
        }
      }
    }
    Main.log("Loaded " + shops.size() + " shops");
    return shops;
  }

  // Helper method to find the next available sequential numeric key for a new shop section.
  private int findNextAvailableNumericKey(FileConfiguration config) {
    int maxKey = -1; // Initialize to -1 so the first key becomes 0 if no numeric keys exist
    Set<String> keys = config.getValues(false).keySet();
    if (keys.isEmpty()) {
      return 0;
    }
    for (String key : keys) {
      try {
        int numericKey = Integer.parseInt(key);
        if (numericKey > maxKey) {
          maxKey = numericKey;
        }
      } catch (NumberFormatException e) {
        // Ignores non-numeric keys when determining the next sequence number.
      }
    }
    return maxKey + 1;
  }

  public void delete(Shop shop) {
    if (shop.getSource() == null
        || shop.getSource().first() == null
        || shop.getSource().second() == null) {
      Main.error(
          "Shop object (ID: "
              + (shop.getShopID() != null ? shop.getShopID() : "N/A")
              + ") is missing source FileConfiguration or path. Cannot delete.");
      return;
    }
    FileConfiguration config = shop.getSource().first();
    String configPath = shop.getSource().second();

    File file = new File(Main.getInstance().getDataFolder(), configPath);
    if (!file.exists()) {
      Main.error("Shop object (ID: " + shop.getShopID() + ") does not exist. Cannot delete.");
      return;
    }

    config.set(shop.getSection().getCurrentPath(), null);
    YAML.getInstance().saveConfig(config, configPath);
  }

  public void create(Shop shop) {
    String shopsFilePath = PATH + SHOPS_FILE_NAME;
    File file = new File(Main.getInstance().getDataFolder(), shopsFilePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException("Failed to create shop file: " + file.getAbsolutePath(), e);
      }
    }

    FileConfiguration config = YAML.getInstance().getConfig(shopsFilePath);
    shop.setSource(new Pair<>(config, shopsFilePath));

    String newSectionKey = String.valueOf(findNextAvailableNumericKey(config));
    ConfigurationSection section = config.createSection(newSectionKey);
    shop.setSection(section);

    save(shop);
  }

  public void save(Shop shop) {
    if (shop.getSource() == null
        || shop.getSource().first() == null
        || shop.getSource().second() == null) {
      Main.error(
          "Shop object (ID: "
              + (shop.getShopID() != null ? shop.getShopID() : "N/A")
              + ") is missing source FileConfiguration or path. Cannot save.");
      return;
    }
    FileConfiguration config = shop.getSource().first();
    String configPath = shop.getSource().second();

    File file = new File(Main.getInstance().getDataFolder(), configPath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(
            "Failed to create shop file during save: " + file.getAbsolutePath(), e);
      }
    }

    ConfigurationSection sectionToSaveIn = shop.getSection();

    if (sectionToSaveIn == null) {
      // This fallback is crucial if a shop object reaches save() without a pre-assigned section.
      Main.error(
          "Shop section is null for shopID: "
              + shop.getShopID()
              + " during save. "
              + "The shop was not properly prepared. Attempting to create a new numeric section as a fallback.");
      String newSectionKey = String.valueOf(findNextAvailableNumericKey(config));
      sectionToSaveIn = config.createSection(newSectionKey);
      shop.setSection(sectionToSaveIn);
    }

    write(sectionToSaveIn, shop); // Pass the correct section (e.g., "0", "1") to write into.
    YAML.getInstance().saveConfig(config, configPath);
  }

  // Writes the shop's data into the provided ConfigurationSection.
  // The 'section' parameter is the parent key (e.g., "0", "1") for this shop's data.
  private void write(ConfigurationSection section, Shop shop) {
    HashMap<String, Class<?>> defaultFormat = getDefaultFormat(shop);
    HashMap<String, Supplier<Boolean>> booleanSuppliers = getBooleanSuppliers(shop);
    HashMap<String, Supplier<Integer>> integerSuppliers = getIntegerSuppliers(shop);
    HashMap<String, Supplier<String>> stringSuppliers = getStringSuppliers(shop);

    for (Map.Entry<String, Class<?>> entry : defaultFormat.entrySet()) {
      String key = entry.getKey();
      Class<?> clazz = entry.getValue();
      if (clazz == Boolean.class) {
        if (booleanSuppliers.containsKey(key) && booleanSuppliers.get(key).get() != null) {
          section.set(key, booleanSuppliers.get(key).get());
        }
      } else if (clazz == Integer.class) {
        if (integerSuppliers.containsKey(key) && integerSuppliers.get(key).get() != null) {
          section.set(key, integerSuppliers.get(key).get());
        }
      } else if (clazz == Enum.class) { // Handles Enum types, specifically 'style'
        if (key.equals("style") && shop.getStyle() != null) {
          section.set(key, shop.getStyle().name()); // Stores the enum by its string name
        }
      } else if (clazz == Map.class) {
        if (key.equals("filler")) {
          section.set(key, shop.getFiller());
        }
      } else { // Default handling, primarily for String.class
        if (stringSuppliers.containsKey(key) && stringSuppliers.get(key).get() != null) {
          // Avoids setting a key if both key and value are blank, which can be ambiguous.
          if (key.isBlank() && stringSuppliers.get(key).get().isBlank()) {
            continue;
          }
          section.set(key, stringSuppliers.get(key).get());
        }
      }
    }
  }

  private HashMap<String, Supplier<String>> getStringSuppliers(Shop shop) {
    return new HashMap<>() {
      {
        put("shop_id", shop::getShopID);
        put("shop_display", shop::getShopDisplay);
        put("default_display_material", shop::getDefaultDisplayMaterialAsString);
        put("locked_bypass_permission", shop::getLockedBypassPermission);
        put("discount_group", shop::getDiscountGroup);
      }
    };
  }

  private HashMap<String, Supplier<Integer>> getIntegerSuppliers(Shop shop) {
    return new HashMap<>() {
      {
        put("default_price", shop::getDefaultPrice);
        put("discount", shop::getDiscount);
        put("size", shop::getSize);
      }
    };
  }

  private HashMap<String, Supplier<Boolean>> getBooleanSuppliers(Shop shop) {
    return new HashMap<>() {
      {
        put("locked", shop::isLocked);
      }
    };
  }

  private HashMap<String, Class<?>> getDefaultFormat(Shop shop) {
    HashMap<String, Class<?>> hashMap =
        new HashMap<>() {
          {
            put("shop_id", String.class);
            put("shop_display", String.class);
            put("default_price", Integer.class);
            put("default_display_material", String.class);
            put("locked", Boolean.class);
            put("locked_bypass_permission", String.class);
            put("discount", Integer.class);
            put("discount_group", String.class);
            put("style", Enum.class); // Assumes Shop.Style is an Enum
            put("size", Integer.class);
            put("filler", Map.class);
          }
        };
    if (shop.getStyle()
        != Shop.Style.CUSTOM) { // Ensures Shop.Style.CUSTOM is the correct enum constant
      hashMap.remove("size");
      hashMap.remove("filler");
    }
    return hashMap;
  }

  private Shop.Builder getBooleanValues(ConfigurationSection config, Shop.Builder builder) {
    HashMap<String, Boolean> keys =
        new HashMap<>() {
          {
            put("locked", false); // field_name, is_required
          }
        };
    HashMap<String, Consumer<Boolean>> consumer =
        new HashMap<>() {
          {
            put("locked", builder::setLocked);
          }
        };
    for (String key : keys.keySet()) {
      if (config.contains(key)) {
        consumer.get(key).accept(config.getBoolean(key));
      } else if (keys.get(key)) { // If required and not present
        missingValueMessage(key, config.getName());
      }
    }
    return builder;
  }

  private Shop.Builder getDigitValues(ConfigurationSection config, Shop.Builder builder) {
    HashMap<String, Boolean> keys =
        new HashMap<>() {
          {
            put("default_price", true);
            put("discount", false);
            put("size", false);
          }
        };
    HashMap<String, Consumer<Integer>> consumer =
        new HashMap<>() {
          {
            put("default_price", builder::setDefaultPrice);
            put("discount", builder::setDiscount);
            put("size", builder::setSize);
          }
        };
    for (String key : keys.keySet()) {
      if (config.contains(key)) {
        if (key.equals("size")) {
          // 'size' attribute is only applicable if the shop style is CUSTOM.
          String styleStr = config.getString("style", Shop.Style.AUTO.name());
          if (!Shop.Style.CUSTOM.name().equalsIgnoreCase(styleStr)) {
            continue;
          }
        }
        consumer.get(key).accept(config.getInt(key));
      } else if (keys.get(key)) { // If required and not present
        missingValueMessage(key, config.getName());
      }
    }
    return builder;
  }

  private Shop.Builder getSingleStringValues(ConfigurationSection config, Shop.Builder builder) {
    HashMap<String, Boolean> keys =
        new HashMap<>() {
          {
            put("shop_id", true);
            put("shop_display", false);
            put("default_display_material", true);
            put("locked_bypass_permission", false);
            put("discount_group", false);
            put("style", true);
          }
        };
    HashMap<String, Consumer<String>> consumer =
        new HashMap<>() {
          {
            put("shop_id", builder::setShopID);
            put("shop_display", builder::setShopDisplay);
            put("default_display_material", builder::setDefaultDisplayMaterial);
            put("locked_bypass_permission", builder::setLockedBypassPermission);
            put("discount_group", builder::setDiscountGroup);
            put(
                "style",
                value -> { // Robustly sets the style, defaulting to AUTO on parse failure.
                  try {
                    builder.setStyleAsEnum(Shop.Style.valueOf(value.toUpperCase()));
                  } catch (IllegalArgumentException e) {
                    Main.warn(
                        "Invalid shop style '"
                            + value
                            + "' in section "
                            + config.getName()
                            + ". Defaulting to AUTO.");
                    builder.setStyleAsEnum(Shop.Style.AUTO);
                  }
                });
          }
        };

    for (String key : keys.keySet()) {
      String value = config.getString(key);
      if (value == null || value.isBlank()) {
        if (keys.get(key)) { // If value is required
          missingValueMessage(key, config.getName());
        }
        continue;
      }
      consumer.get(key).accept(value);
    }
    return builder;
  }

  private Shop.Builder getFiller(ConfigurationSection config, Shop.Builder builder) {
    HashMap<Integer, String> loadedMap = new HashMap<>();
    ConfigurationSection section = config.getConfigurationSection("filler");
    if (section == null) {
      return builder;
    }
    for (String keyString : section.getKeys(false)) {
      int intKey = Integer.parseInt(keyString);
      String value = section.getString(keyString);
      loadedMap.put(intKey, value);
    }
    builder.setFiller(loadedMap);
    return builder;
  }

  private Shop toShop(ConfigurationSection config) {
    Shop.Builder builder = Shop.builder();
    // The order of these calls might matter if there are interdependencies in how Shop objects are
    // built.
    getSingleStringValues(config, builder);
    getBooleanValues(config, builder);
    getDigitValues(config, builder);
    getFiller(config, builder);
    return builder.build();
  }
}
