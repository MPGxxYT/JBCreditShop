package me.mortaldev.jbcreditshop.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.crudapi.loading.CRUDRegistry;

import java.util.HashMap;
import java.util.logging.Logger;

public class PlayerDataManager extends CRUDManager<PlayerData> {
  private final Logger logger;

  public PlayerDataManager(CRUD<PlayerData> crud, CRUDRegistry registry, Logger logger) {
    super(crud, registry);
    this.logger = logger;
  }

  @Override
  public void log(String string) {
    logger.info(string);
  }

  @Override
  public PlayerData getNewInstance(String string) {
    return new PlayerData(string, new HashMap<>());
  }
}
