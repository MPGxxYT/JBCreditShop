package me.mortaldev.jbcreditshop.yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.records.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

// Removed @NotNull as it's not used directly in the refined code's public API in a way that adds
// value over standard checks.

public class ShopItemsYaml {

  private static final String PATH = "/shopitems/";
  private static final String SHOP_ITEMS_FILE_NAME = "shopitems.yml"; // Consistent file naming

  // Singleton pattern
  private static class Singleton {
    private static final ShopItemsYaml INSTANCE = new ShopItemsYaml();
  }

  public static synchronized ShopItemsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopItemsYaml() {}

  private void missingValueMessage(String key, String configName, String itemID) {
    Main.warn(
        "Missing REQUIRED value for key '"
            + key
            + "' in config section '"
            + configName
            + "' (ItemID: "
            + (itemID != null ? itemID : "Unknown")
            + ")");
  }

  // Helper method to find the next available sequential numeric key.
  private int findNextAvailableNumericKey(FileConfiguration config) {
    int maxKey = -1; // Start at -1, so first key is 0
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
        // Ignore non-numeric keys for sequence generation
      }
    }
    return maxKey + 1;
  }

  public void delete(ShopItem shopItem) {
    if (shopItem.getSource() == null
        || shopItem.getSource().first() == null
        || shopItem.getSource().second() == null) {
      Main.error(
          "ShopItem (ID: "
              + (shopItem.getItemID() != null ? shopItem.getItemID() : "N/A")
              + ") is missing source FileConfiguration or path. Cannot delete.");
      return;
    }
    ConfigurationSection section = shopItem.getSection();
    if (section == null) {
      Main.error(
          "ShopItem (ID: "
              + (shopItem.getItemID() != null ? shopItem.getItemID() : "N/A")
              + ") has a null section. Cannot delete.");
      return;
    }

    FileConfiguration config = shopItem.getSource().first();
    String configPath = shopItem.getSource().second();
    config.set(section.getCurrentPath(), null);
    Main.getYAML().saveConfig(config, configPath);
  }

  public void create(ShopItem shopItem) {
    String itemsFilePath = PATH + SHOP_ITEMS_FILE_NAME;
    File file = new File(Main.getInstance().getDataFolder(), itemsFilePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(
            "Failed to create shop items file: " + file.getAbsolutePath(), e);
      }
    }

    FileConfiguration config = Main.getYAML().getConfig(itemsFilePath);
    shopItem.setSource(new Pair<>(config, itemsFilePath));

    String newSectionKey = String.valueOf(findNextAvailableNumericKey(config));
    ConfigurationSection section = config.createSection(newSectionKey);
    shopItem.setSection(section); // Assign the numerically keyed section to the item

    save(shopItem);
  }

  public void save(ShopItem shopItem) {
    if (shopItem.getSource() == null
        || shopItem.getSource().first() == null
        || shopItem.getSource().second() == null) {
      Main.error(
          "ShopItem (ID: "
              + (shopItem.getItemID() != null ? shopItem.getItemID() : "N/A")
              + ") is missing source FileConfiguration or path. Cannot save.");
      return;
    }
    if (shopItem.getSection() == null) {
      Main.error(
          "ShopItem (ID: "
              + (shopItem.getItemID() != null ? shopItem.getItemID() : "N/A")
              + ") has a null section. Cannot save properly. It might need to be created first.");
      return;
    }

    FileConfiguration config = shopItem.getSource().first();
    String configPath = shopItem.getSource().second();

    write(shopItem.getSection(), shopItem); // Write into the item's assigned section
    Main.getYAML().saveConfig(config, configPath);
  }

  public Set<ShopItem> read() {
    Set<ShopItem> shopItems = new HashSet<>();
    HashMap<FileConfiguration, String> configsMap = YamlReader.getInstance().getConfigs(PATH);

    for (Map.Entry<FileConfiguration, String> entry : configsMap.entrySet()) {
      FileConfiguration config = entry.getKey();
      String filePath = entry.getValue();

      // Assuming items are stored under numeric keys if SHOPS_ITEMS_FILE_NAME is used,
      // or could be item_id if other files are loaded by YamlReader.
      // This logic might need adjustment based on how YamlReader loads configs.
      if (!filePath.endsWith(SHOP_ITEMS_FILE_NAME)) {
        // Main.log("Skipping non-standard shop item file for numeric key processing: " + filePath);
        // continue; // Or handle differently if other files have different structures
      }

      for (String key : config.getValues(false).keySet()) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section != null) {
          ShopItem shopItem = toShopItem(section);
          shopItem.setSource(new Pair<>(config, filePath));
          shopItem.setSection(section); // The section (e.g., named "0", "1")
          shopItems.add(shopItem);
        }
      }
    }
    Main.log("Loaded " + shopItems.size() + " shop items.");
    return shopItems;
  }

  private void write(ConfigurationSection section, ShopItem shopItem) {
    // Resolve the parent Shop to determine context-dependent defaults (e.g., shop_slot)
    Optional<Shop> parentShopOpt = ShopManager.getInstance().getShop(shopItem.getShopID());
    if (parentShopOpt.isEmpty()) {
      Main.warn(
          "Parent shop with ID '"
              + shopItem.getShopID()
              + "' not found for ShopItem '"
              + shopItem.getItemID()
              + "'. Using default Shop settings for format.");
    }
    Shop parentShop = parentShopOpt.orElseGet(Shop::new); // Use a default shop if not found

    Map<String, Class<?>> defaultFormat =
        getDefaultFormat(parentShop); // Get format based on actual/default parent shop
    HashMap<String, Supplier<Boolean>> booleanSuppliers = getBooleanSuppliers(shopItem);
    HashMap<String, Supplier<Integer>> integerSuppliers = getIntegerSuppliers(shopItem);
    HashMap<String, Supplier<String>> stringSuppliers = getStringSuppliers(shopItem);

    for (Map.Entry<String, Class<?>> entry : defaultFormat.entrySet()) {
      String formatKey = entry.getKey();
      Class<?> clazz = entry.getValue();

      try {
        if (clazz == Boolean.class) {
          if (booleanSuppliers.containsKey(formatKey)
              && booleanSuppliers.get(formatKey).get() != null) {
            section.set(formatKey, booleanSuppliers.get(formatKey).get());
          }
        } else if (clazz == Integer.class) {
          if (integerSuppliers.containsKey(formatKey)
              && integerSuppliers.get(formatKey).get() != null) {
            section.set(formatKey, integerSuppliers.get(formatKey).get());
          }
        } else if (clazz == List.class) {
          // Explicitly handle list types like 'description'
          if (Objects.equals(formatKey, "description") && shopItem.getDescription() != null) {
            section.set(formatKey, shopItem.getDescription());
          }
          // Add other list properties here if any
        } else if (clazz == String.class) { // Explicitly String.class
          if (stringSuppliers.containsKey(formatKey)
              && stringSuppliers.get(formatKey).get() != null) {
            section.set(formatKey, stringSuppliers.get(formatKey).get());
          }
        } else {
          // Fallback for other types, though getDefaultFormat should ideally cover all.
          // This was previously implicitly handling String, now String is explicit.
          if (stringSuppliers.containsKey(formatKey)
              && stringSuppliers.get(formatKey).get() != null) {
            if (formatKey.isBlank() && stringSuppliers.get(formatKey).get().isBlank()) {
              continue;
            }
            section.set(formatKey, stringSuppliers.get(formatKey).get());
          }
        }
      } catch (NullPointerException e) {
        Main.warn(
            "Null value encountered from supplier for key '"
                + formatKey
                + "' in ShopItem '"
                + shopItem.getItemID()
                + "'. Skipping.");
      }
    }
  }

  private Map<String, Class<?>> getDefaultFormat(Shop shop) { // Takes Shop context
    Map<String, Class<?>> defaultMap =
        new HashMap<>() {
          {
            put("item_id", String.class);
            put("shop_id", String.class);
            put("use_display_itemstack", Boolean.class);
            put("display_itemstack", String.class); // Serialized ItemStack
            put("display_material", String.class);
            put("display_name", String.class);
            put("description", List.class);
            put("shop_slot", Integer.class); // Conditionally removed below
            put("group", String.class);
            put("permission", String.class);
            put("purchased_item", String.class); // Serialized ItemStack
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
          }
        };
    // If shop style is not custom, shop_slot might not be relevant.
    if (shop != null && shop.getStyle() != Shop.Style.CUSTOM) {
      defaultMap.remove("shop_slot");
    }
    return defaultMap;
  }

  private HashMap<String, Supplier<String>> getStringSuppliers(ShopItem shopItem) {
    return new HashMap<>() {
      {
        put("item_id", shopItem::getItemID);
        put("shop_id", shopItem::getShopID);
        put("display_material", shopItem::getDisplayMaterialAsString);
        put("display_name", shopItem::getDisplayName);
        put("group", shopItem::getGroup);
        put("permission", shopItem::getPermission);
        put("purchased_item", shopItem::getPurchasedItemSerialized);
        put("display_itemstack", shopItem::getDisplayItemStackSerialized);
        put("purchased_command", shopItem::getPurchasedCommand);
        put("purchased_permission", shopItem::getPurchasedPermission);
        put("locked_reason", shopItem::getLockedReason);
      }
    };
  }

  private HashMap<String, Supplier<Integer>> getIntegerSuppliers(ShopItem shopItem) {
    return new HashMap<>() {
      {
        put("shop_slot", shopItem::getShopSlot);
        put("price", shopItem::getRawPrice);
        put("discount", shopItem::getDiscount);
      }
    };
  }

  private HashMap<String, Supplier<Boolean>> getBooleanSuppliers(ShopItem shopItem) {
    return new HashMap<>() {
      {
        put("locked", shopItem::isLocked);
        put("visible", shopItem::isVisible);
        put("archived", shopItem::isArchived);
        put("one_time_purchase", shopItem::isOneTimePurchase);
        put("allow_discount_stacking", shopItem::isAllowDiscountStacking);
        put("use_display_itemstack", shopItem::isUseDisplayItemStack);
      }
    };
  }

  // Consumer maps for populating ShopItem.Builder from ConfigurationSection
  private static HashMap<String, Consumer<Boolean>> getBooleanConsumers(
      ShopItem.Builder shopItemBuilder) {
    return new HashMap<>() {
      {
        put("locked", shopItemBuilder::setLocked);
        put("visible", shopItemBuilder::setVisible);
        put("archived", shopItemBuilder::setArchived);
        put("one_time_purchase", shopItemBuilder::setOneTimePurchase);
        put("allow_discount_stacking", shopItemBuilder::setAllowDiscountStacking);
        put("use_display_itemstack", shopItemBuilder::setUseDisplayItemStack);
      }
    };
  }

  private ShopItem.Builder getBooleanValues(
      ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    Map<String, Boolean> keysToParse = // field_name, is_required
        new HashMap<>() {
          {
            put("locked", false);
            put("visible", false); // Default to true in builder if not present?
            put("archived", false);
            put("one_time_purchase", false);
            put("allow_discount_stacking", false);
            put("use_display_itemstack", false);
          }
        };
    Map<String, Consumer<Boolean>> consumers = getBooleanConsumers(shopItemBuilder);

    for (Map.Entry<String, Boolean> entry : keysToParse.entrySet()) {
      String key = entry.getKey();
      boolean isRequired = entry.getValue();
      if (config.contains(key)) {
        consumers.get(key).accept(config.getBoolean(key));
      } else if (isRequired) {
        missingValueMessage(key, config.getName(), config.getString("item_id"));
      }
    }
    return shopItemBuilder;
  }

  private ShopItem.Builder getDigitValues(
      ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    Map<String, Boolean> keysToParse = // field_name, is_required
        new HashMap<>() {
          {
            put("shop_slot", false);
            put("price", true);
            put("discount", false); // Changed from true, assuming discount can be optional (0)
          }
        };
    Map<String, Consumer<Integer>> consumers =
        new HashMap<>() {
          {
            put("shop_slot", shopItemBuilder::setShopSlot);
            put("price", shopItemBuilder::setPrice);
            put("discount", shopItemBuilder::setDiscount);
          }
        };

    for (Map.Entry<String, Boolean> entry : keysToParse.entrySet()) {
      String key = entry.getKey();
      boolean isRequired = entry.getValue();

      if (config.contains(key)) {
        // Contextual check for shop_slot based on parent shop's style
        if (key.equals("shop_slot")) {
          String shopId = config.getString("shop_id");
          if (shopId != null) {
            Optional<Shop> parentShopOpt = ShopManager.getInstance().getShop(shopId);
            if (parentShopOpt.isPresent() && parentShopOpt.get().getStyle() != Shop.Style.CUSTOM) {
              continue; // Skip setting shop_slot if shop style is not CUSTOM
            }
          }
        }
        consumers.get(key).accept(config.getInt(key));
      } else if (isRequired) {
        missingValueMessage(key, config.getName(), config.getString("item_id"));
      }
    }
    return shopItemBuilder;
  }

  private ShopItem.Builder getSingleStringValues(
      ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    Map<String, Boolean> keysToParse = // field_name, is_required
        new HashMap<>() {
          {
            put("item_id", true);
            put("shop_id", true);
            put("display_material", false); // Often has a default like STONE
            put("display_name", false);
            put("group", false);
            put("permission", false);
            put("display_itemstack", false); // Can be null if it's a command item
            put("purchased_item", false); // Can be null if it's a command item
            put("purchased_command", false); // Can be null if it's an item giving item
            put("purchased_permission", false);
            put("locked_reason", false);
          }
        };
    Map<String, Consumer<String>> consumers =
        new HashMap<>() {
          {
            put("item_id", shopItemBuilder::setItemID);
            put("shop_id", shopItemBuilder::setShopID);
            put("display_material", shopItemBuilder::setDisplayMaterial);
            put("display_name", shopItemBuilder::setDisplayName);
            put("group", shopItemBuilder::setGroup);
            put("permission", shopItemBuilder::setPermission);
            put("display_itemstack", shopItemBuilder::setDisplayItemStack);
            put("purchased_item", shopItemBuilder::setPurchasedItem);
            put("purchased_command", shopItemBuilder::setPurchasedCommand);
            put("purchased_permission", shopItemBuilder::setPurchasedPermission);
            put("locked_reason", shopItemBuilder::setLockedReason);
          }
        };

    for (Map.Entry<String, Boolean> entry : keysToParse.entrySet()) {
      String key = entry.getKey();
      boolean isRequired = entry.getValue();
      String value = config.getString(key);

      if (value == null
          || (isRequired && value.isBlank())) { // More precise check for required blank values
        if (isRequired) {
          missingValueMessage(key, config.getName(), config.getString("item_id"));
          // Consider if to `return shopItemBuilder;` here to stop processing a critically flawed
          // item
        }
        // For optional fields, if value is null/blank, we might skip or let builder handle defaults
        if (value == null || value.isBlank()) {
          continue;
        }
      }
      consumers.get(key).accept(value);
    }
    return shopItemBuilder;
  }

  private ShopItem toShopItem(ConfigurationSection config) {
    ShopItem.Builder shopItemBuilder = ShopItem.builder();

    // Populate builder using helper methods
    getSingleStringValues(config, shopItemBuilder);
    getBooleanValues(config, shopItemBuilder);
    getDigitValues(config, shopItemBuilder);

    // Handle list types like description
    if (config.isList("description")) {
      List<String> description = config.getStringList("description");
      if (!description.isEmpty()) { // Avoid setting an empty list if not intended
        shopItemBuilder.setDescription(description);
      }
    } else if (config.contains("description")) { // Handle if it's a single string (forgiving parse)
      String singleLineDescription = config.getString("description");
      if (singleLineDescription != null && !singleLineDescription.isBlank()) {
        shopItemBuilder.setDescription(List.of(singleLineDescription));
      }
    }
    return shopItemBuilder.build();
  }
}
