package me.mortaldev.jbcreditshop.menus.basicmenu;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MenuData implements Serializable {
  // Added Serializable for potential state saving in the future.

  private String search = "";
  private int page = 1;
  private String orderId;
  private Direction direction = Direction.ASCENDING;
  private Map<String, Enum<?>> enumFilters = new HashMap<>();
  private int activeFilterIndex = 0;

  public void reset() {
    this.search = "";
    this.page = 1;
    this.orderId = null;
    this.direction = Direction.ASCENDING;
    this.enumFilters = new HashMap<>();
    this.activeFilterIndex = 0;
  }

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  // Add getters and setters for 'order'
  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Map<String, Enum<?>> getEnumFilters() {
    return enumFilters;
  }

  public void setEnumFilters(Map<String, Enum<?>> enumFilters) {
    this.enumFilters = enumFilters;
  }

  public Optional<Enum<?>> getEnumFilter(String filterId) {
    return Optional.ofNullable(enumFilters.get(filterId));
  }

  public void setEnumFilter(String filterId, Enum<?> value) {
    enumFilters.put(filterId, value);
  }

  // Add a helper method to cycle to the next sorting option
  public <T> void cycleOrder(List<OrderOption<T>> options) {
    if (options == null || options.isEmpty()) {
      this.orderId = null;
      return;
    }
    if (this.orderId == null) {
      this.orderId = options.get(0).id();
      return;
    }
    int currentIndex = options.stream().map(OrderOption::id).toList().indexOf(this.orderId);

    // Cycle to the next one, or wrap around to the start
    int nextIndex = (currentIndex + 1) % options.size();
    this.orderId = options.get(nextIndex).id();
  }

  public int getActiveFilterIndex() {
    return activeFilterIndex;
  }

  public void setActiveFilterIndex(int activeFilterIndex) {
    this.activeFilterIndex = activeFilterIndex;
  }

  public void cycleActiveFilter(int totalFilters) {
    if (totalFilters > 0) {
      this.activeFilterIndex = (this.activeFilterIndex + 1) % totalFilters;
    }
  }

  public void toggleDirection() {
    this.direction =
        (this.direction == Direction.ASCENDING) ? Direction.DESCENDING : Direction.ASCENDING;
  }

  public enum Direction {
    ASCENDING,
    DESCENDING
  }

  public interface IDisplayName {
    String getDisplayName();
  }
}
