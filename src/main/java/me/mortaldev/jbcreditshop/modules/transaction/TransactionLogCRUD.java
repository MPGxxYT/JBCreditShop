package me.mortaldev.jbcreditshop.modules.transaction;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;

public class TransactionLogCRUD extends CRUD<TransactionLog> {
  private final String storagePath;

  public TransactionLogCRUD(String storagePath, Jackson handler) {
    super(handler);
    this.storagePath = storagePath;
  }

  @Override
  public Class<TransactionLog> getClazz() {
    return TransactionLog.class;
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
