package me.mortaldev.jbcreditshop.modules.shopstats.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopItemStat {
  private final String itemID;
  private int purchaseCount;

  @JsonCreator
  public ShopItemStat(@JsonProperty("itemID") String itemID) {
    this.itemID = itemID;
  }

  public String getItemID() {
    return itemID;
  }

  public int getPurchaseCount() {
    return purchaseCount;
  }

  public void setPurchaseCount(int purchaseCount) {
    this.purchaseCount = purchaseCount;
  }

  public int addPurchaseCount(int amount) {
    this.purchaseCount += amount;
    return this.purchaseCount;
  }

  public int removePurchaseCount(int amount) {
    this.purchaseCount -= amount;
    return this.purchaseCount;
  }
}
