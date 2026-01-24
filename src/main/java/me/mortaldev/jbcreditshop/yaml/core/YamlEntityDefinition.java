package me.mortaldev.jbcreditshop.yaml.core;

import java.util.List;
import java.util.function.Supplier;

/**
 * Interface for defining YAML entity field mappings.
 *
 * <p>This interface defines the contract for YAML entity definitions, providing:
 * <ul>
 *   <li>Field definitions for reading/writing</li>
 *   <li>Builder creation and entity construction</li>
 *   <li>Entity identification for logging</li>
 * </ul>
 *
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class ShopsYaml extends AbstractYamlCRUD<Shop, Shop.Builder> {
 *
 *     private final List<YamlField<Shop, Shop.Builder>> fields = List.of(
 *         YamlField.<Shop, Shop.Builder>builder("shop_id", YamlFieldType.STRING)
 *             .required()
 *             .getter(Shop::getShopID)
 *             .setter((b, v) -> b.setShopID((String) v))
 *             .build()
 *         // ... more fields
 *     );
 *
 *     @Override
 *     public List<YamlField<Shop, Shop.Builder>> getFields() {
 *         return fields;
 *     }
 *
 *     @Override
 *     public Supplier<Shop.Builder> builderSupplier() {
 *         return Shop::builder;
 *     }
 *
 *     @Override
 *     public Shop build(Shop.Builder builder) {
 *         return builder.build();
 *     }
 *
 *     @Override
 *     public String getEntityName() {
 *         return "shop";
 *     }
 *
 *     @Override
 *     public String getIdentifier(Shop entity) {
 *         return entity.getShopID() != null ? entity.getShopID() : "N/A";
 *     }
 * }
 * }</pre>
 *
 * @param <T> The entity type (e.g., Shop, ShopItem) - must implement {@link YamlPersistable}
 * @param <B> The builder type (e.g., Shop.Builder, ShopItem.Builder)
 * @see AbstractYamlCRUD
 * @see YamlField
 */
public interface YamlEntityDefinition<T extends YamlPersistable, B> {

  /**
   * Returns the list of field definitions for this entity.
   *
   * <p>These fields define how the entity is read from and written to YAML.
   *
   * @return An immutable list of field definitions
   */
  List<YamlField<T, B>> getFields();

  /**
   * Returns a supplier that creates a new builder instance.
   *
   * <p>This is called when reading entities from YAML to create a fresh builder.
   *
   * @return A supplier that creates new builder instances
   */
  Supplier<B> builderSupplier();

  /**
   * Builds the entity from the populated builder.
   *
   * <p>Called after all fields have been set on the builder during YAML reading.
   *
   * @param builder The builder populated with field values
   * @return The constructed entity
   */
  T build(B builder);

  /**
   * Returns the entity name for logging purposes.
   *
   * <p>Used in log messages like "Loaded 5 shop(s)" or "Missing REQUIRED field for shop".
   *
   * @return The human-readable entity name (e.g., "shop", "shop item")
   */
  String getEntityName();

  /**
   * Returns a unique identifier for the entity (for logging purposes).
   *
   * <p>Used in error messages to help identify which entity has an issue.
   *
   * @param entity The entity to identify
   * @return The identifier string (e.g., the shop ID), or "N/A" if not available
   */
  String getIdentifier(T entity);
}
