package me.mortaldev.jbcreditshop.modules.shopstats;

import java.util.HashMap;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.interfaces.Handler;

public class ShopStatsCRUD extends SingleCRUD<ShopStats> {
  private final String storagePath;

  public ShopStatsCRUD(Handler handler, String storagePath) {
    super(handler);
    this.storagePath = storagePath;
  }

  @Override
  public ShopStats construct() {
    return new ShopStats(new HashMap<>());
  }

  @Override
  public String getPath() {
    return storagePath;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters();
  }

  @Override
  public Class<ShopStats> getClazz() {
    return ShopStats.class;
  }

  @Override
  public String getId() {
    return "shopStats";
  }
}
