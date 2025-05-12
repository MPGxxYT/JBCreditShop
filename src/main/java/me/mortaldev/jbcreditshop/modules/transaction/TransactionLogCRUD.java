package me.mortaldev.jbcreditshop.modules.transaction;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcreditshop.Main;

public class TransactionLogCRUD extends CRUD<TransactionLog> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/transactions/";

  private static class Singleton {
    private static final TransactionLogCRUD INSTANCE = new TransactionLogCRUD();
  }

  public static synchronized TransactionLogCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private TransactionLogCRUD() {
    super(Jackson.getInstance());
  }
  @Override
  public Class<TransactionLog> getClazz() {
    return TransactionLog.class;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    CRUDAdapters crudAdapters = new CRUDAdapters();
    crudAdapters.addModule(new JavaTimeModule());
    return crudAdapters;
  }

  @Override
  public String getPath() {
    return PATH;
  }
}
