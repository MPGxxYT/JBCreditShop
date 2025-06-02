package me.mortaldev.jbcreditshop.modules.bundles;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.mortaldev.crudapi.CRUD;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Bundle implements CRUD.Identifiable {

  private final String ID;
  private String name;
  private String description;
  private final List<ItemStack> items;

  public Bundle(@JsonProperty("items") List<ItemStack> items, @JsonProperty("id") String ID) {
    this.items = items == null ? new ArrayList<>() : items;
    this.ID = ID;
  }

  public static Bundle create(String ID) {
    Bundle bundle = new Bundle(new ArrayList<>(), ID);
    bundle.setDescription(ID);
    bundle.setName(ID);
    return bundle;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<ItemStack> getItems() {
    return items;
  }

  public void addItem(ItemStack itemStack) {
    items.add(itemStack);
  }

  public void removeItem(ItemStack itemStack) {
    items.remove(itemStack);
  }

  public boolean hasItem(ItemStack itemStack) {
    return items.contains(itemStack);
  }

  @Override
  public String getID() {
    return ID;
  }
}
