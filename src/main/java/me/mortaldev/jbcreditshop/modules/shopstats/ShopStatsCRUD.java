package me.mortaldev.jbcreditshop.modules.shopstats;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcreditshop.Main;

public class ShopStatsCRUD extends SingleCRUD<ShopStats> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/plugin/";

  private static class Singleton {
    private static final ShopStatsCRUD INSTANCE = new ShopStatsCRUD();
  }

  public static synchronized ShopStatsCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private ShopStatsCRUD() {
    super(Jackson.getInstance());
  }

  @Override
  public ShopStats construct() {
    return new ShopStats(new HashMap<>());
  }

  @Override
  public String getPath() {
    return PATH;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    CRUDAdapters crudAdapters = new CRUDAdapters();
    crudAdapters.addModule(new JavaTimeModule());
    return crudAdapters;
  }

  @Override
  public Class<ShopStats> getClazz() {
    return ShopStats.class;
  }

  @Override
  public String getID() {
    return "shopStats";
  }
}
