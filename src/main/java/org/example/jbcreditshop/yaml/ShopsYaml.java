package org.example.jbcreditshop.yaml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.example.jbcreditshop.Main;
import org.example.jbcreditshop.modules.Shop;

public class ShopsYaml {


  private static final String PATH = "/shops/";

  public void add(Shop shop) {

  }

  private static class Singleton {
    private static final ShopsYaml INSTANCE = new ShopsYaml();
  }

  public static synchronized ShopsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopsYaml() {}
  public Set<Shop> read() {
    Set<Shop> shops = new HashSet<>();
    Set<FileConfiguration> fileConfig = YamlReader.getInstance().getConfigs(PATH);
    for (FileConfiguration config : fileConfig) {
      for (String string : config.getValues(false).keySet()) {
        Shop shop = toShop(config.getConfigurationSection(string));
        shops.add(shop);
      }
    }
    Main.log("Loaded " + shops.size() + " shops");
    return shops;
  }

  private void missingValueMessage(String key, String configName) {
    Main.warn("Missing REQUIRED " + key + " for shop " + configName);
  }

  private Shop.Builder getBooleanValues(ConfigurationSection config, Shop.Builder builder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("locked", false);
    }};
    HashMap<String, Consumer<Boolean>> consumer = new HashMap<>(){{
      put("locked", builder::setLocked);
    }};
    for (String key : keys.keySet()) {
      boolean value = config.getBoolean(key);
      consumer.get(key).accept(value);
    }
    return builder;
  }


  private Shop.Builder getDigitValues(ConfigurationSection config, Shop.Builder builder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("default_price", true);
      put("discount", false);
      put("size", false);
    }};
    HashMap<String, Consumer<Integer>> consumer = new HashMap<>(){{
      put("default_price", builder::setDefaultPrice);
      put("discount", builder::setDiscount);
      put("size", builder::setSize);
    }};
    for (String key : keys.keySet()) {
      int value = config.getInt(key);
      consumer.get(key).accept(value);
    }
    return builder;
  }

  private Shop.Builder getSingleStringValues(ConfigurationSection config, Shop.Builder builder) {
    // Value, Required
    HashMap<String, Boolean> keys = new HashMap<>(){{
      put("shop_id", true);
      put("shop_display", false);
      put("default_display_material", true);
      put("locked_bypass_permission", false);
      put("discount_group", false);
      put("style", true);
    }};
    HashMap<String, Consumer<String>> consumer = new HashMap<>(){{
      put("shop_id", builder::setShopID);
      put("shop_display", builder::setShopDisplay);
      put("default_display_material", builder::setDefaultDisplayMaterial);
      put("locked_bypass_permission", builder::setLockedBypassPermission);
      put("discount_group", builder::setDiscountGroup);
      put("style", builder::setStyle);
    }};


    for (String key : keys.keySet()) {
      String value = config.getString(key);
      if (value == null || value.isBlank()) {
        if (keys.get(key)) {
          missingValueMessage(key, config.getName());
          return builder;
        }
        continue;
      }
      consumer.get(key).accept(value);
    }
    return builder;
  }

  private Shop toShop(ConfigurationSection config) {
    Shop.Builder builder = Shop.builder();
    getSingleStringValues(config, builder);
    getBooleanValues(config, builder);
    getDigitValues(config, builder);
    return builder.build();
  }
}
