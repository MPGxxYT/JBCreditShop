package me.mortaldev.jbcreditshop.modules.bundles;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.serializers.ItemStackDeserializer;
import me.mortaldev.jbcreditshop.serializers.ItemStackSerializer;
import org.bukkit.inventory.ItemStack;

public class BundleCRUD extends CRUD<Bundle> {

  private static final String PATH = Main.getInstance().getDataFolder() + "/bundles/";

  private static class Singleton {
    private static final BundleCRUD INSTANCE = new BundleCRUD();
  }

  public static synchronized BundleCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private BundleCRUD() {
    super(Jackson.getInstance());
  }

  @Override
  public Class<Bundle> getClazz() {
    return Bundle.class;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters()
        .addSerializer(ItemStack.class, new ItemStackSerializer())
        .addDeserializer(ItemStack.class, new ItemStackDeserializer());
  }

  @Override
  public String getPath() {
    return PATH;
  }
}
