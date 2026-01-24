package me.mortaldev.jbcreditshop.menus.basicmenu;

import java.util.function.BiPredicate;
import java.util.function.Function;
import org.bukkit.entity.Player;

/**
 * Represents a filter based on predicates that require player context. Unlike {@link EnumFilter}
 * which extracts an enum property from data objects, PredicateFilter applies condition-based
 * filtering where each enum option represents a different predicate to test against.
 *
 * <p>Use this when filtering depends on external state like player permissions, purchase history,
 * or other player-specific conditions.
 *
 * <p><b>Example:</b>
 *
 * <pre>{@code
 * new PredicateFilter<>("ownership", "Filter", OwnershipFilter.class, option -> {
 *     return switch (option) {
 *         case NONE -> null;  // show all
 *         case OWNED -> (item, player) -> isOwnedByPlayer(item, player);
 *         case UNOWNED -> (item, player) -> !isOwnedByPlayer(item, player);
 *         case UNLOCKED -> (item, player) -> !item.isLocked();
 *     };
 * })
 * }</pre>
 *
 * @param <T> The type of the data object being filtered.
 * @param <E> The enum type representing filter options.
 */
public record PredicateFilter<T, E extends Enum<E> & MenuData.IDisplayName>(
    String filterId,
    String filterName,
    Class<E> enumClass,
    Function<E, BiPredicate<T, Player>> predicateSupplier) {

  /**
   * @param filterId A unique identifier for the filter (e.g., "ownership").
   * @param filterName The display name for the filter button (e.g., "Filter").
   * @param enumClass The class of the enum representing filter options.
   * @param predicateSupplier A function that takes an enum option and returns a BiPredicate to test
   *     items. Return null for options that should show all items (no filtering).
   */
  public PredicateFilter {}
}
