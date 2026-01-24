package me.mortaldev.jbcreditshop.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.modules.ShopItem;
import me.mortaldev.jbcreditshop.modules.ShopManager;
import me.mortaldev.jbcreditshop.yaml.core.AbstractYamlCRUD;
import me.mortaldev.jbcreditshop.yaml.core.YamlField;
import me.mortaldev.jbcreditshop.yaml.core.YamlFieldType;
import org.bukkit.configuration.ConfigurationSection;

public class ShopItemsYaml extends AbstractYamlCRUD<ShopItem, ShopItem.Builder> {

  private static final String PATH = "/shopitems/";
  private static final String SHOP_ITEMS_FILE_NAME = "shopitems.yml";

  private static class Singleton {
    private static final ShopItemsYaml INSTANCE = new ShopItemsYaml();
  }

  public static synchronized ShopItemsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private final List<YamlField<ShopItem, ShopItem.Builder>> fields;
  private final YamlField<ShopItem, ShopItem.Builder> shopSlotField;

  @SuppressWarnings("unchecked")
  private ShopItemsYaml() {
    super();
    this.shopSlotField =
        YamlField.<ShopItem, ShopItem.Builder>builder("shop_slot", YamlFieldType.INTEGER)
            .getter(ShopItem::getShopSlot)
            .setter((b, v) -> b.setShopSlot((Integer) v))
            .build();
    this.fields = buildFields();
  }

  @SuppressWarnings("unchecked")
  private List<YamlField<ShopItem, ShopItem.Builder>> buildFields() {
    return List.of(
        YamlField.<ShopItem, ShopItem.Builder>builder("item_id", YamlFieldType.STRING)
            .required()
            .getter(ShopItem::getItemID)
            .setter((b, v) -> b.setItemID((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("shop_id", YamlFieldType.STRING)
            .required()
            .getter(ShopItem::getShopID)
            .setter((b, v) -> b.setShopID((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder(
                "use_display_itemstack", YamlFieldType.BOOLEAN)
            .getter(ShopItem::isUseDisplayItemStack)
            .setter((b, v) -> b.setUseDisplayItemStack((Boolean) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("display_itemstack", YamlFieldType.STRING)
            .getter(ShopItem::getDisplayItemStackSerialized)
            .setter((b, v) -> b.setDisplayItemStack((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("display_material", YamlFieldType.STRING)
            .getter(ShopItem::getDisplayMaterialAsString)
            .setter((b, v) -> b.setDisplayMaterial((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("display_name", YamlFieldType.STRING)
            .getter(ShopItem::getDisplayName)
            .setter((b, v) -> b.setDisplayName((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("description", YamlFieldType.STRING_LIST)
            .getter(ShopItem::getDescription)
            .setter((b, v) -> b.setDescription((List<String>) v))
            .build(),
        shopSlotField,
        YamlField.<ShopItem, ShopItem.Builder>builder("group", YamlFieldType.STRING)
            .getter(ShopItem::getGroup)
            .setter((b, v) -> b.setGroup((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("permission", YamlFieldType.STRING)
            .getter(ShopItem::getPermission)
            .setter((b, v) -> b.setPermission((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("purchased_item", YamlFieldType.STRING)
            .getter(ShopItem::getPurchasedItemSerialized)
            .setter((b, v) -> b.setPurchasedItem((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("purchased_command", YamlFieldType.STRING)
            .getter(ShopItem::getPurchasedCommand)
            .setter((b, v) -> b.setPurchasedCommand((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("purchased_permission", YamlFieldType.STRING)
            .getter(ShopItem::getPurchasedPermission)
            .setter((b, v) -> b.setPurchasedPermission((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("one_time_purchase", YamlFieldType.BOOLEAN)
            .getter(ShopItem::isOneTimePurchase)
            .setter((b, v) -> b.setOneTimePurchase((Boolean) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("price", YamlFieldType.INTEGER)
            .required()
            .getter(ShopItem::getRawPrice)
            .setter((b, v) -> b.setPrice((Integer) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("discount", YamlFieldType.INTEGER)
            .getter(ShopItem::getDiscount)
            .setter((b, v) -> b.setDiscount((Integer) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder(
                "allow_discount_stacking", YamlFieldType.BOOLEAN)
            .getter(ShopItem::isAllowDiscountStacking)
            .setter((b, v) -> b.setAllowDiscountStacking((Boolean) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("locked", YamlFieldType.BOOLEAN)
            .getter(ShopItem::isLocked)
            .setter((b, v) -> b.setLocked((Boolean) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("locked_reason", YamlFieldType.STRING)
            .getter(ShopItem::getLockedReason)
            .setter((b, v) -> b.setLockedReason((String) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("visible", YamlFieldType.BOOLEAN)
            .getter(ShopItem::isVisible)
            .setter((b, v) -> b.setVisible((Boolean) v))
            .build(),
        YamlField.<ShopItem, ShopItem.Builder>builder("archived", YamlFieldType.BOOLEAN)
            .getter(ShopItem::isArchived)
            .setter((b, v) -> b.setArchived((Boolean) v))
            .build());
  }

  @Override
  protected String getPath() {
    return PATH;
  }

  @Override
  protected String getDefaultFileName() {
    return SHOP_ITEMS_FILE_NAME;
  }

  @Override
  public List<YamlField<ShopItem, ShopItem.Builder>> getFields() {
    return fields;
  }

  @Override
  protected List<YamlField<ShopItem, ShopItem.Builder>> getFieldsForWrite(ShopItem entity) {
    Optional<Shop> parentShopOpt = ShopManager.getInstance().getShop(entity.getShopID());
    if (parentShopOpt.isEmpty()) {
      Main.warn(
          "Parent shop with ID '"
              + entity.getShopID()
              + "' not found for ShopItem '"
              + entity.getItemID()
              + "'. Using default settings.");
    }
    Shop parentShop = parentShopOpt.orElseGet(Shop::new);

    // Filter out shop_slot if shop style is not CUSTOM
    if (parentShop.getStyle() != Shop.Style.CUSTOM) {
      List<YamlField<ShopItem, ShopItem.Builder>> filtered = new ArrayList<>(fields);
      filtered.remove(shopSlotField);
      return filtered;
    }
    return fields;
  }

  @Override
  protected List<YamlField<ShopItem, ShopItem.Builder>> getFieldsForRead(
      ConfigurationSection section) {
    String shopId = section.getString("shop_id");
    if (shopId != null) {
      Optional<Shop> parentShopOpt = ShopManager.getInstance().getShop(shopId);
      if (parentShopOpt.isPresent() && parentShopOpt.get().getStyle() != Shop.Style.CUSTOM) {
        // Filter out shop_slot for non-CUSTOM style shops
        List<YamlField<ShopItem, ShopItem.Builder>> filtered = new ArrayList<>(fields);
        filtered.remove(shopSlotField);
        return filtered;
      }
    }
    return fields;
  }

  @Override
  public Supplier<ShopItem.Builder> builderSupplier() {
    return ShopItem::builder;
  }

  @Override
  public ShopItem build(ShopItem.Builder builder) {
    return builder.build();
  }

  @Override
  public String getEntityName() {
    return "shop item";
  }

  @Override
  public String getIdentifier(ShopItem entity) {
    return entity.getItemID() != null ? entity.getItemID() : "N/A";
  }
}
