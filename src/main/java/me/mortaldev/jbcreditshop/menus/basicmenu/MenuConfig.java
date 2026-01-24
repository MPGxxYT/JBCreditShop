package me.mortaldev.jbcreditshop.menus.basicmenu;

/**
 * Configuration class for BasicMenu features and button placement.
 *
 * <p>Use the builder pattern to selectively enable features and customize slot positions.
 *
 * <p><b>Example:</b>
 *
 * <pre>{@code
 * MenuConfig config = MenuConfig.builder()
 *     .enableSearch(true).searchSlot(5)
 *     .enableOrder(true).orderSlot(3)
 *     .enableFilters(false)
 *     .enableCreate(false)
 *     .build();
 * }</pre>
 */
public class MenuConfig {

  // Feature toggles
  private final boolean backEnabled;
  private final boolean searchEnabled;
  private final boolean orderEnabled;
  private final boolean directionEnabled;
  private final boolean filtersEnabled;
  private final boolean createEnabled;
  private final boolean paginationEnabled;
  private final boolean autoRefreshEnabled;

  // Button slots (0-8 for top row)
  private final int backSlot;
  private final int searchSlot;
  private final int orderSlot;
  private final int directionSlot;
  private final int filtersSlot;
  private final int createSlot;
  private final int nextSlot;

  // Auto-refresh settings
  private final long autoRefreshIntervalTicks;

  private MenuConfig(Builder builder) {
    this.backEnabled = builder.backEnabled;
    this.searchEnabled = builder.searchEnabled;
    this.orderEnabled = builder.orderEnabled;
    this.directionEnabled = builder.directionEnabled;
    this.filtersEnabled = builder.filtersEnabled;
    this.createEnabled = builder.createEnabled;
    this.paginationEnabled = builder.paginationEnabled;
    this.autoRefreshEnabled = builder.autoRefreshEnabled;

    this.backSlot = builder.backSlot;
    this.searchSlot = builder.searchSlot;
    this.orderSlot = builder.orderSlot;
    this.directionSlot = builder.directionSlot;
    this.filtersSlot = builder.filtersSlot;
    this.createSlot = builder.createSlot;
    this.nextSlot = builder.nextSlot;

    this.autoRefreshIntervalTicks = builder.autoRefreshIntervalTicks;
  }

  /**
   * Creates a default configuration with all features enabled and standard slot positions.
   *
   * @return A MenuConfig with all features enabled
   */
  public static MenuConfig defaultConfig() {
    return builder().build();
  }

  /**
   * Creates a minimal configuration with only back/next pagination.
   *
   * @return A MenuConfig with only pagination enabled
   */
  public static MenuConfig minimalConfig() {
    return builder()
        .enableBack(false)
        .enableSearch(false)
        .enableOrder(false)
        .enableDirection(false)
        .enableFilters(false)
        .enableCreate(false)
        .build();
  }

  /**
   * Creates a configuration with all features disabled.
   *
   * @return A MenuConfig with all features disabled
   */
  public static MenuConfig disabledConfig() {
    return builder()
        .enableBack(false)
        .enableSearch(false)
        .enableOrder(false)
        .enableDirection(false)
        .enableFilters(false)
        .enableCreate(false)
        .enablePagination(false)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a builder pre-populated with this config's values. Allows you to easily create
   * variations of existing configurations.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * MenuConfig base = MenuConfig.disabledConfig();
   * MenuConfig custom = base.toBuilder()
   *     .enableSearch(true)
   *     .enablePagination(true)
   *     .build();
   * }</pre>
   *
   * @return A new Builder with this config's current values
   */
  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.backEnabled = this.backEnabled;
    builder.searchEnabled = this.searchEnabled;
    builder.orderEnabled = this.orderEnabled;
    builder.directionEnabled = this.directionEnabled;
    builder.filtersEnabled = this.filtersEnabled;
    builder.createEnabled = this.createEnabled;
    builder.paginationEnabled = this.paginationEnabled;
    builder.autoRefreshEnabled = this.autoRefreshEnabled;

    builder.backSlot = this.backSlot;
    builder.searchSlot = this.searchSlot;
    builder.orderSlot = this.orderSlot;
    builder.directionSlot = this.directionSlot;
    builder.filtersSlot = this.filtersSlot;
    builder.createSlot = this.createSlot;
    builder.nextSlot = this.nextSlot;

    builder.autoRefreshIntervalTicks = this.autoRefreshIntervalTicks;

    return builder;
  }

  // Getters
  public boolean isBackEnabled() {
    return backEnabled;
  }

  public boolean isSearchEnabled() {
    return searchEnabled;
  }

  public boolean isOrderEnabled() {
    return orderEnabled;
  }

  public boolean isDirectionEnabled() {
    return directionEnabled;
  }

  public boolean isFiltersEnabled() {
    return filtersEnabled;
  }

  public boolean isCreateEnabled() {
    return createEnabled;
  }

  public boolean isPaginationEnabled() {
    return paginationEnabled;
  }

  public boolean isAutoRefreshEnabled() {
    return autoRefreshEnabled;
  }

  public int getBackSlot() {
    return backSlot;
  }

  public int getSearchSlot() {
    return searchSlot;
  }

  public int getOrderSlot() {
    return orderSlot;
  }

  public int getDirectionSlot() {
    return directionSlot;
  }

  public int getFiltersSlot() {
    return filtersSlot;
  }

  public int getCreateSlot() {
    return createSlot;
  }

  public int getNextSlot() {
    return nextSlot;
  }

  public long getAutoRefreshIntervalTicks() {
    return autoRefreshIntervalTicks;
  }

  public static class Builder {
    private boolean backEnabled = true;
    private boolean searchEnabled = true;
    private boolean orderEnabled = true;
    private boolean directionEnabled = true;
    private boolean filtersEnabled = true;
    private boolean createEnabled = true;
    private boolean paginationEnabled = true;
    private boolean autoRefreshEnabled = false;

    private int backSlot = 0;
    private int searchSlot = 5;
    private int orderSlot = 3;
    private int directionSlot = 4;
    private int filtersSlot = 2;
    private int createSlot = 6;
    private int nextSlot = 8;

    private long autoRefreshIntervalTicks = 20L; // 1 second default

    public Builder enableBack(boolean enabled) {
      this.backEnabled = enabled;
      return this;
    }

    public Builder enableSearch(boolean enabled) {
      this.searchEnabled = enabled;
      return this;
    }

    public Builder enableOrder(boolean enabled) {
      this.orderEnabled = enabled;
      return this;
    }

    public Builder enableDirection(boolean enabled) {
      this.directionEnabled = enabled;
      return this;
    }

    public Builder enableFilters(boolean enabled) {
      this.filtersEnabled = enabled;
      return this;
    }

    public Builder enableCreate(boolean enabled) {
      this.createEnabled = enabled;
      return this;
    }

    public Builder enablePagination(boolean enabled) {
      this.paginationEnabled = enabled;
      return this;
    }

    public Builder enableAutoRefresh(boolean enabled) {
      this.autoRefreshEnabled = enabled;
      return this;
    }

    public Builder autoRefreshIntervalTicks(long ticks) {
      if (ticks <= 0) {
        throw new IllegalArgumentException("Auto-refresh interval must be positive, got: " + ticks);
      }
      this.autoRefreshIntervalTicks = ticks;
      return this;
    }

    public Builder backSlot(int slot) {
      this.backSlot = validateSlot(slot);
      return this;
    }

    public Builder searchSlot(int slot) {
      this.searchSlot = validateSlot(slot);
      return this;
    }

    public Builder orderSlot(int slot) {
      this.orderSlot = validateSlot(slot);
      return this;
    }

    public Builder directionSlot(int slot) {
      this.directionSlot = validateSlot(slot);
      return this;
    }

    public Builder filtersSlot(int slot) {
      this.filtersSlot = validateSlot(slot);
      return this;
    }

    public Builder createSlot(int slot) {
      this.createSlot = validateSlot(slot);
      return this;
    }

    public Builder nextSlot(int slot) {
      this.nextSlot = validateSlot(slot);
      return this;
    }

    private int validateSlot(int slot) {
      if (slot < 0 || slot > 8) {
        throw new IllegalArgumentException(
            "Slot must be between 0 and 8 (inclusive), got: " + slot);
      }
      return slot;
    }

    public MenuConfig build() {
      return new MenuConfig(this);
    }
  }
}
