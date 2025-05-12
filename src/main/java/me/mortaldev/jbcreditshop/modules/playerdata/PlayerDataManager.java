package me.mortaldev.jbcreditshop.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.jbcreditshop.Main;

public class PlayerDataManager extends CRUDManager<PlayerData> {

  private static class Singleton {
    private static final PlayerDataManager INSTANCE = new PlayerDataManager();
  }

  public static synchronized PlayerDataManager getInstance() {
    return Singleton.INSTANCE;
  }

  private PlayerDataManager() {}
  @Override
  public CRUD<PlayerData> getCRUD() {
    return PlayerDataCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }
}
