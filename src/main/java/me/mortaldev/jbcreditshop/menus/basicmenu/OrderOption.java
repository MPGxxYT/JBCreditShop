package me.mortaldev.jbcreditshop.menus.basicmenu;

import java.util.Comparator;

/**
 * Represents a sorting option for a BasicMenu. This class encapsulates the logic and properties
 * required to sort a dataset of type T.
 *
 * @param <T> The type of the data object being sorted.
 */
public record OrderOption<T>(String id, String displayName, Comparator<T> comparator) {

  /**
   * @param id A unique identifier for the sorting option (e.g., "alphabetical").
   * @param displayName The display name for the order button (e.g., "Alphabetical").
   * @param comparator The comparator that defines the sorting logic.
   */
  public OrderOption {}
}
