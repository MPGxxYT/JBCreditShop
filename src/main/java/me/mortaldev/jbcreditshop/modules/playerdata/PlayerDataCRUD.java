package me.mortaldev.jbcreditshop.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcreditshop.Main;

public class PlayerDataCRUD extends CRUD<PlayerData> {
  private final String storagePath;

  public PlayerDataCRUD(String storagePath, Jackson handler) {
    super(handler);
    this.storagePath = storagePath;
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
    return storagePath;
  }
}
