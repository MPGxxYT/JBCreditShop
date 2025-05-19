package me.mortaldev.jbcreditshop.modules.shopstats.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;

public record StatsHolder(HashSet<ShopItemStat> itemStats) {
  @JsonCreator
  public StatsHolder(@JsonProperty("itemStats") HashSet<ShopItemStat> itemStats) {
    this.itemStats = itemStats == null ? new HashSet<>() : itemStats;
  }

  @JsonIgnore
  public ShopItemStat getStat(String itemID) {
    for (ShopItemStat stat : itemStats) {
      if (stat.getItemID().equals(itemID)) {
        return stat;
      }
    }
    ShopItemStat shopItemStat = new ShopItemStat(itemID);
    itemStats.add(shopItemStat);
    return shopItemStat;
  }

  public void addStat(ShopItemStat stat) {
    itemStats.add(stat);
  }

  public void removeStat(ShopItemStat stat) {
    itemStats.remove(stat);
  }

  public boolean hasStat(ShopItemStat stat) {
    return itemStats.contains(stat);
  }

  public boolean hasStat(String itemID) {
    for (ShopItemStat stat : itemStats) {
      if (stat.getItemID().equals(itemID)) {
        return true;
      }
    }
    return false;
  }
}
