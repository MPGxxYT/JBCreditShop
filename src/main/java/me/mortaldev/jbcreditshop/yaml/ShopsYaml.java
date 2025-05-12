package me.mortaldev.jbcreditshop.yaml;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.mortaldev.YAML;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.records.Pair;

public class ShopsYaml {

  private static final String PATH = "/shops/";

  public void add(Shop shop) {}

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
      FileConfiguration config = entry.getKey();
      String path = entry.getValue();
      for (String string : config.getValues(false).keySet()) {
        ConfigurationSection section = config.getConfigurationSection(string);
        Shop shop = toShop(section);
        shop.setSource(new Pair<>(config, path));
        shop.setSection(section);
        shops.add(shop);
      }
    }
    Main.log("Loaded " + shops.size() + " shops");
    return shops;
  }

  public void create(Shop shop) {
    File dir = new File(Main.getInstance().getDataFolder() + PATH + "shops.yml");
    if (!dir.exists()) {
      dir.getParentFile().mkdirs();
      try {
        dir.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    FileConfiguration config = YAML.getInstance().getConfig(PATH + "shops.yml");
    shop.setSource(new Pair<>(config, PATH + "shops.yml"));
    Set<String> strings = config.getValues(false).keySet();
    int size = strings.size();
    ConfigurationSection section = config.createSection(size+"");
    shop.setSection(section);
    save(shop);
  }

  public void save(Shop shop) {
    File dir = new File(Main.getInstance().getDataFolder() + PATH + "shops.yml");
    if (!dir.exists()) {
      dir.getParentFile().mkdirs();
      try {
        dir.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    FileConfiguration config = YAML.getInstance().getConfig(PATH + "shops.yml");
    write(config.createSection(shop.getShopID()), shop);
  }

  private void write(ConfigurationSection config, Shop shop) {
    HashMap<String, Class<?>> defaultFormat = getDefaultFormat(shop);
    HashMap<String, Supplier<Boolean>> booleanSuppliers = getBooleanSuppliers(shop);
    HashMap<String, Supplier<Integer>> integerSuppliers = getIntegerSuppliers(shop);
    HashMap<String, Supplier<String>> stringSuppliers = getStringSuppliers(shop);
    for (Map.Entry<String, Class<?>> entry : defaultFormat.entrySet()) {
      String key = entry.getKey();
      Class<?> clazz = entry.getValue();
      if (clazz == Boolean.class) {
        config.set(key, booleanSuppliers.get(key).get());
      } else if (clazz == Integer.class) {
        config.set(key, integerSuppliers.get(key));
      } else if (clazz == Enum.class) {
        if (key.equals("style")) {
          config.set(key, shop.getStyle());
        }
      } else {
        if (key.isBlank()) {
          continue;
        }
        config.set(key, stringSuppliers.get(key));
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
    HashMap<String, Class<?>> hashMap = new HashMap<>() {
      {
        put("shop_id", String.class);
        put("shop_display", String.class);
        put("default_price", Integer.class);
        put("default_display_material", String.class);
        put("locked", Boolean.class);
        put("locked_bypass_permission", String.class);
        put("discount", Integer.class);
        put("discount_group", String.class);
        put("style", Enum.class);
        put("size", Integer.class);
      }
    };
    if (shop.getStyle() != Shop.Style.CUSTOM) {
      hashMap.remove("size");
    }
    return hashMap;
  }

  private Shop.Builder getBooleanValues(ConfigurationSection config, Shop.Builder builder) {
    // Value, Required
    HashMap<String, Boolean> keys =
        new HashMap<>() {
          {
            put("locked", false);
          }
        };
    HashMap<String, Consumer<Boolean>> consumer =
        new HashMap<>() {
          {
            put("locked", builder::setLocked);
          }
        };
    for (String key : keys.keySet()) {
      boolean value = config.getBoolean(key);
      consumer.get(key).accept(value);
    }
    return builder;
  }

  private Shop.Builder getDigitValues(ConfigurationSection config, Shop.Builder builder) {
    // Value, Required
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
      int value = config.getInt(key);
      consumer.get(key).accept(value);
    }
    return builder;
  }

  private Shop.Builder getSingleStringValues(ConfigurationSection config, Shop.Builder builder) {
    // Value, Required
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
            put("style", builder::setStyle);
          }
        };

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
