package me.mortaldev.jbcreditshop.yaml.core;

import me.mortaldev.jbcreditshop.records.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Interface for entities that can be persisted to YAML files.
 *
 * <p>Entities implementing this interface track their source file and configuration section,
 * allowing the YAML CRUD system to save changes back to the correct location.
 *
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class Shop implements YamlPersistable {
 *     private Pair<FileConfiguration, String> source;
 *     private ConfigurationSection section;
 *
 *     @Override
 *     public Pair<FileConfiguration, String> getSource() {
 *         return source;
 *     }
 *
 *     @Override
 *     public void setSource(Pair<FileConfiguration, String> source) {
 *         this.source = source;
 *     }
 *
 *     @Override
 *     public ConfigurationSection getSection() {
 *         return section;
 *     }
 *
 *     @Override
 *     public void setSection(ConfigurationSection section) {
 *         this.section = section;
 *     }
 * }
 * }</pre>
 *
 * @see AbstractYamlCRUD
 */
public interface YamlPersistable {

  /**
   * Gets the source file configuration and path for this entity.
   *
   * @return A pair containing the FileConfiguration and the relative path to the file,
   *         or null if the entity hasn't been loaded from or saved to a file yet
   */
  Pair<FileConfiguration, String> getSource();

  /**
   * Sets the source file configuration and path for this entity.
   *
   * @param source A pair containing the FileConfiguration and the relative path to the file
   */
  void setSource(Pair<FileConfiguration, String> source);

  /**
   * Gets the configuration section where this entity's data is stored.
   *
   * @return The ConfigurationSection for this entity, or null if not yet assigned
   */
  ConfigurationSection getSection();

  /**
   * Sets the configuration section where this entity's data is stored.
   *
   * @param section The ConfigurationSection for this entity
   */
  void setSection(ConfigurationSection section);
}
