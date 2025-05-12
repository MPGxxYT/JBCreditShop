package me.mortaldev.jbcreditshop.modules.playerdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcreditshop.modules.ShopItem;

import java.util.HashMap;

public class PlayerData implements CRUD.Identifiable{

  private final String UUID;
  private final HashMap<String, Integer> purchasedItems;

  @JsonCreator
  public PlayerData(@JsonProperty("UUID") String UUID, @JsonProperty("purchasedItems") HashMap<String, Integer> purchasedItems) {
    this.UUID = UUID;
    this.purchasedItems = purchasedItems == null ? new HashMap<>() : purchasedItems;
  }

  @JsonIgnore
  public static PlayerData create(String UUID) {
    return new PlayerData(UUID, new HashMap<>());
  }

  public void addPurchasedItem(ShopItem item, int amount) {
    int newAmount = 0;
    if (purchasedItems.containsKey(item.getItemID())) {
      newAmount = purchasedItems.get(item.getItemID());
    }
    purchasedItems.put(item.getItemID(), newAmount + amount);
  }

  public void removePurchasedItem(ShopItem item) {
    purchasedItems.remove(item.getItemID());
  }

  public boolean hasPurchasedItem(ShopItem item) {
    return purchasedItems.containsKey(item.getItemID());
  }

  @JsonIgnore
  public int getPurchasedAmount(ShopItem item) {
    return purchasedItems.getOrDefault(item.getItemID(), 0);
  }


  @JsonProperty("purchasedItems")
  public HashMap<String, Integer> getPurchasedItems() {
    return purchasedItems;
  }

  @Override
  @JsonProperty("UUID")
  public String getID() {
    return UUID;
  }
}
