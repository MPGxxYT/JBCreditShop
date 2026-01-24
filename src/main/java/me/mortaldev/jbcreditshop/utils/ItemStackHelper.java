package me.mortaldev.jbcreditshop.utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackHelper {

  public record NameHolder(String stringName, Component componentName) {
    public boolean hasComponent() {
      return componentName != null;
    }

    public Component asComponent() {
      if (hasComponent()) {
        return componentName;
      } else {
        return Component.text(stringName);
      }
    }

    public String asString() {
      if (hasComponent()) {
        return TextUtil.deformat(componentName);
      } else {
        return stringName;
      }
    }
  }

  private static final Set<Character> VOWELS = Set.of('a', 'e', 'i', 'o', 'u');

  /**
   * Converts the given ItemStack into a grammatically correct item name string.
   *
   * @param itemStack The ItemStack to convert.
   * @return The grammatically correct item name string.
   */
  public static String grammarItem(@NotNull ItemStack itemStack) {
    String name = itemName(itemStack);
    if (name.isEmpty()) {
      return "an Item";
    }

    if (itemStack.getAmount() > 1) {
      return itemStack.getAmount() + " " + pluralize(name);
    }

    String lowerName = name.toLowerCase();
    char lastChar = lowerName.charAt(lowerName.length() - 1);

    // Items ending in 's' don't need article modification
    if (lastChar == 's') {
      return name;
    }

    char firstChar = lowerName.charAt(0);
    return (VOWELS.contains(firstChar) ? "an " : "a ") + name;
  }

  /**
   * Converts a singular item name to its plural form.
   *
   * @param name The singular name to pluralize.
   * @return The pluralized name.
   */
  private static String pluralize(@NotNull String name) {
    String lowerName = name.toLowerCase();
    char lastChar = lowerName.charAt(lowerName.length() - 1);

    if (lastChar == 's'
        || lastChar == 'x'
        || lastChar == 'z'
        || lowerName.endsWith("sh")
        || lowerName.endsWith("ch")) {
      return name + "es";
    }
    return name + "s";
  }

  /**
   * Retrieves the name of an ItemStack, including both its formatted string name and its Component
   * display name.
   *
   * @param item The ItemStack to retrieve the name from.
   * @return A NameHolder record containing the string name and Component display name (if present).
   */
  public static NameHolder getName(@NotNull ItemStack item) {
    if (item.hasItemMeta()) {
      Component displayName = item.getItemMeta().displayName();
      if (item.getItemMeta().hasDisplayName() && displayName != null) {
        return new NameHolder(itemName(item), displayName);
      } else {
        return new NameHolder(itemName(item), null);
      }
    } else {
      return new NameHolder(itemName(item), null);
    }
  }

  /**
   * Converts the given ItemStack into a formatted item name string.
   *
   * @param itemStack The ItemStack to convert.
   * @return The formatted item name string.
   */
  public static String itemName(@NotNull ItemStack itemStack) {
    String key = itemStack.getType().getKey().getKey();
    String[] parts = key.split("_");

    if (parts.length == 0) {
      return "";
    }

    if (parts.length == 1) {
      return capitalize(parts[0]);
    }

    StringBuilder result = new StringBuilder(key.length() + parts.length);
    for (int i = 0; i < parts.length; i++) {
      if (!parts[i].isEmpty()) {
        if (i > 0) {
          result.append(' ');
        }
        result.append(capitalize(parts[i]));
      }
    }
    return result.toString();
  }

  /**
   * Capitalizes the first character of a string.
   *
   * @param str The string to capitalize.
   * @return The capitalized string.
   */
  private static String capitalize(@NotNull String str) {
    if (str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  /**
   * Serializes an ItemStack object into a Base64 encoded string.
   *
   * @param itemStack The ItemStack object to be serialized.
   * @return A Base64 encoded string representation of the serialized ItemStack object.
   */
  public static String serialize(@NotNull ItemStack itemStack) {
    byte[] itemStackAsBytes = itemStack.serializeAsBytes();
    return Base64.getEncoder().encodeToString(itemStackAsBytes);
  }

  /**
   * Deserializes a Base64 encoded string into an ItemStack object.
   *
   * @param string The Base64 encoded string to be deserialized.
   * @return The deserialized ItemStack object.
   */
  public static ItemStack deserialize(@NotNull String string) {
    byte[] bytes = Base64.getDecoder().decode(string);
    return ItemStack.deserializeBytes(bytes);
  }

  /**
   * Converts an ItemStack object to a String Notation Base64 (SNBT) string.
   *
   * <p>If {@code asHover} is true, the returned string will be in the format of "{@code
   * material}:{@code amount}:{@code itemMeta}".
   *
   * <p>If {@code asHover} is false, the returned string will be the result of calling {@link
   * ItemStack#getItemMeta()}{@code .getAsString()}.
   *
   * <p>Usage in hover:
   *
   * <p>"##itm:"+toSNBT(itemStack, true)
   *
   * @param itemStack The ItemStack object to be converted.
   * @param asHover Whether to format the returned string for a Hover ShowItem Event.
   * @return The converted SNBT string.
   */
  public static String toSNBT(@NotNull ItemStack itemStack, boolean asHover) {
    StringBuilder sb = new StringBuilder();
    if (asHover) {
      sb.append(itemStack.getType().getKey().getKey());
      sb.append(":").append(itemStack.getAmount());
      if (itemStack.hasItemMeta()) {
        String meta = itemStack.getItemMeta().getAsString().replace("\"", "\\\"");
        sb.append(":\"").append(meta).append("\"");
      }
    } else {
      if (itemStack.hasItemMeta()) {
        sb.append(itemStack.getItemMeta().getAsString());
      }
    }
    return sb.toString();
  }

  /**
   * Returns a new instance of the Builder class with the given ItemStack.
   *
   * @param itemStack The ItemStack to be used in the Builder.
   * @return A new instance of the Builder class.
   */
  public static Builder builder(ItemStack itemStack) {
    return new Builder(itemStack);
  }

  /**
   * Returns a new instance of the Builder class with the given Material.
   *
   * @param material The Material to be used in the Builder.
   * @return A new instance of the Builder class.
   */
  public static Builder builder(Material material) {
    return new Builder(material);
  }

  /** Class used for building ItemStack objects with customized lore and attributes. */
  public static class Builder {

    ItemStack itemStack;

    private Builder(ItemStack itemStack) {
      this.itemStack = itemStack;
    }

    private Builder(Material material) {
      this.itemStack = new ItemStack(material);
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The list of Components to set as the lore.
     * @return The builder instance.
     */
    public Builder lore(List<Component> lore) {
      ItemStack cloneItemStack = itemStack.clone();
      cloneItemStack.editMeta(itemMeta -> itemMeta.lore(lore));

      this.itemStack = cloneItemStack;
      return this;
    }

    /**
     * Sets the item flags of the item.
     *
     * @param itemFlags The array of item flags to set.
     * @return The builder instance.
     */
    public Builder itemFlags(ItemFlag... itemFlags) {
      Set<ItemFlag> currentItemFlags = itemStack.getItemFlags();
      for (ItemFlag flag : currentItemFlags) {
        removeItemFlag(flag);
      }
      addItemFlag(itemFlags);
      return this;
    }

    /**
     * Adds item flags to the item.
     *
     * @param itemFlags The array of item flags to add.
     * @return The builder instance.
     */
    public Builder addItemFlag(ItemFlag... itemFlags) {
      itemStack.editMeta(itemMeta -> itemMeta.addItemFlags(itemFlags));
      return this;
    }

    /**
     * Removes the specified item flags from the item.
     *
     * @param itemFlags The item flags to remove from the item.
     * @return The updated builder instance.
     */
    public Builder removeItemFlag(ItemFlag... itemFlags) {
      itemStack.editMeta(itemMeta -> itemMeta.removeItemFlags(itemFlags));
      return this;
    }

    /**
     * Adds a lore to the item. Empty will add blank lore.
     *
     * @return The builder instance.
     */
    public Builder addLore() {
      return addLore("");
    }

    /**
     * Adds a lore to the item. Empty will add blank lore.
     *
     * @param components The Components to add as the lore.
     * @return The builder instance.
     */
    public Builder addLore(Component... components) {
      for (Component component : components) {
        addLore(component);
      }
      return this;
    }

    public Builder addLore(String... strings) {
      for (String string : strings) {
        addLore(TextUtil.format(string));
      }
      return this;
    }

    public Builder addLore(List<String> strings) {
      for (String string : strings) {
        addLore(TextUtil.format(string));
      }
      return this;
    }

    /**
     * Adds a lore to the item. Empty will add blank lore.
     *
     * @param lore The Component to add as the lore.
     * @return The builder instance.
     */
    public Builder addLore(Component lore) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        loreList = new ArrayList<>();
      }
      loreList.add(lore);
      this.itemStack.lore(loreList);
      return this;
    }

    public Builder setLore(int index, String lore) {
      return setLore(index, TextUtil.format(lore));
    }

    public Builder setLore(int index, Component lore) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      if (loreList.size() < index + 1) {
        return this;
      }
      loreList.set(index, lore);
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Inserts a lore component at the specified index in the item's lore list.
     *
     * @param lore The Component to insert as the lore.
     * @param index The index at which to insert the lore component.
     * @return The builder instance.
     */
    public Builder insertLore(Component lore, int index) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      if (loreList.size() < index + 1) {
        return this;
      }
      loreList.add(index, lore);
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Sets the lore of the item at specified indexes.
     *
     * @param lore The Component to set as the lore.
     * @param indexes The indexes of the lore items to be set.
     * @return The builder instance.
     */
    public Builder setLore(Component lore, int... indexes) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      for (int i : indexes) {
        if (loreList.size() < i + 1) {
          continue;
        }
        loreList.set(i, lore);
      }
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Removes the specified lore from the item.
     *
     * @param lore The Component to remove from the item's lore.
     * @return The updated builder instance.
     */
    public Builder removeLore(String lore) {
      return removeLore(TextUtil.format(lore));
    }

    /**
     * Removes the specified Component from the item's lore.
     *
     * @param lore The Component to remove from the item's lore.
     * @return The updated builder instance.
     */
    public Builder removeLore(Component lore) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      loreList.remove(lore);
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Removes the lore at the specified index from the item's lore list.
     *
     * @param index The index of the lore to remove.
     * @return The updated builder instance.
     */
    public Builder removeLore(int index) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      loreList.remove(index);
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Replaces all occurrences of specified lore components with a replacement component in the
     * item's lore.
     *
     * @param replacement The Component to replace the existing lore components with.
     * @param lores The array of Components representing the lore components to be replaced.
     * @return The updated builder instance.
     */
    public Builder replaceAllLore(String replacement, Component... lores) {
      return replaceAllLore(TextUtil.format(replacement), lores);
    }

    /**
     * Replaces all occurrences of specified lore components with a replacement component in the
     * item's lore.
     *
     * @param replacement The Component to replace the existing lore components with.
     * @param lores The array of Components representing the lore components to be replaced.
     * @return The updated builder instance.
     */
    public Builder replaceAllLore(String replacement, String... lores) {
      Component[] components = new Component[lores.length];
      for (int i = 0; i < lores.length; i++) {
        components[i] = TextUtil.format(lores[1]);
      }
      return replaceAllLore(TextUtil.format(replacement), components);
    }

    /**
     * Replaces all occurrences of specified lore components with a replacement component in the
     * item's lore.
     *
     * @param replacement The Component to replace the existing lore components with.
     * @param lores The array of Components representing the lore components to be replaced.
     * @return The updated builder instance.
     */
    public Builder replaceAllLore(Component replacement, String... lores) {
      Component[] components = new Component[lores.length];
      for (int i = 0; i < lores.length; i++) {
        components[i] = TextUtil.format(lores[1]);
      }
      return replaceAllLore(replacement, components);
    }

    /**
     * Replaces all occurrences of specified lore components with a replacement component in the
     * item's lore.
     *
     * @param replacement The Component to replace the existing lore components with.
     * @param lores The array of Components representing the lore components to be replaced.
     * @return The updated builder instance.
     */
    public Builder replaceAllLore(Component replacement, Component... lores) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      for (Component loreToCompare : lores) {
        for (int i = 0; i < loreList.size(); i++) {
          if (loreList.get(i).equals(loreToCompare)) {
            loreList.set(i, replacement);
          }
        }
      }
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @param offset The starting index to search for the lore component in the lore list.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(Component lore, Component replacement, int offset) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        return this;
      }
      for (int i = offset; i < loreList.size(); i++) {
        if (loreList.get(i).equals(lore)) {
          loreList.set(i, replacement);
          break;
        }
      }
      this.itemStack.lore(loreList);
      return this;
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(Component lore, Component replacement) {
      return replaceFirstLore(lore, replacement, 0);
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(String lore, Component replacement) {
      return replaceFirstLore(TextUtil.format(lore), replacement);
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @param offset The starting index to search for the lore component in the lore list.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(String lore, Component replacement, int offset) {
      return replaceFirstLore(TextUtil.format(lore), replacement, offset);
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(String lore, String replacement) {
      return replaceFirstLore(TextUtil.format(lore), TextUtil.format(replacement));
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @param offset The starting index to search for the lore component in the lore list.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(String lore, String replacement, int offset) {
      return replaceFirstLore(TextUtil.format(lore), TextUtil.format(replacement), offset);
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(Component lore, String replacement) {
      return replaceFirstLore(lore, TextUtil.format(replacement));
    }

    /**
     * Replaces the first occurrence of a given lore component with a replacement component in the
     * item's lore list.
     *
     * @param lore The Component to be replaced.
     * @param replacement The Component to replace the existing lore component with.
     * @param offset The starting index to search for the lore component in the lore list.
     * @return The updated builder instance.
     */
    public Builder replaceFirstLore(Component lore, String replacement, int offset) {
      return replaceFirstLore(lore, TextUtil.format(replacement), offset);
    }

    /**
     * Sets the name of the item. Empty will set the name to "&7"
     *
     * @return The builder instance.
     */
    public Builder name() {
      return name(TextUtil.format("&7"));
    }

    /**
     * Sets the name of the item. Empty will set the name to "&7"
     *
     * @return The builder instance.
     */
    public Builder emptyName() {
      this.itemStack.editMeta(itemMeta -> itemMeta.displayName(Component.empty()));
      return this;
    }

    /**
     * Sets the name of the item. Empty will set the name to "&7"
     *
     * @param name The Component to set as the name.
     * @return The builder instance.
     */
    public Builder name(String name) {
      return name(TextUtil.format(name));
    }

    /**
     * Sets the name of the item. Empty will set the name to "&7"
     *
     * @param name The Component to set as the name.
     * @return The builder instance.
     */
    public Builder name(Component name) {
      this.itemStack.editMeta(itemMeta -> itemMeta.displayName(name));
      return this;
    }

    /**
     * Builds and returns the ItemStack.
     *
     * @return The instance of ItemStack built.
     */
    public ItemStack build() {
      return itemStack;
    }
  }
}
