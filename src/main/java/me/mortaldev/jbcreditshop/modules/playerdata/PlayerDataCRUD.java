package me.mortaldev.jbcreditshop.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcreditshop.Main;

public class PlayerDataCRUD extends CRUD<PlayerData> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/playerdata/";

  private static class Singleton {
    private static final PlayerDataCRUD INSTANCE = new PlayerDataCRUD();
  }

  public static synchronized PlayerDataCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private PlayerDataCRUD() {
    super(Jackson.getInstance());
  }


  @Override
  public Class<PlayerData> getClazz() {
    return PlayerData.class;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters();
  }

  @Override
  public String getPath() {
    return PATH;
  }
}
