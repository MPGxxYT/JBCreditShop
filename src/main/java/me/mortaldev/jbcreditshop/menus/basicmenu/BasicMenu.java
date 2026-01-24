package me.mortaldev.jbcreditshop.menus.basicmenu;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 * An abstract, generic base class for creating powerful, paginated, and interactive menus.
 *
 * <p>This class provides a robust framework for displaying collections of data with built-in
 * features for:
 *
 * <ul>
 *   <li>Pagination (up to 45 items per page).
 *   <li>Text-based searching.
 *   <li>Multi-criteria sorting with ascending/descending direction.
 *   <li>Filtering based on one or more enums.
 *   <li>A creation mechanism using an Anvil GUI for user input.
 * </ul>
 *
 * Implementations must provide the specific logic for displaying data, handling interactions, and
 * defining the available sorting and filtering options.
 *
 * @param <T> The type of identifiable data object this menu manages.
 * @version 1.0.6
 */
public abstract class BasicMenu<T> extends InventoryGUI {

  /**
   * The state object for the menu, storing the current page, search query, sorting, and filter
   * selections.
   */
  protected final MenuData menuData;

  /** The complete, unfiltered set of data to be displayed in the menu. */
  protected final Set<T> dataSet;

  /** The configuration determining which features are enabled and where buttons are placed. */
  protected final MenuConfig config;

  /** Tracks all currently open menus by player for refreshing capabilities. */
  private static final Map<Player, BasicMenu<?>> openMenus = new ConcurrentHashMap<>();

  /** The auto-refresh task for this menu instance, if enabled. */
  private BukkitTask autoRefreshTask;

  public Set<T> getDataSet() {
    return dataSet;
  }

  /**
   * Hook method to preprocess the dataset before filtering, sorting, and display.
   * Override this to transform the data (e.g., apply visibility filters) before
   * it's used by the menu system.
   *
   * <p><b>Default:</b> Returns the data unchanged.
   *
   * <p><b>Example:</b>
   * <pre>{@code
   * @Override
   * protected Set<ShopItem> preprocessData(Set<ShopItem> data) {
   *   if (adminMode) return data;
   *   return data.stream()
   *       .filter(item -> item.isVisible())
   *       .collect(Collectors.toSet());
   * }
   * }</pre>
   *
   * @param data The raw dataset.
   * @return The processed dataset to be used for display.
   */
  protected Set<T> preprocessData(Set<T> data) {
    return data;
  }

  /**
   * Constructs a new BasicMenu with default configuration (all features enabled).
   *
   * @param menuData The data object that maintains the state of the menu (e.g., page, search).
   * @param dataSet The full set of data items to be displayed and managed by this menu.
   *     <p>example: super(menuData, LobbyManager.getInstance().getLobbies());
   */
  public BasicMenu(MenuData menuData, Set<T> dataSet) {
    this(menuData, dataSet, MenuConfig.defaultConfig());
  }

  /**
   * Constructs a new BasicMenu with custom configuration.
   *
   * @param menuData The data object that maintains the state of the menu (e.g., page, search).
   * @param dataSet The full set of data items to be displayed and managed by this menu.
   * @param config The configuration for enabled features and button placement.
   */
  public BasicMenu(MenuData menuData, Set<T> dataSet, MenuConfig config) {
    this.menuData = menuData;
    this.dataSet = dataSet;
    this.config = config;
    if (config.isOrderEnabled() && getOrderOptions() != null && !getOrderOptions().isEmpty()) {
      // Set to first option if orderId is null or doesn't exist in current options
      String currentOrderId = menuData.getOrderId();
      boolean orderIdExists =
          currentOrderId != null
              && getOrderOptions().stream().anyMatch(opt -> opt.id().equals(currentOrderId));
      if (!orderIdExists) {
        menuData.setOrderId(getOrderOptions().get(0).id());
      }
    }
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(getInventoryName()));
  }

  /** Calculates the maximum number of pages based on the default search results. */
  private int getMaxPage() {
    return getMaxPage(applySearch(preprocessData(dataSet)));
  }

  private int getMaxPage(Set<T> searchResults) {
    int searchResultSize = searchResults.size();
    if (searchResultSize == 0) {
      return 1;
    }
    return (int) Math.ceil(searchResultSize / 45.0);
  }

  /**
   * Determines the appropriate inventory size (2-6 rows) based on the number of items on the
   * current page.
   */
  private int getSize() {
    Set<T> searchResults = applySearch(preprocessData(dataSet));
    int maxPage = getMaxPage(searchResults);
    if (menuData.getPage() >= maxPage) {
      int totalItems = searchResults.size();
      if (totalItems == 0) return 2; // For control bar + empty row
      int itemsOnLastPage = totalItems % 45;
      if (itemsOnLastPage == 0) itemsOnLastPage = 45;
      return clamp((int) Math.ceil(itemsOnLastPage / 9.0) + 1);
    }
    return 6;
  }

  /** Clamps an integer value between a minimum of 2 and a maximum of 6. */
  private int clamp(int value) {
    return Math.max(2, Math.min(6, value));
  }

  /**
   * Filters the dataset based on the search query in {@link MenuData}.
   *
   * @param dataSet The set of data to filter by search query.
   * @return A new set containing only the items that match the search query.
   */
  private Set<T> applySearch(Set<T> dataSet) {
    String search = menuData.getSearch() != null ? menuData.getSearch().toLowerCase() : null;
    if (search == null || search.isEmpty()) {
      return dataSet;
    }
    return dataSet.stream().filter(getSearchPredicate(search)).collect(Collectors.toSet());
  }

  /**
   * Filters the dataset based on the selected enum filters in {@link MenuData}.
   *
   * @param dataSet The set of data to filter.
   * @return A new set containing only the items that match the active enum filters.
   */
  private Set<T> applyEnumFilters(Set<T> dataSet) {
    if (getEnumFilters() == null || getEnumFilters().isEmpty()) {
      return dataSet;
    }
    Set<T> filteredData = new HashSet<>(dataSet);
    for (EnumFilter<T, ?> filter :
        getEnumFilters()) { // The wildcard '?' is the source of the issue
      // We call a helper method to capture the wildcard type
      applySingleFilter(filteredData, filter);
    }
    return filteredData;
  }

  /**
   * Helper method to apply a single enum filter to a dataset. This method uses a generic capture to
   * resolve the wildcard type issue from `applyEnumFilters`.
   *
   * @param dataToFilter The set of data to be filtered (will be modified).
   * @param filter The specific {@link EnumFilter} to apply.
   * @param <E> The captured enum type that implements {@link MenuData.IDisplayName}.
   */
  private <E extends Enum<E> & MenuData.IDisplayName> void applySingleFilter(
      Set<T> dataToFilter, EnumFilter<T, E> filter) {
    menuData
        .getEnumFilter(filter.filterId())
        .ifPresent(
            filterValue -> {
              dataToFilter.removeIf(
                  data -> {
                    E dataValue = filter.dataExtractor().apply(data);
                    return dataValue != null && !dataValue.equals(filterValue);
                  });
            });
  }

  /**
   * Applies predicate-based filters to the dataset. Unlike enum filters which extract properties,
   * predicate filters test each item against a condition that may depend on the player.
   *
   * @param dataSet The set of data to filter.
   * @param player The player viewing the menu (used for player-context predicates).
   * @return A new set containing only the items that pass all active predicate filters.
   */
  private Set<T> applyPredicateFilters(Set<T> dataSet, Player player) {
    List<PredicateFilter<T, ?>> predicateFilters = getPredicateFilters();
    if (predicateFilters == null || predicateFilters.isEmpty()) {
      return dataSet;
    }
    Set<T> filteredData = new HashSet<>(dataSet);
    for (PredicateFilter<T, ?> filter : predicateFilters) {
      applySinglePredicateFilter(filteredData, filter, player);
    }
    return filteredData;
  }

  /**
   * Helper method to apply a single predicate filter to a dataset.
   *
   * @param dataToFilter The set of data to be filtered (will be modified).
   * @param filter The specific {@link PredicateFilter} to apply.
   * @param player The player viewing the menu.
   * @param <E> The captured enum type that implements {@link MenuData.IDisplayName}.
   */
  @SuppressWarnings("unchecked")
  private <E extends Enum<E> & MenuData.IDisplayName> void applySinglePredicateFilter(
      Set<T> dataToFilter, PredicateFilter<T, E> filter, Player player) {
    menuData
        .getEnumFilter(filter.filterId())
        .ifPresent(
            filterValue -> {
              // Get the predicate for this filter option
              BiPredicate<T, Player> predicate =
                  filter.predicateSupplier().apply((E) filterValue);
              // If predicate is null, it means "show all" - don't filter
              if (predicate != null) {
                dataToFilter.removeIf(data -> !predicate.test(data, player));
              }
            });
  }

  /**
   * Reverses the order of the dataset if the direction in {@link MenuData} is DESCENDING.
   *
   * @param data The ordered set of data to potentially reverse.
   * @return A new {@link LinkedHashSet} with the final order.
   */
  private LinkedHashSet<T> applyDirection(LinkedHashSet<T> data) {
    if (menuData.getDirection() == MenuData.Direction.DESCENDING) {
      List<T> list = new ArrayList<>(data);
      Collections.reverse(list);
      return new LinkedHashSet<>(list);
    }
    return data;
  }

  /**
   * Paginates the dataset based on the current page number in {@link MenuData}.
   *
   * @param data The full, sorted, and filtered set of data to paginate.
   * @return A {@link LinkedHashSet} containing only the items for the current page.
   */
  private LinkedHashSet<T> applyPage(Set<T> data) {
    // No need to calculate maxPage here, as it's done in decorate()
    int maxPage = getMaxPage(data);
    int page = menuData.getPage();

    // If the current page is now out of bounds (e.g., after a search), reset it to the new max
    // page.
    if (page > maxPage) {
      page = maxPage;
      menuData.setPage(page);
    }

    return data.stream()
        .skip((long) (page - 1) * 45L) // Cast to long for safety with large numbers
        .limit(45)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Decorates the inventory with all buttons and items. This method orchestrates the filtering,
   * sorting, and pagination of data, then adds the data buttons and control buttons to the
   * inventory.
   *
   * @param player The player for whom the GUI is being decorated.
   */
  @Override
  public void decorate(Player player) {
    // Top control bar background
    ItemStack glass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, glass);
    }

    // Process and display data
    Set<T> searchFiltered = applySearch(preprocessData(dataSet));
    Set<T> enumFiltered = applyEnumFilters(searchFiltered);
    Set<T> filtered = applyPredicateFilters(enumFiltered, player);
    int maxPage = getMaxPage(filtered);
    LinkedHashSet<T> ordered = applyOrder(filtered, menuData);
    LinkedHashSet<T> directionApplied = applyDirection(ordered); // This reverses the ordered set
    LinkedHashSet<T> pageAdjusted = applyPage(directionApplied);

    int slot = 9;
    for (T data : pageAdjusted) {
      addButton(slot++, DataButton(data));
    }

    // Add control buttons based on config
    if (config.isBackEnabled() && (backButton() != null || menuData.getPage() > 1)) {
      addButton(config.getBackSlot(), BackButton());
    }

    if (config.isPaginationEnabled() && menuData.getPage() < maxPage) {
      addButton(config.getNextSlot(), NextButton());
    }

    if (config.isCreateEnabled() && hasCreateButton()) {
      addButton(config.getCreateSlot(), CreateButton());
    }

    if (config.isSearchEnabled()) {
      addButton(config.getSearchSlot(), SearchButton());
    }

    if (config.isDirectionEnabled()) {
      addButton(config.getDirectionSlot(), DirectionButton());
    }

    if (config.isOrderEnabled() && getOrderOptions() != null && !getOrderOptions().isEmpty()) {
      addButton(config.getOrderSlot(), OrderButton());
    }

    List<EnumFilter<T, ?>> enumFilters = getEnumFilters();
    List<PredicateFilter<T, ?>> predicateFilters = getPredicateFilters();
    int totalFilters =
        (enumFilters != null ? enumFilters.size() : 0)
            + (predicateFilters != null ? predicateFilters.size() : 0);
    if (config.isFiltersEnabled() && totalFilters > 0) {
      addButton(config.getFiltersSlot(), FilterManagerButton());
    }

    if (!getCustomButtons().isEmpty()) {
      for (Map.Entry<InventoryButton, Integer> entry : getCustomButtons().entrySet()) {
        addButton(entry.getValue(), entry.getKey());
      }
    }

    super.decorate(player);
  }

  /**
   * A factory method to create a new instance of the concrete menu implementation. This is
   * essential for refreshing the menu state correctly.
   *
   * @param menuData The current menu state.
   * @param dataSet The original, full dataset.
   * @return A new instance of this menu.
   *     <p>A factory method to create a new instance of the concrete menu implementation. This is
   *     essential for refreshing the menu state correctly.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public BasicMenu<Lobby> getNewInstance(MenuData menuData, Set<Lobby> dataSet) {
   *   return new LobbiesMenu(menuData); // dataSet is implicitly passed in the constructor
   * }
   * }</pre>
   */
  public abstract BasicMenu<T> getNewMenuInstance(MenuData menuData, Set<T> dataSet);

  /**
   * Gets the name to be displayed at the top of the inventory.
   *
   * @return The inventory title.
   *     <p>Gets the name to be displayed at the top of the inventory.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public String getInventoryName() { return "Available Lobbies"; }
   * }</pre>
   */
  public abstract String getInventoryName();

  /**
   * Defines the action to be performed when the back button is clicked on the first page.
   *
   * <p><b>Default:</b> Returns null (no back button on page 1). Override only if you enable back in
   * your config and want a back action on page 1.
   *
   * @return A {@link Consumer} for the click event, or null to define no action.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public Consumer<InventoryClickEvent> backButton() {
   *   return event -> GUIManager.getInstance().openGUI(new MainMenu(), (Player) event.getWhoClicked());
   * }
   * }</pre>
   */
  public Consumer<InventoryClickEvent> backButton() {
    return null;
  }

  /**
   * Determines whether the "Create" button should be displayed in the control bar.
   *
   * <p><b>Default:</b> Returns false. Override only if you enable create in your config.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public boolean hasCreateButton() { return true; }
   * }</pre>
   */
  public boolean hasCreateButton() {
    return false;
  }

  /**
   * Whether to skip the anvil GUI when clicking create and directly call onCreateButtonClick.
   *
   * <p><b>Default:</b> Returns false (shows anvil). Override and return true to skip anvil.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public boolean skipCreateAnvil() { return true; }
   * }</pre>
   */
  public boolean skipCreateAnvil() {
    return false;
  }

  /**
   * Called when the create button is clicked and skipCreateAnvil() returns true. Use this for
   * custom creation flows that don't need an ID input.
   *
   * <p><b>Default:</b> Does nothing. Override only if skipCreateAnvil() returns true.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public void onCreateButtonClick(InventoryClickEvent event, MenuData menuData) {
   *   Player player = (Player) event.getWhoClicked();
   *   GUIManager.getInstance().openGUI(new TypeSelectionMenu(menuData), player);
   * }
   * }</pre>
   */
  public void onCreateButtonClick(InventoryClickEvent event, MenuData menuData) {
    // Override this when skipCreateAnvil() returns true
  }

  /**
   * Defines the action to be performed when a new data item is created via the Anvil GUI.
   *
   * <p><b>Default:</b> Does nothing. Override only if you enable create in your config.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
   *   return () -> {
   *     Player player = (Player) event.getWhoClicked();
   *     Lobby newLobby = new Lobby(textEntry, ...);
   *     LobbyManager.getInstance().add(newLobby);
   *     player.sendMessage("Lobby " + textEntry + " created!");
   *   };
   * }
   * }</pre>
   */
  public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
    return () -> {};
  }

  /**
   * Checks if a given ID already exists in the dataset.
   *
   * <p><b>Default:</b> Returns false (no duplicates). Override only if you enable create in your
   * config.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public boolean isDuplicate(String id) {
   *   return dataSet.stream().anyMatch(lobby -> lobby.getId().equalsIgnoreCase(id));
   * }
   * }</pre>
   */
  public boolean isDuplicate(String id) {
    return false;
  }

  /**
   * Returns a predicate for filtering items based on a search query.
   *
   * <p><b>Default:</b> Matches nothing. Override only if you enable search in your config.
   *
   * @param searchQuery The user-provided search string, already converted to lowercase.
   * @return A {@link Predicate} that returns true for items that match the query.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public Predicate<Lobby> getSearchPredicate(String searchQuery) {
   *   return lobby -> lobby.getId().toLowerCase().contains(searchQuery);
   * }
   * }</pre>
   */
  public Predicate<T> getSearchPredicate(String searchQuery) {
    return item -> false;
  }

  /**
   * Creates the {@link ItemStack} used to represent a single data item in the menu.
   *
   * @param data The data item to display.
   * @param player The player viewing the menu.
   * @return The visual representation of the data item.
   */
  /**
   * Creates the {@link ItemStack} used to represent a single data item in the menu.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public ItemStack getDataButtonDisplayStack(Lobby data, Player player) {
   *   return ItemStackHelper.builder(Material.BEACON)
   *       .name("&a" + data.getId())
   *       .addLore("&7Status: " + data.getStatus().getDisplayName())
   *       .addLore("&7Players: " + data.getProfiles().size())
   *       .build();
   * }
   * }</pre>
   */
  public abstract ItemStack getDataButtonDisplayStack(T data, Player player);

  /**
   * Defines the action to be performed when a data item's button is clicked.
   *
   * @param data The data item associated with the clicked button.
   * @param menuData The current menu state.
   * @return A {@link Consumer} for the click event.
   */
  /**
   * Defines the action to be performed when a data item's button is clicked.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Consumer<InventoryClickEvent> dataButtonClickConsumer(Lobby data, MenuData menuData) {
   *   return event -> {
   *     data.join((Player) event.getWhoClicked());
   *   };
   * }
   * }</pre>
   */
  public abstract Consumer<InventoryClickEvent> dataButtonClickConsumer(T data, MenuData menuData);

  /**
   * Gets the sound to be played when an action is denied (e.g., invalid input).
   *
   * @return The deny {@link Sound}.
   */
  /**
   * Gets the sound to be played when an action is denied (e.g., invalid input).
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Sound getDenySound() { return Sound.BLOCK_NOTE_BLOCK_BASS; }
   * }</pre>
   */
  public Sound getDenySound() {
    return Sound.BLOCK_NOTE_BLOCK_BASS;
  }

  public HashMap<InventoryButton, Integer> getCustomButtons() {
    return new HashMap<>();
  }

  /**
   * Provides the list of available sorting options for this menu.
   *
   * <p><b>Default:</b> Returns empty list. Override only if you enable order in your config.
   *
   * @return A list of {@link OrderOption} objects.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public List<OrderOption<Lobby>> getOrderOptions() {
   *   return List.of(
   *       new OrderOption<>("id", "Alphabetical", Comparator.comparing(Lobby::getId)),
   *       new OrderOption<>("players", "Player Count", Comparator.comparingInt(l -> l.getProfiles().size()).reversed())
   *   );
   * }
   * }</pre>
   */
  public List<OrderOption<T>> getOrderOptions() {
    return List.of();
  }

  /**
   * Provides the list of available enum-based filters for this menu.
   *
   * <p><b>Default:</b> Returns empty list. Override only if you enable filters in your config.
   *
   * @return A list of {@link EnumFilter} objects.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public List<EnumFilter<Lobby, ?>> getEnumFilters() {
   *   return List.of(
   *       new EnumFilter<>("gamemode", "Gamemode", Gamemode.class, Lobby::getGamemode),
   *       new EnumFilter<>("status", "Status", LobbyStatus.class, Lobby::getStatus)
   *   );
   * }
   * }</pre>
   */
  public List<EnumFilter<T, ?>> getEnumFilters() {
    return List.of();
  }

  /**
   * Provides the list of available predicate-based filters for this menu. Use predicate filters
   * when filtering depends on player context (permissions, purchase history, etc.) rather than
   * extracting a property from the data object.
   *
   * <p><b>Default:</b> Returns empty list. Override only if you need player-aware filtering.
   *
   * @return A list of {@link PredicateFilter} objects.
   *     <p><b>Example:</b>
   *     <pre>{@code
   * @Override
   * public List<PredicateFilter<ShopItem, ?>> getPredicateFilters() {
   *   return List.of(
   *       new PredicateFilter<>("ownership", "Filter", OwnershipFilter.class, option -> {
   *           return switch (option) {
   *               case NONE -> null;
   *               case OWNED -> (item, player) -> isOwnedByPlayer(item, player);
   *               case UNOWNED -> (item, player) -> !isOwnedByPlayer(item, player);
   *           };
   *       })
   *   );
   * }
   * }</pre>
   */
  public List<PredicateFilter<T, ?>> getPredicateFilters() {
    return List.of();
  }

  /**
   * Applies the currently selected sorting option to the dataset.
   *
   * @param data The set of data to be sorted.
   * @param menuData The current menu state, containing the selected order ID.
   * @return A {@link LinkedHashSet} with the data sorted according to the selected comparator.
   */
  public LinkedHashSet<T> applyOrder(Set<T> data, MenuData menuData) {
    Optional<OrderOption<T>> selectedOption =
        getOrderOptions().stream()
            .filter(option -> option.id().equals(menuData.getOrderId()))
            .findFirst();

    // Return unsorted if no option is found
    return selectedOption
        .map(
            tOrderOption ->
                data.stream()
                    .sorted(tOrderOption.comparator())
                    .collect(Collectors.toCollection(LinkedHashSet::new)))
        .orElseGet(() -> new LinkedHashSet<>(data));
  }

  /** Creates the inventory button for a single data item. */
  private InventoryButton DataButton(T data) {
    return new InventoryButton()
        .creator(player -> getDataButtonDisplayStack(data, player))
        .consumer(dataButtonClickConsumer(data, menuData));
  }

  /** Creates the "Order By" button, which cycles through the available {@link OrderOption}s. */
  private InventoryButton OrderButton() {
    String currentOrderId = menuData.getOrderId();
    List<String> middleLore = new ArrayList<>();
    for (OrderOption<T> orderOption : getOrderOptions()) {
      String displayName = orderOption.displayName();
      if (orderOption.id().equals(currentOrderId)) {
        middleLore.add("&f> &l" + displayName);
      } else {
        middleLore.add("&7  " + displayName);
      }
    }

    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.COMPARATOR)
                    .name("&3&lOrder By")
                    .addLore("&7Change the sorting order of the items.")
                    .addLore()
                    .addLore(middleLore)
                    .addLore()
                    .addLore("&7( click to cycle )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              updateMenu(player, () -> menuData.cycleOrder(getOrderOptions()));
            });
  }

  /** Creates the "Direction" button, which toggles between ASCENDING and DESCENDING order. */
  private InventoryButton DirectionButton() {
    boolean isAscending = menuData.getDirection() == MenuData.Direction.ASCENDING;
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(isAscending ? Material.ENDER_PEARL : Material.ENDER_EYE)
                    .name("&3&lDirection")
                    .addLore("&7Change the sorting direction.")
                    .addLore()
                    .addLore(
                        isAscending
                            ? "&f&lASCENDING &8/ &7DESCENDING"
                            : "&7ASCENDING &8/ &f&lDESCENDING")
                    .addLore()
                    .addLore("&7( click to toggle )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              updateMenu(player, menuData::toggleDirection);
            });
  }

  /** Creates a single, powerful button to manage all available filters (both enum and predicate). */
  private InventoryButton FilterManagerButton() {
    return new InventoryButton()
        .creator(
            player -> {
              List<EnumFilter<T, ?>> enumFilters =
                  getEnumFilters() != null ? getEnumFilters() : List.of();
              List<PredicateFilter<T, ?>> predicateFilters =
                  getPredicateFilters() != null ? getPredicateFilters() : List.of();
              int totalFilters = enumFilters.size() + predicateFilters.size();
              int activeIndex = menuData.getActiveFilterIndex();

              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.HOPPER)
                      .name("&3&lFilters")
                      .addLore("&7Manage and apply filters to the list.")
                      .addLore("");

              // Add enum filters first
              for (int i = 0; i < enumFilters.size(); i++) {
                if (i == activeIndex) {
                  addExpandedFilterLore(builder, enumFilters.get(i));
                } else {
                  addCollapsedFilterLore(builder, enumFilters.get(i));
                }
              }

              // Add predicate filters after enum filters
              for (int i = 0; i < predicateFilters.size(); i++) {
                int combinedIndex = enumFilters.size() + i;
                if (combinedIndex == activeIndex) {
                  addExpandedPredicateFilterLore(builder, predicateFilters.get(i));
                } else {
                  addCollapsedPredicateFilterLore(builder, predicateFilters.get(i));
                }
              }

              builder
                  .addLore("")
                  .addLore("&bRight-Click: &7Next filter.")
                  .addLore("&bLeft-Click: &7Change filter option.");

              return builder.build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              List<EnumFilter<T, ?>> enumFilters =
                  getEnumFilters() != null ? getEnumFilters() : List.of();
              List<PredicateFilter<T, ?>> predicateFilters =
                  getPredicateFilters() != null ? getPredicateFilters() : List.of();
              int totalFilters = enumFilters.size() + predicateFilters.size();
              if (totalFilters == 0) return;

              if (event.isRightClick()) {
                menuData.cycleActiveFilter(totalFilters);
              } else {
                // Left-Click logic to cycle the active filter's value
                int activeIndex = menuData.getActiveFilterIndex();
                if (activeIndex < enumFilters.size()) {
                  // It's an enum filter
                  cycleActiveFilterValue(enumFilters.get(activeIndex));
                } else {
                  // It's a predicate filter
                  int predicateIndex = activeIndex - enumFilters.size();
                  cycleActivePredicateFilterValue(predicateFilters.get(predicateIndex));
                }
              }
              // Refresh the menu without resetting the page
              GUIManager.getInstance().openGUI(getNewMenuInstance(menuData, dataSet), player);
              player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            });
  }

  /** Adds the lore for a collapsed (inactive) filter to the builder. */
  private void addCollapsedFilterLore(ItemStackHelper.Builder builder, EnumFilter<T, ?> filter) {
    String currentSetting =
        menuData
            .getEnumFilter(filter.filterId())
            .map(val -> ((MenuData.IDisplayName) val).getDisplayName())
            .orElse("All");
    builder.addLore("&7  " + filter.filterName() + ": &f" + currentSetting);
  }

  /** Adds the lore for an expanded (active) filter, showing all its options. */
  private <E extends Enum<E> & MenuData.IDisplayName> void addExpandedFilterLore(
      ItemStackHelper.Builder builder, EnumFilter<T, E> filter) {

    Optional<Enum<?>> currentFilterValue = menuData.getEnumFilter(filter.filterId());
    builder.addLore(
        "&f > "
            + filter.filterName()
            + ": "
            + (currentFilterValue.isEmpty()
                ? "All"
                : ((MenuData.IDisplayName) currentFilterValue.get()).getDisplayName()));

    // "All" option
    if (currentFilterValue.isEmpty()) {
      builder.addLore("  &f > &lAll");
    } else {
      builder.addLore("  &7   All");
    }

    // Enum options
    for (E enumValue : filter.enumClass().getEnumConstants()) {
      if (currentFilterValue.isPresent() && currentFilterValue.get().equals(enumValue)) {
        builder.addLore("  &f > &l" + enumValue.getDisplayName());
      } else {
        builder.addLore("  &7   " + enumValue.getDisplayName());
      }
    }
  }

  /** Helper method to cycle through the values of the currently active filter. */
  private <E extends Enum<E> & MenuData.IDisplayName> void cycleActiveFilterValue(
      EnumFilter<T, E> filter) {
    E[] enumValues = filter.enumClass().getEnumConstants();
    if (enumValues.length == 0) return;

    Optional<Enum<?>> currentFilterValue = menuData.getEnumFilter(filter.filterId());

    if (currentFilterValue.isEmpty()) {
      // If current is "All", cycle to the first enum value
      menuData.setEnumFilter(filter.filterId(), enumValues[0]);
    } else {
      // Find the index of the current value in the array of all possible values.
      int currentIndex = -1;
      for (int i = 0; i < enumValues.length; i++) {
        if (enumValues[i].equals(currentFilterValue.get())) {
          currentIndex = i;
          break;
        }
      }
      if (currentIndex == -1 || currentIndex == enumValues.length - 1) {
        // If it's the last enum value, cycle back to "All"
        menuData.getEnumFilters().remove(filter.filterId());
      } else {
        // Cycle to the next enum value
        menuData.setEnumFilter(filter.filterId(), enumValues[currentIndex + 1]);
      }
    }
  }

  /** Adds the lore for a collapsed (inactive) predicate filter to the builder. */
  private void addCollapsedPredicateFilterLore(
      ItemStackHelper.Builder builder, PredicateFilter<T, ?> filter) {
    String currentSetting =
        menuData
            .getEnumFilter(filter.filterId())
            .map(val -> ((MenuData.IDisplayName) val).getDisplayName())
            .orElse("All");
    builder.addLore("&7  " + filter.filterName() + ": &f" + currentSetting);
  }

  /** Adds the lore for an expanded (active) predicate filter, showing all its options. */
  private <E extends Enum<E> & MenuData.IDisplayName> void addExpandedPredicateFilterLore(
      ItemStackHelper.Builder builder, PredicateFilter<T, E> filter) {

    Optional<Enum<?>> currentFilterValue = menuData.getEnumFilter(filter.filterId());
    builder.addLore(
        "&f > "
            + filter.filterName()
            + ": "
            + (currentFilterValue.isEmpty()
                ? "All"
                : ((MenuData.IDisplayName) currentFilterValue.get()).getDisplayName()));

    // "All" option
    if (currentFilterValue.isEmpty()) {
      builder.addLore("  &f > &lAll");
    } else {
      builder.addLore("  &7   All");
    }

    // Enum options
    for (E enumValue : filter.enumClass().getEnumConstants()) {
      if (currentFilterValue.isPresent() && currentFilterValue.get().equals(enumValue)) {
        builder.addLore("  &f > &l" + enumValue.getDisplayName());
      } else {
        builder.addLore("  &7   " + enumValue.getDisplayName());
      }
    }
  }

  /** Helper method to cycle through the values of the currently active predicate filter. */
  private <E extends Enum<E> & MenuData.IDisplayName> void cycleActivePredicateFilterValue(
      PredicateFilter<T, E> filter) {
    E[] enumValues = filter.enumClass().getEnumConstants();
    if (enumValues.length == 0) return;

    Optional<Enum<?>> currentFilterValue = menuData.getEnumFilter(filter.filterId());

    if (currentFilterValue.isEmpty()) {
      // If current is "All", cycle to the first enum value
      menuData.setEnumFilter(filter.filterId(), enumValues[0]);
    } else {
      // Find the index of the current value in the array of all possible values.
      int currentIndex = -1;
      for (int i = 0; i < enumValues.length; i++) {
        if (enumValues[i].equals(currentFilterValue.get())) {
          currentIndex = i;
          break;
        }
      }
      if (currentIndex == -1 || currentIndex == enumValues.length - 1) {
        // If it's the last enum value, cycle back to "All"
        menuData.getEnumFilters().remove(filter.filterId());
      } else {
        // Cycle to the next enum value
        menuData.setEnumFilter(filter.filterId(), enumValues[currentIndex + 1]);
      }
    }
  }

  /** Creates the "Create" button, which opens an Anvil GUI for user input. */
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

              // Check if we should skip the anvil
              if (skipCreateAnvil()) {
                onCreateButtonClick(event, menuData);
                return;
              }

              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Create")
                  .itemLeft(ItemStackHelper.builder(Material.PAPER).name("id").build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText().trim();
                          if (textEntry.isBlank()) {
                            player.sendMessage(TextUtil.format("&cMust enter an id!"));
                            player.playSound(player.getLocation(), getDenySound(), 0.5f, 0.75f);
                            GUIManager.getInstance()
                                .openGUI(getNewMenuInstance(menuData, dataSet), player);
                            return Collections.emptyList();
                          }
                          if (isDuplicate(textEntry)) {
                            player.sendMessage(
                                TextUtil.format("&cAn entry with that ID already exists!"));
                            player.playSound(player.getLocation(), getDenySound(), 0.5f, 0.75f);
                            return Collections.emptyList();
                          }
                          textEntry = sanitizeText(textEntry);
                          createNewData(textEntry, event, menuData).run();
                          return Collections.emptyList();
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  public String sanitizeText(String text) {
    return text.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
  }

  /**
   * Creates the "Search" button, which opens an Anvil GUI for query input or clears the current
   * search.
   */
  private InventoryButton SearchButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.ANVIL)
                      .name("&3&lSearch")
                      .addLore("&7Enter a search query to find something specific.")
                      .addLore("");
              if (menuData.getSearch() != null && !menuData.getSearch().isBlank()) {
                builder
                    .addLore("&7Query: &f" + menuData.getSearch())
                    .addLore("")
                    .addLore("&7( click to clear )");
              } else {
                builder.addLore("&7Query: &fNone").addLore("").addLore("&7( click to search )");
              }
              return builder.build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.75f);
              if (menuData.getSearch() != null && !menuData.getSearch().isBlank()) {
                updateMenu(player, () -> menuData.setSearch(""));
                return;
              }
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Search")
                  .itemLeft(ItemStackHelper.builder(Material.PAPER).name(" ").build())
                  .onClick((slot, stateSnapshot) -> handleSearchInput(slot, stateSnapshot, player))
                  .open(player);
            });
  }

  /**
   * Creates the "Back" button, which navigates to the previous page or triggers the custom back
   * action.
   */
  private InventoryButton BackButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to the previous page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page > 1) {
                menuData.setPage(page - 1);
                GUIManager.getInstance().openGUI(getNewMenuInstance(menuData, dataSet), player);
              } else {
                // This handles the case where page is 1 but a back action is defined
                if (backButton() != null) {
                  backButton().accept(event);
                }
              }
            });
  }

  /** Creates the "Next" button, which navigates to the next page. */
  private InventoryButton NextButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&a&lNext")
                    .addLore("&7Click to go to the next page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (menuData.getPage() < getMaxPage()) {
                menuData.setPage(menuData.getPage() + 1);
              }
              GUIManager.getInstance().openGUI(getNewMenuInstance(menuData, dataSet), player);
            });
  }

  /**
   * A helper method to perform an action, reset the page to 1, and refresh the menu.
   *
   * @param player The player viewing the menu.
   * @param action The {@link Runnable} action to perform before refreshing (e.g., changing a
   *     filter).
   */
  private void updateMenu(Player player, Runnable action) {
    action.run();
    menuData.setPage(1);
    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
    GUIManager.getInstance().openGUI(getNewMenuInstance(menuData, dataSet), player);
  }

  /** Handles the input from the search Anvil GUI. */
  private List<AnvilGUI.ResponseAction> handleSearchInput(
      int slot, AnvilGUI.StateSnapshot stateSnapshot, Player player) {
    if (slot == AnvilGUI.Slot.OUTPUT) {
      String textEntry = stateSnapshot.getText().trim();
      menuData.setSearch(textEntry);
      menuData.setPage(1);
      // We need to schedule the GUI opening for the next tick to avoid conflicts with AnvilGUI
      // closing.
      Bukkit.getScheduler()
          .runTask(
              Main.getInstance(),
              () -> {
                GUIManager.getInstance().openGUI(getNewMenuInstance(menuData, dataSet), player);
                player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 1f);
              });
    }
    return Collections.emptyList();
  }

  // ========== Dynamic Update Methods ==========

  /**
   * Refreshes only the data buttons in the menu without recreating the inventory. This is efficient
   * for updating item displays when the underlying data changes, but doesn't handle size changes
   * (when items are added/removed from the dataset).
   *
   * <p>Use this when you know the number of items hasn't changed significantly, but their display
   * needs to update (e.g., lobby status changes).
   */
  public void refreshDataButtons() {
    Player player;
    try {
      player = getRegisteredPlayer();
    } catch (IllegalStateException e) {
      stopAutoRefresh();
      return;
    }

    if (player == null || !player.isOnline()) {
      stopAutoRefresh();
      return;
    }

    // Clear existing data buttons (slots 9+)
    getButtonMap().entrySet().removeIf(entry -> entry.getKey() >= 9);

    // Re-process data
    Set<T> searchFiltered = applySearch(preprocessData(dataSet));
    Set<T> filtered = applyEnumFilters(searchFiltered);
    LinkedHashSet<T> ordered = applyOrder(filtered, menuData);
    LinkedHashSet<T> directionApplied = applyDirection(ordered);
    LinkedHashSet<T> pageAdjusted = applyPage(directionApplied);

    // Re-add data buttons
    int slot = 9;
    for (T data : pageAdjusted) {
      addButton(slot++, DataButton(data));
    }

    // Refresh the button visuals
    refreshButtons();
  }

  /**
   * Performs a full menu refresh, handling potential size changes. This reopens the menu with
   * updated data, which is necessary when items are added or removed from the dataset.
   *
   * <p>Use this when the dataset size might have changed (e.g., lobbies created/deleted).
   */
  public void refreshMenu() {
    Player player;
    try {
      player = getRegisteredPlayer();
    } catch (IllegalStateException e) {
      stopAutoRefresh();
      return;
    }

    if (player == null || !player.isOnline()) {
      stopAutoRefresh();
      return;
    }

    // Stop auto-refresh on this instance before opening new one
    stopAutoRefresh();

    GUIManager.getInstance().openGUI(getNewMenuInstance(menuData, dataSet), player);
  }

  /**
   * Starts automatic refreshing of this menu at the configured interval. Only activates if
   * auto-refresh is enabled in the MenuConfig.
   *
   * <p>This is automatically called when the menu is opened if configured.
   */
  public void startAutoRefresh() {
    if (!config.isAutoRefreshEnabled()) {
      return;
    }

    // Stop existing task if any
    stopAutoRefresh();

    autoRefreshTask =
        Bukkit.getScheduler()
            .runTaskTimer(
                Main.getInstance(),
                this::autoRefreshTick,
                config.getAutoRefreshIntervalTicks(),
                config.getAutoRefreshIntervalTicks());
  }

  /**
   * Internal method called by the auto-refresh timer. Uses full refresh to pick up any dataset
   * changes (items added/removed).
   */
  private void autoRefreshTick() {
    Player player;
    try {
      player = getRegisteredPlayer();
    } catch (IllegalStateException e) {
      stopAutoRefresh();
      return;
    }

    if (player == null || !player.isOnline()) {
      stopAutoRefresh();
      return;
    }

    // Use full refresh to ensure we pick up dataset changes
    // (e.g., lobbies being created/removed)
    BasicMenu<T> newInstance = getNewMenuInstance(menuData, dataSet);

    // Don't stop auto-refresh here since the new instance will start its own
    GUIManager.getInstance().openGUI(newInstance, player);
  }

  /** Stops the automatic refresh task for this menu. */
  public void stopAutoRefresh() {
    if (autoRefreshTask != null && !autoRefreshTask.isCancelled()) {
      autoRefreshTask.cancel();
      autoRefreshTask = null;
    }
  }

  /**
   * Refreshes all open menus of a specific type. Useful for globally updating all players viewing a
   * particular menu when data changes.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * // When a lobby is created/destroyed, refresh all LobbiesMenu instances
   * BasicMenu.refreshAllMenusOfType(LobbiesMenu.class);
   * }</pre>
   *
   * @param menuClass The class of menus to refresh
   */
  public static void refreshAllMenusOfType(Class<? extends BasicMenu> menuClass) {
    openMenus.values().stream()
        .filter(menu -> menuClass.isInstance(menu))
        .forEach(BasicMenu::refreshMenu);
  }

  /**
   * Refreshes data buttons for all open menus of a specific type. More efficient than full refresh
   * when only button displays need updating.
   *
   * @param menuClass The class of menus to refresh
   */
  public static void refreshDataButtonsForType(Class<? extends BasicMenu> menuClass) {
    openMenus.values().stream()
        .filter(menu -> menuClass.isInstance(menu))
        .forEach(BasicMenu::refreshDataButtons);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    super.onOpen(event);
    Player player = (Player) event.getPlayer();

    // Stop auto-refresh for any previously open menu by this player
    BasicMenu<?> previousMenu = openMenus.get(player);
    if (previousMenu != null && previousMenu != this) {
      previousMenu.stopAutoRefresh();
    }

    // Register this menu as the active one for this player
    openMenus.put(player, this);

    // Start auto-refresh if enabled
    if (config.isAutoRefreshEnabled()) {
      startAutoRefresh();
    }
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    super.onClose(event);
    Player player = (Player) event.getPlayer();

    // Only remove from openMenus if this is still the active menu for this player
    if (openMenus.get(player) == this) {
      openMenus.remove(player);
    }

    // Stop auto-refresh
    stopAutoRefresh();
  }
}
