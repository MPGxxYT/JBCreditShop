package org.example.jbcreditshop.yaml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.example.jbcreditshop.Main;
import org.example.jbcreditshop.modules.ShopItem;

public class ShopItemsYaml {

  private static final String PATH = "/shopitems/";

  public void add(ShopItem shopItem) {

  }

  private static class Singleton {
    private static final ShopItemsYaml INSTANCE = new ShopItemsYaml();
  }

  public static synchronized ShopItemsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopItemsYaml() {}

  public Set<ShopItem> read() {
    Set<ShopItem> shopItems = new HashSet<>();
    Set<FileConfiguration> fileConfig = YamlReader.getInstance().getConfigs(PATH);
    for (FileConfiguration config : fileConfig) {
      for (String string : config.getValues(false).keySet()) {
        ShopItem shopItem = toShopItem(config.getConfigurationSection(string));
        shopItems.add(shopItem);
      }
    }
    Main.log("Loaded " + shopItems.size() + " shopitems");
    return shopItems;
  }

  private void missingValueMessage(String key, String configName) {
    Main.warn("Missing REQUIRED " + key + " for shopitem " + configName);
  }

  private ShopItem.Builder getBooleanValues(ConfigurationSection config, ShopItem.Builder shopItemBuilder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("locked", false);
      put("visible", false);
      put("archived", false);
    }};
    HashMap<String, Consumer<Boolean>> consumer = new HashMap<>(){{
      put("locked", shopItemBuilder::setLocked);
      put("visible", shopItemBuilder::setVisible);
      put("archived", shopItemBuilder::setArchived);
    }};
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
    }};
    HashMap<String, Consumer<Integer>> consumer = new HashMap<>(){{
      put("shop_slot", shopItemBuilder::setShopSlot);
      put("price", shopItemBuilder::setPrice);
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
