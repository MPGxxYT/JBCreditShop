package me.mortaldev.jbcreditshop.menus;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.MenuData;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.jbcreditshop.utils.Utils;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class BasicMenu<T extends CRUD.Identifiable> extends InventoryGUI {

  private final MenuData menuData;
  private final Set<T> dataSet;

  public BasicMenu(MenuData menuData, Set<T> dataSet) {
    this.menuData = menuData;
    this.dataSet = dataSet;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(getInventoryName()));
  }

  private int getMaxPage() {
    if (dataSet.isEmpty()) {
      return 1;
    }
    return (int) Math.ceil(dataSet.size() / 45.0);
  }

  private int getSize() { // Paginated
    int maxPage = getMaxPage();
    if (menuData.getPage() >= maxPage) {
      int mod = (maxPage - 1) * 45;
      int size = dataSet.size();
      if (mod > 0) {
        size = size % mod;
      }
      return Utils.clamp((int) Math.ceil(size / 9.0) + 1, 2, 6);
    }
    return 6;
  }

  private Set<T> applySearch(Set<T> dataSet) {
    if (dataSet.isEmpty()) {
      return new HashSet<>();
    }
    String search = menuData.getSearchQuery();
    if (search == null || search.isEmpty()) {
      return dataSet;
    }
    Set<T> filtered = new HashSet<>();
    for (T data : dataSet) {
      if (data.getID().toLowerCase().contains(search.toLowerCase())) {
        filtered.add(data);
      }
    }
    return filtered;
  }

  private LinkedHashSet<T> applyPage(Set<T> data) {
    int page = menuData.getPage();
    if (page > getMaxPage()) {
      page = getMaxPage();
    }
    return data.stream()
        .skip((page - 1) * 45L)
        .limit(45)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private LinkedHashSet<T> applySort(Set<T> data) {
    LinkedHashSet<T> result = new LinkedHashSet<>(data);
    return result.stream()
        .sorted(Comparator.comparing(CRUD.Identifiable::getID))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public void decorate(Player player) {
    ItemStack whiteGlass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, whiteGlass);
    }
    if (getMaxPage() > 1) {
      addButton(8, NextButton());
    }
    if (menuData.getPage() > 1 || backButton() != null) {
      addButton(0, BackButton());
    }
    Set<T> filtered = applySearch(dataSet);
    LinkedHashSet<T> sorted = applySort(filtered);
    LinkedHashSet<T> pageAdjusted = applyPage(sorted);
    int slot = 0;
    for (T data : pageAdjusted) {
      addButton(slot + 9, DataButton(data));
      if (slot == (getSize() - 1) * 9) {
        break;
      }
      slot++;
    }
    addButton(4, SearchButton());
    addButton(5, CreateButton());
    super.decorate(player);
  }

  public abstract BasicMenu<T> getNewInstance(MenuData menuData, Set<T> dataSet);

  public abstract String getInventoryName();

  public abstract Consumer<InventoryClickEvent> backButton();

  public abstract Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData);

  public abstract ItemStack getDataButtonDisplayStack(T data, Player player);

  public abstract Consumer<InventoryClickEvent> dataButtonClickConsumer(T data);

  private InventoryButton DataButton(T data) {
    return new InventoryButton()
        .creator(player -> getDataButtonDisplayStack(data, player))
        .consumer(dataButtonClickConsumer(data));
  }

  private InventoryButton CreateButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.LIME_DYE)
                    .name("&2&lCreate")
                    .addLore("&7Creates a new instance to manage.")
                    .addLore()
                    .addLore("&7( click to create )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Create")
                  .itemLeft(ItemStackHelper.builder(Material.PAPER).name("id").build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText();
                          textEntry = textEntry.trim();
                          if (textEntry.isBlank()) {
                            player.sendMessage("&cMust enter an id!");
                            Main.playDenySound(player);
                            GUIManager.getInstance()
                                .openGUI(getNewInstance(menuData, dataSet), player);
                            return Collections.emptyList();
                          }
                          textEntry = textEntry.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
                          createNewData(textEntry, event, menuData).run();
                          return Collections.emptyList();
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton SearchButton() {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.ANVIL)
            .name("&3&lSearch")
            .addLore("&7Enter a search query to find something specific.")
            .addLore("");
    if (!menuData.getSearchQuery().isBlank()) {
      builder
          .addLore("&7Query: &f" + menuData.getSearchQuery())
          .addLore("")
          .addLore("&7( click to clear )");
    } else {
      builder.addLore("&7Query: &fNone").addLore("").addLore("&7( click to search )");
    }
    return new InventoryButton()
        .creator(player -> builder.build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.75f);
              if (!menuData.getSearchQuery().isBlank()) {
                menuData.setSearchQuery("");
                GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
                return;
              }
              if (menuData.getSearchQuery().isBlank() || event.isLeftClick()) {
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Search")
                    .itemLeft(ItemStackHelper.builder(Material.PAPER).name(" ").build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            String textEntry = stateSnapshot.getText();
                            textEntry = textEntry.trim();
                            menuData.setSearchQuery(textEntry);
                            GUIManager.getInstance()
                                .openGUI(getNewInstance(menuData, dataSet), player);
                            player.playSound(
                                player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 1f);
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              } else if (event.isRightClick()) {
                menuData.setSearchQuery("");
                GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
              }
            });
  }

  private InventoryButton BackButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&7&lBack")
                    .addLore("&7Click to return to previous page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page > 1) {
                menuData.setPage(page - 1);
                GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
              } else {
                if (backButton() != null) {
                  backButton().accept(event);
                }
              }
            });
  }

  private InventoryButton NextButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&7&lBack")
                    .addLore("&7Click to go to next page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page < getMaxPage()) {
                menuData.setPage(page + 1);
              }
              GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
            });
  }
}
