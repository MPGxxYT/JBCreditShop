package me.mortaldev.jbcreditshop.yaml.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.mortaldev.YAML;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.records.Pair;
import me.mortaldev.jbcreditshop.yaml.YamlReader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abstract base class for YAML-based CRUD operations.
 *
 * <p>This class provides a complete implementation of Create, Read, Update (save), and Delete
 * operations for entities that implement {@link YamlPersistable}. Subclasses only need to:
 * <ul>
 *   <li>Define field mappings using {@link YamlField}</li>
 *   <li>Implement {@link YamlEntityDefinition} methods</li>
 *   <li>Provide path and filename configuration</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * public class ShopsYaml extends AbstractYamlCRUD<Shop, Shop.Builder> {
 *
 *     private static final String PATH = "/shops/";
 *     private static final String SHOPS_FILE_NAME = "shops.yml";
 *
 *     // Singleton pattern
 *     private static class Singleton {
 *         private static final ShopsYaml INSTANCE = new ShopsYaml();
 *     }
 *
 *     public static ShopsYaml getInstance() {
 *         return Singleton.INSTANCE;
 *     }
 *
 *     private final List<YamlField<Shop, Shop.Builder>> fields;
 *
 *     private ShopsYaml() {
 *         super();
 *         this.fields = List.of(
 *             YamlField.<Shop, Shop.Builder>builder("shop_id", YamlFieldType.STRING)
 *                 .required()
 *                 .getter(Shop::getShopID)
 *                 .setter((b, v) -> b.setShopID((String) v))
 *                 .build(),
 *             // ... more fields
 *         );
 *     }
 *
 *     @Override
 *     protected String getPath() { return PATH; }
 *
 *     @Override
 *     protected String getDefaultFileName() { return SHOPS_FILE_NAME; }
 *
 *     @Override
 *     public List<YamlField<Shop, Shop.Builder>> getFields() { return fields; }
 *
 *     @Override
 *     public Supplier<Shop.Builder> builderSupplier() { return Shop::builder; }
 *
 *     @Override
 *     public Shop build(Shop.Builder builder) { return builder.build(); }
 *
 *     @Override
 *     public String getEntityName() { return "shop"; }
 *
 *     @Override
 *     public String getIdentifier(Shop entity) {
 *         return entity.getShopID() != null ? entity.getShopID() : "N/A";
 *     }
 * }
 * }</pre>
 *
 * <h2>CRUD Operations:</h2>
 * <ul>
 *   <li>{@link #read()} - Load all entities from YAML files in the configured path</li>
 *   <li>{@link #create(YamlPersistable)} - Create a new entity in the default YAML file</li>
 *   <li>{@link #save(YamlPersistable)} - Save changes to an existing entity</li>
 *   <li>{@link #delete(YamlPersistable)} - Delete an entity from its YAML file</li>
 * </ul>
 *
 * <h2>Context-Dependent Fields:</h2>
 * <p>Override {@link #getFieldsForWrite(YamlPersistable)} or {@link #getFieldsForRead(ConfigurationSection)}
 * to filter fields based on context. For example, to exclude a field when the parent entity
 * has a certain property:
 * <pre>{@code
 * @Override
 * protected List<YamlField<ShopItem, ShopItem.Builder>> getFieldsForWrite(ShopItem entity) {
 *     Optional<Shop> parentShop = ShopManager.getInstance().getShop(entity.getShopID());
 *     if (parentShop.isPresent() && parentShop.get().getStyle() != Shop.Style.CUSTOM) {
 *         // Filter out shop_slot field for non-CUSTOM style shops
 *         return fields.stream()
 *             .filter(f -> !f.getKey().equals("shop_slot"))
 *             .toList();
 *     }
 *     return fields;
 * }
 * }</pre>
 *
 * @param <T> The entity type (e.g., Shop, ShopItem) - must implement {@link YamlPersistable}
 * @param <B> The builder type (e.g., Shop.Builder, ShopItem.Builder)
 * @see YamlPersistable
 * @see YamlEntityDefinition
 * @see YamlField
 */
public abstract class AbstractYamlCRUD<T extends YamlPersistable, B>
    implements YamlEntityDefinition<T, B> {

  protected final YAML yaml;
  protected final JavaPlugin main;

  protected AbstractYamlCRUD() {
    this.yaml = YAML.getInstance();
    this.main = Main.getInstance();
  }

  /**
   * Returns the path to the directory containing YAML files.
   *
   * <p>The path should start and end with a forward slash (e.g., "/shops/").
   *
   * @return The directory path relative to the plugin's data folder
   */
  protected abstract String getPath();

  /**
   * Returns the default file name for this entity type.
   *
   * <p>This file is used when creating new entities (e.g., "shops.yml").
   *
   * @return The default YAML file name
   */
  protected abstract String getDefaultFileName();

  /**
   * Override to provide context-dependent field filtering for writes.
   *
   * <p>This method is called before writing an entity to YAML. Override it to exclude
   * certain fields based on the entity's state or related entities.
   *
   * @param entity The entity being written
   * @return The list of fields to write (defaults to all fields)
   */
  protected List<YamlField<T, B>> getFieldsForWrite(T entity) {
    return getFields();
  }

  /**
   * Override to provide context-dependent field filtering for reads.
   *
   * <p>This method is called before reading an entity from YAML. Override it to exclude
   * certain fields based on the section's content.
   *
   * @param section The configuration section being read
   * @return The list of fields to read (defaults to all fields)
   */
  protected List<YamlField<T, B>> getFieldsForRead(ConfigurationSection section) {
    return getFields();
  }

  /**
   * Logs a warning message for a missing required field.
   *
   * @param key The missing field key
   * @param sectionName The section name (for identification)
   */
  protected void missingValueMessage(String key, String sectionName) {
    Main.warn("Missing REQUIRED " + key + " for " + getEntityName() + " " + sectionName);
  }

  /**
   * Reads all entities from YAML files in the configured path.
   *
   * <p>This method:
   * <ol>
   *   <li>Loads all YAML files from {@link #getPath()}</li>
   *   <li>Iterates through each top-level section</li>
   *   <li>Converts each section to an entity using field definitions</li>
   *   <li>Sets the source file and section on each entity</li>
   * </ol>
   *
   * @return A set of all loaded entities
   */
  public Set<T> read() {
    Set<T> entities = new HashSet<>();
    HashMap<FileConfiguration, String> configs = YamlReader.getInstance().getConfigs(getPath());

    for (Map.Entry<FileConfiguration, String> entry : configs.entrySet()) {
      FileConfiguration config = entry.getKey();
      String filePath = entry.getValue();

      for (String key : config.getValues(false).keySet()) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section != null) {
          T entity = toEntity(section);
          entity.setSource(new Pair<>(config, filePath));
          entity.setSection(section);
          entities.add(entity);
        }
      }
    }
    Main.info("Loaded " + entities.size() + " " + getEntityName() + "(s)");
    return entities;
  }

  /**
   * Creates a new entity in the default YAML file.
   *
   * <p>This method:
   * <ol>
   *   <li>Creates the file if it doesn't exist</li>
   *   <li>Assigns a new numeric section key</li>
   *   <li>Sets the source and section on the entity</li>
   *   <li>Saves the entity to disk</li>
   * </ol>
   *
   * @param entity The entity to create
   * @throws RuntimeException if the file cannot be created
   */
  public void create(T entity) {
    String filePath = getPath() + getDefaultFileName();
    File file = new File(Main.getInstance().getDataFolder(), filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(
            "Failed to create " + getEntityName() + " file: " + file.getAbsolutePath(), e);
      }
    }

    FileConfiguration config = yaml.getConfig(main, filePath);
    entity.setSource(new Pair<>(config, filePath));

    String newSectionKey = String.valueOf(findNextAvailableNumericKey(config));
    ConfigurationSection section = config.createSection(newSectionKey);
    entity.setSection(section);

    save(entity);
  }

  /**
   * Saves changes to an existing entity.
   *
   * <p>The entity must have a valid source and section set (either from {@link #read()}
   * or {@link #create(YamlPersistable)}).
   *
   * @param entity The entity to save
   */
  public void save(T entity) {
    if (entity.getSource() == null
        || entity.getSource().first() == null
        || entity.getSource().second() == null) {
      Main.severe(
          getEntityName()
              + " (ID: "
              + getIdentifier(entity)
              + ") is missing source FileConfiguration or path. Cannot save.");
      return;
    }

    FileConfiguration config = entity.getSource().first();
    String configPath = entity.getSource().second();

    File file = new File(Main.getInstance().getDataFolder(), configPath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(
            "Failed to create " + getEntityName() + " file during save: " + file.getAbsolutePath(),
            e);
      }
    }

    ConfigurationSection section = entity.getSection();
    if (section == null) {
      Main.severe(
          getEntityName()
              + " section is null for ID: "
              + getIdentifier(entity)
              + " during save. Creating new section as fallback.");
      String newSectionKey = String.valueOf(findNextAvailableNumericKey(config));
      section = config.createSection(newSectionKey);
      entity.setSection(section);
    }

    write(section, entity);
    yaml.saveConfig(main, config, configPath);
  }

  /**
   * Deletes an entity from its YAML file.
   *
   * <p>The entity must have a valid source and section set.
   *
   * @param entity The entity to delete
   * @throws NullPointerException if the section's current path is null
   */
  public void delete(T entity) {
    if (entity.getSource() == null
        || entity.getSource().first() == null
        || entity.getSource().second() == null) {
      Main.severe(
          getEntityName()
              + " (ID: "
              + getIdentifier(entity)
              + ") is missing source FileConfiguration or path. Cannot delete.");
      return;
    }

    FileConfiguration config = entity.getSource().first();
    String configPath = entity.getSource().second();

    File file = new File(Main.getInstance().getDataFolder(), configPath);
    if (!file.exists()) {
      Main.severe(
          getEntityName() + " (ID: " + getIdentifier(entity) + ") does not exist. Cannot delete.");
      return;
    }

    String currentPath = entity.getSection().getCurrentPath();
    if (currentPath == null) {
      throw new NullPointerException(
          "FAILED TO DELETE " + getEntityName() + "! 'currentPath' is NULL");
    }
    config.set(currentPath, null);
    yaml.saveConfig(main, config, configPath);
  }

  /**
   * Finds the next available numeric key for a new section.
   *
   * <p>Iterates through existing keys, finds the maximum numeric key, and returns max + 1.
   *
   * @param config The configuration to search
   * @return The next available numeric key (starts at 0 if no numeric keys exist)
   */
  protected int findNextAvailableNumericKey(FileConfiguration config) {
    int maxKey = -1;
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
        // Ignore non-numeric keys
      }
    }
    return maxKey + 1;
  }

  /**
   * Converts a configuration section to an entity using field definitions.
   *
   * @param section The section to read from
   * @return The constructed entity
   */
  protected T toEntity(ConfigurationSection section) {
    B builder = builderSupplier().get();

    for (YamlField<T, B> field : getFieldsForRead(section)) {
      String key = field.getKey();
      if (!section.contains(key)) {
        if (field.isRequired()) {
          missingValueMessage(key, section.getName());
        }
        continue;
      }

      Object value = readValue(section, field);
      if (value != null) {
        field.setValue(builder, value);
      } else if (field.isRequired()) {
        missingValueMessage(key, section.getName());
      }
    }

    return build(builder);
  }

  /**
   * Reads a value from a configuration section based on the field type.
   *
   * @param section The section to read from
   * @param field The field definition
   * @return The read value, or null if not present or invalid
   */
  @SuppressWarnings("unchecked")
  protected Object readValue(ConfigurationSection section, YamlField<T, B> field) {
    String key = field.getKey();
    switch (field.getType()) {
      case STRING:
        String strValue = section.getString(key);
        if (strValue != null && strValue.isBlank() && field.isRequired()) {
          return null;
        }
        return strValue;

      case INTEGER:
        return section.getInt(key);

      case BOOLEAN:
        return section.getBoolean(key);

      case STRING_LIST:
        if (section.isList(key)) {
          List<String> list = section.getStringList(key);
          return list.isEmpty() ? null : list;
        } else if (section.contains(key)) {
          String singleValue = section.getString(key);
          if (singleValue != null && !singleValue.isBlank()) {
            return List.of(singleValue);
          }
        }
        return null;

      case ENUM:
        String enumStr = section.getString(key);
        if (enumStr == null || enumStr.isBlank()) {
          return null;
        }
        if (field.getEnumClass() != null) {
          try {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getEnumClass();
            return Enum.valueOf(enumClass, enumStr.toUpperCase());
          } catch (IllegalArgumentException e) {
            Main.warn(
                "Invalid " + key + " value '" + enumStr + "' in section " + section.getName());
            return null;
          }
        }
        return enumStr;

      case INT_STRING_MAP:
        ConfigurationSection mapSection = section.getConfigurationSection(key);
        if (mapSection == null) {
          return null;
        }
        HashMap<Integer, String> map = new HashMap<>();
        for (String mapKey : mapSection.getKeys(false)) {
          try {
            int intKey = Integer.parseInt(mapKey);
            String value = mapSection.getString(mapKey);
            map.put(intKey, value);
          } catch (NumberFormatException e) {
            Main.warn("Invalid integer key '" + mapKey + "' in " + key + " map");
          }
        }
        return map.isEmpty() ? null : map;

      default:
        return null;
    }
  }

  /**
   * Writes an entity to a configuration section using field definitions.
   *
   * @param section The section to write to
   * @param entity The entity to write
   */
  protected void write(ConfigurationSection section, T entity) {
    for (YamlField<T, B> field : getFieldsForWrite(entity)) {
      if (!field.shouldWrite(entity)) {
        continue;
      }

      String key = field.getKey();
      Object value = field.getValue(entity);

      if (value == null) {
        continue;
      }

      writeValue(section, key, value, field.getType());
    }
  }

  /**
   * Writes a value to a configuration section based on the field type.
   *
   * @param section The section to write to
   * @param key The key to write
   * @param value The value to write
   * @param type The field type
   */
  @SuppressWarnings("unchecked")
  protected void writeValue(
      ConfigurationSection section, String key, Object value, YamlFieldType type) {
    switch (type) {
      case STRING:
        if (value instanceof String strValue && !strValue.isBlank()) {
          section.set(key, strValue);
        }
        break;

      case INTEGER:
        if (value instanceof Integer intValue) {
          section.set(key, intValue);
        }
        break;

      case BOOLEAN:
        if (value instanceof Boolean boolValue) {
          section.set(key, boolValue);
        }
        break;

      case STRING_LIST:
        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
          section.set(key, listValue);
        }
        break;

      case ENUM:
        if (value instanceof Enum<?> enumValue) {
          section.set(key, enumValue.name());
        } else if (value instanceof String strValue && !strValue.isBlank()) {
          section.set(key, strValue);
        }
        break;

      case INT_STRING_MAP:
        if (value instanceof Map<?, ?> mapValue && !mapValue.isEmpty()) {
          section.set(key, mapValue);
        }
        break;
    }
  }
}
