package me.mortaldev.jbcreditshop.yaml.core;

/**
 * Enum representing the supported field types for YAML persistence.
 *
 * <p>Each type defines how values are read from and written to YAML configuration sections.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * YamlField.<Shop, Shop.Builder>builder("shop_id", YamlFieldType.STRING)
 *     .required()
 *     .getter(Shop::getShopID)
 *     .setter((b, v) -> b.setShopID((String) v))
 *     .build();
 * }</pre>
 *
 * @see YamlField
 * @see AbstractYamlCRUD
 */
public enum YamlFieldType {
  /** String values - reads/writes as {@code config.getString(key)} */
  STRING,

  /** Integer values - reads/writes as {@code config.getInt(key)} */
  INTEGER,

  /** Boolean values - reads/writes as {@code config.getBoolean(key)} */
  BOOLEAN,

  /** List of strings - reads/writes as {@code config.getStringList(key)} */
  STRING_LIST,

  /** Enum values - stored as the enum's name string, parsed back via {@code Enum.valueOf()} */
  ENUM,

  /** Map of Integer keys to String values - stored as a nested YAML section */
  INT_STRING_MAP
}
