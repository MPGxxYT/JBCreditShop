package me.mortaldev.jbcreditshop.yaml.core;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Field descriptor that serves as a single source of truth for YAML field definitions.
 *
 * <p>This class eliminates the need to define field mappings in multiple places (read methods,
 * write methods, format definitions). Each field is defined once with all its metadata.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Define a required string field
 * YamlField.<Shop, Shop.Builder>builder("shop_id", YamlFieldType.STRING)
 *     .required()
 *     .getter(Shop::getShopID)
 *     .setter((b, v) -> b.setShopID((String) v))
 *     .build();
 *
 * // Define an optional integer field
 * YamlField.<Shop, Shop.Builder>builder("discount", YamlFieldType.INTEGER)
 *     .getter(Shop::getDiscount)
 *     .setter((b, v) -> b.setDiscount((Integer) v))
 *     .build();
 *
 * // Define an enum field
 * YamlField.<Shop, Shop.Builder>builder("style", YamlFieldType.ENUM)
 *     .required()
 *     .enumClass(Shop.Style.class)
 *     .getter(Shop::getStyle)
 *     .setter((b, v) -> b.setStyleAsEnum((Shop.Style) v))
 *     .build();
 *
 * // Define a conditional field (only written when condition is met)
 * YamlField.<Shop, Shop.Builder>builder("size", YamlFieldType.INTEGER)
 *     .getter(Shop::getSize)
 *     .setter((b, v) -> b.setSize((Integer) v))
 *     .condition(shop -> shop.getStyle() == Shop.Style.CUSTOM)
 *     .build();
 * }</pre>
 *
 * <h2>Field Components:</h2>
 * <ul>
 *   <li><b>key</b> - The YAML key name (e.g., "shop_id", "default_price")</li>
 *   <li><b>type</b> - The {@link YamlFieldType} for read/write handling</li>
 *   <li><b>required</b> - Whether the field is required (logs warning if missing)</li>
 *   <li><b>getter</b> - Function to extract value from entity for writing</li>
 *   <li><b>setter</b> - BiConsumer to set value on builder when reading</li>
 *   <li><b>condition</b> - Predicate to determine if field should be written</li>
 *   <li><b>enumClass</b> - The enum class for ENUM type fields</li>
 * </ul>
 *
 * @param <T> The entity type (e.g., Shop, ShopItem)
 * @param <B> The builder type (e.g., Shop.Builder, ShopItem.Builder)
 * @see YamlFieldType
 * @see AbstractYamlCRUD
 */
public class YamlField<T, B> {

  private final String key;
  private final YamlFieldType type;
  private final boolean required;
  private final Function<T, Object> getter;
  private final BiConsumer<B, Object> setter;
  private final Predicate<T> condition;
  private final Class<?> enumClass;

  private YamlField(Builder<T, B> builder) {
    this.key = builder.key;
    this.type = builder.type;
    this.required = builder.required;
    this.getter = builder.getter;
    this.setter = builder.setter;
    this.condition = builder.condition;
    this.enumClass = builder.enumClass;
  }

  /** Returns the YAML key name for this field. */
  public String getKey() {
    return key;
  }

  /** Returns the field type for read/write handling. */
  public YamlFieldType getType() {
    return type;
  }

  /** Returns true if this field is required (will log warning if missing). */
  public boolean isRequired() {
    return required;
  }

  /** Returns the getter function for extracting value from entity. */
  public Function<T, Object> getGetter() {
    return getter;
  }

  /** Returns the setter consumer for setting value on builder. */
  public BiConsumer<B, Object> getSetter() {
    return setter;
  }

  /** Returns the condition predicate for conditional writes, or null if always written. */
  public Predicate<T> getCondition() {
    return condition;
  }

  /** Returns the enum class for ENUM type fields, or null for other types. */
  public Class<?> getEnumClass() {
    return enumClass;
  }

  /**
   * Checks if this field should be written for the given entity.
   *
   * @param entity The entity to check
   * @return true if the field should be written (condition is null or passes), false otherwise
   */
  public boolean shouldWrite(T entity) {
    return condition == null || condition.test(entity);
  }

  /**
   * Gets the value from the entity using the getter.
   *
   * @param entity The entity to get the value from
   * @return The field value, or null if getter is null
   */
  public Object getValue(T entity) {
    if (getter == null) {
      return null;
    }
    return getter.apply(entity);
  }

  /**
   * Sets the value on the builder using the setter.
   *
   * @param builder The builder to set the value on
   * @param value The value to set (ignored if null)
   */
  public void setValue(B builder, Object value) {
    if (setter != null && value != null) {
      setter.accept(builder, value);
    }
  }

  /**
   * Creates a new builder for constructing a YamlField.
   *
   * @param key The YAML key name
   * @param type The field type
   * @param <T> The entity type
   * @param <B> The builder type
   * @return A new Builder instance
   */
  public static <T, B> Builder<T, B> builder(String key, YamlFieldType type) {
    return new Builder<>(key, type);
  }

  /**
   * Builder for constructing YamlField instances.
   *
   * @param <T> The entity type
   * @param <B> The builder type
   */
  public static class Builder<T, B> {
    private final String key;
    private final YamlFieldType type;
    private boolean required = false;
    private Function<T, Object> getter;
    private BiConsumer<B, Object> setter;
    private Predicate<T> condition;
    private Class<?> enumClass;

    public Builder(String key, YamlFieldType type) {
      this.key = key;
      this.type = type;
    }

    /**
     * Marks this field as required. Missing required fields will log a warning.
     *
     * @return This builder for chaining
     */
    public Builder<T, B> required() {
      this.required = true;
      return this;
    }

    /**
     * Sets the getter function for extracting the field value from an entity.
     *
     * @param getter Function that takes an entity and returns the field value
     * @return This builder for chaining
     */
    public Builder<T, B> getter(Function<T, Object> getter) {
      this.getter = getter;
      return this;
    }

    /**
     * Sets the setter consumer for setting the field value on a builder.
     *
     * @param setter BiConsumer that takes a builder and value to set
     * @return This builder for chaining
     */
    public Builder<T, B> setter(BiConsumer<B, Object> setter) {
      this.setter = setter;
      return this;
    }

    /**
     * Sets a condition predicate for conditional field writes.
     *
     * <p>The field will only be written to YAML if the condition returns true.
     * This is useful for fields that only apply in certain contexts.
     *
     * @param condition Predicate that takes an entity and returns true if field should be written
     * @return This builder for chaining
     */
    public Builder<T, B> condition(Predicate<T> condition) {
      this.condition = condition;
      return this;
    }

    /**
     * Sets the enum class for ENUM type fields.
     *
     * <p>Required for YamlFieldType.ENUM to properly parse the enum value.
     *
     * @param enumClass The enum class
     * @return This builder for chaining
     */
    public Builder<T, B> enumClass(Class<?> enumClass) {
      this.enumClass = enumClass;
      return this;
    }

    /**
     * Builds the YamlField instance.
     *
     * @return The constructed YamlField
     */
    public YamlField<T, B> build() {
      return new YamlField<>(this);
    }
  }
}
