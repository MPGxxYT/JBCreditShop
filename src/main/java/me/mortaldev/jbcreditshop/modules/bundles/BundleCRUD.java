package me.mortaldev.jbcreditshop.modules.bundles;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;

public class BundleCRUD extends CRUD<Bundle> {
  private final String storagePath;

  public BundleCRUD(String storagePath, Jackson handler) {
    super(handler);
    this.storagePath = storagePath;
  }

  @Override
  public Class<Bundle> getClazz() {
    return Bundle.class;
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
