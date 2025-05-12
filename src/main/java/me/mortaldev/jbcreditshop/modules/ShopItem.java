package me.mortaldev.jbcreditshop.modules;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.records.Pair;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;

public class ShopItem {
  private Pair<FileConfiguration, String> source;
  private ConfigurationSection section;
  private String itemID = "";
  private String shopID = "";
  private Material displayMaterial = null;
  private String displayName = "";
  private List<String> description = new ArrayList<>();
  private int shopSlot = 0;
  private String group = "";
  private String permission = "";
  private ItemStack purchasedItem = null;
  private String purchasedCommand = "";
  private String purchasedPermission = "";
  private int price = -1;
  private int discount = 0;
  private boolean allowDiscountStacking = true;
  private boolean locked = false;
  private String lockedReason = "&cshopItem item cannot be purchased.";
  private boolean visible = false;
  private boolean archived = false;
  private boolean oneTimePurchase = false;

  public Pair<FileConfiguration, String> getSource() {
    return source;
  }

  public void setSource(Pair<FileConfiguration, String> source) {
    this.source = source;
  }

  public void setSection(ConfigurationSection section) {
    this.section = section;
  }

  public ConfigurationSection getSection() {
    return section;
  }

  public boolean canBeDisplayed() {
    return visible
        || purchasedCommand.isBlank()
        || purchasedItem == null
        || purchasedItem.getType().isAir()
        || purchasedPermission.isBlank();
  }

  public static Builder builder() {
    return new Builder(new ShopItem());
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder(ShopItem shopItem) {
    return new Builder(shopItem);
  }

  public int getDiscount() {
    return discount;
  }

  public boolean isAllowDiscountStacking() {
    return allowDiscountStacking;
  }

  public boolean isOneTimePurchase() {
    return oneTimePurchase;
  }

  public boolean isArchived() {
    return archived;
  }

  public boolean isVisible() {
    return visible;
  }

  public String getLockedReason() {
    return lockedReason;
  }

  public boolean isLocked() {
    return locked;
  }

  public int getPrice() {
    if (price < 0) {
      ShopManager.getInstance().getShop(shopID).ifPresentOrElse(shop -> price = shop.getDefaultPrice(), () -> {
        Main.warn("Shop " + shopID + " does not exist or it's not loaded.");
      });
    }
    return price;
  }

  public String getPurchasedPermission() {
    return purchasedPermission;
  }

  public String getPurchasedCommand() {
    return purchasedCommand;
  }

  public ItemStack getPurchasedItem() {
    return purchasedItem;
  }

  public String getPurchasedItemSerialized() {
    return ItemStackHelper.serialize(purchasedItem);
  }

  public String getPermission() {
    return permission;
  }

  public String getGroup() {
    return group;
  }

  public int getShopSlot() {
    return shopSlot;
  }

  public List<String> getDescription() {
    return description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPlainDisplayName() {
    String string = TextUtil.removeColors(displayName);
    string = TextUtil.removeDecoration(string);
    return string;
  }

  public Material getDisplayMaterial() {
    return displayMaterial;
  }

  public String getDisplayMaterialAsString() {
    return displayMaterial.getKey().toString();
  }

  public String getShopID() {
    return shopID;
  }

  public String getItemID() {
    return itemID;
  }

  public static class Builder {

    private final ShopItem shopItem;

    public Builder(ShopItem shopItem) {
      this.shopItem = shopItem;
    }

    public Builder() {
      this.shopItem = new ShopItem();
    }
    
    public ShopItem build() {
      return shopItem;
    }

    public Builder setDiscount(int discount) {
      shopItem.discount = discount;
      return this;
    }

    public Builder setAllowDiscountStacking(boolean allowDiscountStacking) {
      shopItem.allowDiscountStacking = allowDiscountStacking;
      return this;
    }

    public Builder setItemID(String itemID) {
      shopItem.itemID = itemID;
      return this;
    }

    public Builder setShopID(String shopID) {
      shopItem.shopID = shopID;
      return this;
    }

    public Builder setDisplayMaterial(String displayMaterial) {
      Material material = Material.matchMaterial(displayMaterial);
      if (material == null || material == Material.AIR) {
        Main.warn("Invalid material " + displayMaterial + " for shopitem " + shopItem.itemID);
        return this;
      }
      shopItem.displayMaterial = material;
      return this;
    }

    public Builder setDisplayMaterial(Material displayMaterial) {
      shopItem.displayMaterial = displayMaterial;
      return this;
    }

    public Builder setDisplayName(String displayName) {
      shopItem.displayName = displayName;
      return this;
    }

    public Builder setDescription(List<String> description) {
      shopItem.description = description;
      return this;
    }

    public Builder setShopSlot(int shopSlot) {
      shopItem.shopSlot = shopSlot;
      return this;
    }

    public Builder setGroup(String group) {
      shopItem.group = group;
      return this;
    }

    public Builder setPermission(String permission) {
      shopItem.permission = permission;
      return this;
    }

    public Builder setPurchasedItem(String purchasedItem) {
      if (purchasedItem.isBlank()) return this;
      try {
        shopItem.purchasedItem = ItemStackHelper.deserialize(purchasedItem);
      } catch (Exception e) {
        Main.warn("Invalid item '" + purchasedItem + "' for shopitem " + shopItem.itemID);
      }
      return this;
    }

    public Builder setPurchasedItem(ItemStack purchasedItem) {
      shopItem.purchasedItem = purchasedItem;
      return this;
    }

    public Builder setPurchasedCommand(String purchasedCommand) {
      shopItem.purchasedCommand = purchasedCommand;
      return this;
    }

    public Builder setPurchasedPermission(String purchasedPermission) {
      shopItem.purchasedPermission = purchasedPermission;
      return this;
    }

    public Builder setPrice(int price) {
      shopItem.price = price;
      return this;
    }

    public Builder setLocked(boolean locked) {
      shopItem.locked = locked;
      return this;
    }

    public Builder setLockedReason(String lockedReason) {
      shopItem.lockedReason = lockedReason;
      return this;
    }

    public Builder setVisible(boolean visible) {
      shopItem.visible = visible;
      return this;
    }

    public Builder setArchived(boolean archived) {
      shopItem.archived = archived;
      return this;
    }

    public Builder setOneTimePurchase(boolean oneTimePurchase) {
      shopItem.oneTimePurchase = oneTimePurchase;
      return this;
    }

  }

}
