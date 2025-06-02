package me.mortaldev.jbcreditshop.utils;

import me.mortaldev.jbcreditshop.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class NBTAPI {

  private static final Main MAIN_INSTANCE = Main.getInstance();

  public static @Nullable String getNBT(@NotNull ItemStack item, String key) {
    Objects.requireNonNull(MAIN_INSTANCE, "JeffLib hasn't been initialized.");
    Objects.requireNonNull(item, "item must not be null");
    if (!item.hasItemMeta()) return null;
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    NamespacedKey namespacedKey = new NamespacedKey(MAIN_INSTANCE, key);
    if (pdc.has(namespacedKey, PersistentDataType.STRING)) {
      return pdc.get(namespacedKey, PersistentDataType.STRING);
    }
    return null;
  }

  public static void addNBT(@NotNull ItemStack item, String key, String value) {
    Objects.requireNonNull(MAIN_INSTANCE, "JeffLib hasn't been initialized.");
    Objects.requireNonNull(item, "item must not be null");
    ItemMeta meta =
        item.hasItemMeta()
            ? item.getItemMeta()
            : Bukkit.getItemFactory().getItemMeta(item.getType());
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    NamespacedKey namespacedKey = new NamespacedKey(MAIN_INSTANCE, key);
    pdc.set(namespacedKey, PersistentDataType.STRING, value);
    item.setItemMeta(meta);
  }

  public static boolean hasNBT(@NotNull ItemStack item, String key) {
    Objects.requireNonNull(MAIN_INSTANCE, "JeffLib hasn't been initialized.");
    Objects.requireNonNull(item, "item must not be null");
    if (!item.hasItemMeta()) return false;
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    return pdc.has(new NamespacedKey(MAIN_INSTANCE, key), PersistentDataType.STRING);
  }

  public static void removeNBT(@NotNull ItemStack item, String key) {
    Objects.requireNonNull(MAIN_INSTANCE, "JeffLib hasn't been initialized.");
    Objects.requireNonNull(item, "item must not be null");
    if (!item.hasItemMeta()) return;
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.remove(new NamespacedKey(MAIN_INSTANCE, key));
    item.setItemMeta(meta);
  }
}