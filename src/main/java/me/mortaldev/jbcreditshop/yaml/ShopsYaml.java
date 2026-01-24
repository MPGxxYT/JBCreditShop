package me.mortaldev.jbcreditshop.yaml;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import me.mortaldev.jbcreditshop.modules.Shop;
import me.mortaldev.jbcreditshop.yaml.core.AbstractYamlCRUD;
import me.mortaldev.jbcreditshop.yaml.core.YamlField;
import me.mortaldev.jbcreditshop.yaml.core.YamlFieldType;

public class ShopsYaml extends AbstractYamlCRUD<Shop, Shop.Builder> {

  private static final String PATH = "/shops/";
  private static final String SHOPS_FILE_NAME = "shops.yml";

  private static class Singleton {
    private static final ShopsYaml INSTANCE = new ShopsYaml();
  }

  public static synchronized ShopsYaml getInstance() {
    return Singleton.INSTANCE;
  }

  private final List<YamlField<Shop, Shop.Builder>> fields;

  private ShopsYaml() {
    super();
    this.fields = buildFields();
  }

  @SuppressWarnings("unchecked")
  private List<YamlField<Shop, Shop.Builder>> buildFields() {
    return List.of(
        YamlField.<Shop, Shop.Builder>builder("shop_id", YamlFieldType.STRING)
            .required()
            .getter(Shop::getShopID)
            .setter((b, v) -> b.setShopID((String) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("shop_display", YamlFieldType.STRING)
            .getter(Shop::getShopDisplay)
            .setter((b, v) -> b.setShopDisplay((String) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("default_price", YamlFieldType.INTEGER)
            .required()
            .getter(Shop::getDefaultPrice)
            .setter((b, v) -> b.setDefaultPrice((Integer) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("default_display_material", YamlFieldType.STRING)
            .required()
            .getter(Shop::getDefaultDisplayMaterialAsString)
            .setter((b, v) -> b.setDefaultDisplayMaterial((String) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("locked", YamlFieldType.BOOLEAN)
            .getter(Shop::isLocked)
            .setter((b, v) -> b.setLocked((Boolean) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("locked_bypass_permission", YamlFieldType.STRING)
            .getter(Shop::getLockedBypassPermission)
            .setter((b, v) -> b.setLockedBypassPermission((String) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("discount", YamlFieldType.INTEGER)
            .getter(Shop::getDiscount)
            .setter((b, v) -> b.setDiscount((Integer) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("discount_group", YamlFieldType.STRING)
            .getter(Shop::getDiscountGroup)
            .setter((b, v) -> b.setDiscountGroup((String) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("style", YamlFieldType.ENUM)
            .required()
            .enumClass(Shop.Style.class)
            .getter(Shop::getStyle)
            .setter((b, v) -> b.setStyleAsEnum((Shop.Style) v))
            .build(),
        YamlField.<Shop, Shop.Builder>builder("size", YamlFieldType.INTEGER)
            .getter(Shop::getSize)
            .setter((b, v) -> b.setSize((Integer) v))
            .condition(shop -> shop.getStyle() == Shop.Style.CUSTOM)
            .build(),
        YamlField.<Shop, Shop.Builder>builder("filler", YamlFieldType.INT_STRING_MAP)
            .getter(shop -> shop.getFiller().isEmpty() ? null : getFillerAsStringMap(shop))
            .setter((b, v) -> b.setFiller((HashMap<Integer, String>) v))
            .condition(shop -> shop.getStyle() == Shop.Style.CUSTOM)
            .build());
  }

  private static HashMap<Integer, String> getFillerAsStringMap(Shop shop) {
    HashMap<Integer, String> result = new HashMap<>();
    shop.getFiller()
        .forEach(
            (slot, item) ->
                result.put(
                    slot,
                    me.mortaldev.jbcreditshop.utils.ItemStackHelper.serialize(item)));
    return result;
  }

  @Override
  protected String getPath() {
    return PATH;
  }

  @Override
  protected String getDefaultFileName() {
    return SHOPS_FILE_NAME;
  }

  @Override
  public List<YamlField<Shop, Shop.Builder>> getFields() {
    return fields;
  }

  @Override
  public Supplier<Shop.Builder> builderSupplier() {
    return Shop::builder;
  }

  @Override
  public Shop build(Shop.Builder builder) {
    return builder.build();
  }

  @Override
  public String getEntityName() {
    return "shop";
  }

  @Override
  public String getIdentifier(Shop entity) {
    return entity.getShopID() != null ? entity.getShopID() : "N/A";
  }
}
