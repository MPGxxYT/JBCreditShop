package me.mortaldev.jbcreditshop.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.records.Pair;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class Shop implements CRUD.Identifiable {

  private Pair<FileConfiguration, String> source;
  private ConfigurationSection section;

  private String shopID;
  private String shopDisplay;
  private int defaultPrice = 12000;
  private Material defaultDisplayMaterial = Material.GOLD_INGOT;
  private boolean locked = true;
  private String lockedBypassPermission;
  private int discount;
  private String
      discountGroup; // TODO: Turn discount + discountGroup into HashMap<String, Integer> as <Group,
                     // Discount> to allow for multiple discounts
  private Style style = Style.AUTO;
  private int size;
  private HashMap<Integer, String> filler;

  public Pair<FileConfiguration, String> getSource() {
    return source;
  }

  public void setSource(Pair<FileConfiguration, String> source) {
    this.source = source;
  }

  public ConfigurationSection getSection() {
    return section;
  }

  public void setSection(ConfigurationSection section) {
    this.section = section;
  }

  public String getShopID() {
    return shopID;
  }

  @Override
  public String getID() {
    return shopID;
  }

  public String getShopDisplay() {
    if (shopDisplay.isBlank()) {
      return shopID;
    }
    return shopDisplay;
  }

  public int getDefaultPrice() {
    return defaultPrice;
  }

  public Material getDefaultDisplayMaterial() {
    return defaultDisplayMaterial;
  }

  public String getDefaultDisplayMaterialAsString() {
    return defaultDisplayMaterial.name();
  }

  public boolean isLocked() {
    return locked;
  }

  public String getLockedBypassPermission() {
    return lockedBypassPermission;
  }

  public int getDiscount() {
    return discount;
  }

  public String getDiscountGroup() {
    return discountGroup == null ? "" : discountGroup;
  }

  public Style getStyle() {
    return style;
  }

  public int getSize() {
    return size;
  }

  public HashMap<Integer, ItemStack> getFiller() {
    if (filler == null) {
      filler = new HashMap<>();
    }
    return filler.entrySet().stream()
        .map(
            entry -> {
              ItemStack deserializedItem = ItemStackHelper.deserialize(entry.getValue());
              return Map.entry(entry.getKey(), deserializedItem);
            })
        .collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, HashMap::new));
  }

  public void addFiller(int slot, ItemStack item) {
    if (filler == null) {
      filler = new HashMap<>();
    }
    filler.put(slot, ItemStackHelper.serialize(item));
  }

  public void removeFiller(int slot) {
    if (filler == null) {
      return;
    }
    filler.remove(slot);
  }

  public static Builder builder() {
    return new Builder(new Shop());
  }

  public static Builder builder(Shop shop) {
    return new Builder(shop);
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    Shop shop;

    public Builder() {
      this.shop = new Shop();
    }

    public Builder(Shop shop) {
      this.shop = shop;
    }

    public Shop build() {
      return shop;
    }

    public Builder setShopID(String shopID) {
      shop.shopID = shopID;
      return this;
    }

    public Builder setShopDisplay(String shopDisplay) {
      shop.shopDisplay = shopDisplay;
      return this;
    }

    public Builder setDefaultPrice(int defaultPrice) {
      shop.defaultPrice = defaultPrice;
      return this;
    }

    public Builder setDefaultDisplayMaterial(String displayMaterial) {
      Material material = Material.matchMaterial(displayMaterial);
      if (material == null || material == Material.AIR) {
        Main.warn("Invalid material " + displayMaterial + " for shop " + shop.shopID);
        return this;
      }
      shop.defaultDisplayMaterial = material;
      return this;
    }

    public Builder setDefaultDisplayMaterial(Material displayMaterial) {
      shop.defaultDisplayMaterial = displayMaterial;
      return this;
    }

    public Builder setLocked(boolean locked) {
      shop.locked = locked;
      return this;
    }

    public Builder setLockedBypassPermission(String lockedBypassPermission) {
      shop.lockedBypassPermission = lockedBypassPermission;
      return this;
    }

    public Builder setDiscount(int discount) {
      shop.discount = discount;
      return this;
    }

    public Builder setDiscountGroup(String discountGroup) {
      shop.discountGroup = discountGroup;
      return this;
    }

    public Builder setStyle(String style) {
      Style styleEnum;
      try {
        styleEnum = Style.valueOf(style.toUpperCase());
      } catch (IllegalArgumentException e) {
        Main.warn("Invalid shop style: " + style);
        return this;
      }
      shop.style = styleEnum;
      return this;
    }

    public Builder setStyleAsEnum(Style style) {
      shop.style = style;
      return this;
    }

    public Builder setSize(int size) {
      shop.size = size;
      return this;
    }

    public Builder setFiller(HashMap<Integer, String> filler) {
      shop.filler = filler;
      return this;
    }
  }

  public enum Style {
    AUTO,
    CUSTOM
  }
}
