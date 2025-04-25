package org.example.jbcreditshop.modules;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.example.jbcreditshop.Main;
import org.example.jbcreditshop.utils.ItemStackHelper;

public class ShopItem {
  private String itemID;
  private String shopID;
  private Material displayMaterial;
  private String displayName;
  private List<String> description;
  private int shopSlot;
  private String group;
  private String permission;
  private ItemStack purchasedItem = null;
  private String purchasedCommand  = "";
  private String purchasedPermission = "";
  private int price = -1;
  private boolean locked = false;
  private String lockedReason = "&cshopItem item cannot be purchased.";
  private boolean visible = false;
  private boolean archived = false;

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

  public static Builder builder(ShopItem shopItem) {
    return new Builder(shopItem);
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

  public Material getDisplayMaterial() {
    return displayMaterial;
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


  }

}
