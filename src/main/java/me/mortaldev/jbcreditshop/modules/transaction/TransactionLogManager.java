package me.mortaldev.jbcreditshop.modules.transaction;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.jbcreditshop.Main;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class TransactionLogManager extends CRUDManager<TransactionLog> {
  private static class Singleton {
    private static final TransactionLogManager INSTANCE = new TransactionLogManager();
  }

  public static synchronized TransactionLogManager getInstance() {
    return Singleton.INSTANCE;
  }

  private TransactionLogManager() {}

  @Override
  public CRUD<TransactionLog> getCRUD() {
    return TransactionLogCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }

  public TransactionLog getTodayLog() {
    return getByID(Main.getLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse(new TransactionLog(Main.getLocalDate(), new HashMap<>()));
  }
}
