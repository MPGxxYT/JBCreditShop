package me.mortaldev.jbcreditshop.modules;

public class MenuData {

  private String searchQuery = "";
  private OrderBy orderBy = OrderBy.NAME;
  private Direction direction = Direction.ASCENDING;
  private Filter filter = Filter.NONE;
  private int page;

  public MenuData() {
    this.page = 1;
  }

  public String getSearchQuery() {
    return searchQuery;
  }

  public void setSearchQuery(String searchQuery) {
    this.searchQuery = searchQuery;
  }

  public OrderBy getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public enum Filter {
    NONE,
    UNLOCKED,
    OWNED,
    UNOWNED;

    public Filter getNext(Filter value) {
      return switch (value) {
        case NONE -> UNLOCKED;
        case UNLOCKED -> OWNED;
        case OWNED -> UNOWNED;
        case UNOWNED -> NONE;
      };
    }
  }

  public enum Direction {
    ASCENDING,
    DESCENDING;

    public static Direction getNext(Direction value) {
      return switch (value) {
        case ASCENDING -> DESCENDING;
        case DESCENDING -> ASCENDING;
      };
    }
  }

  public enum OrderBy {
    PRICE,
    NAME,
    GROUP;

    public static OrderBy getNext(OrderBy value) {
      return switch (value) {
        case PRICE -> NAME;
        case NAME -> GROUP;
        case GROUP -> PRICE;
      };
    }
  }
}
