package org.example.jbcreditshop.modules;

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
    LOCKED,
    OWNED,
    UNOWNED
  }

  public enum Direction {
    ASCENDING,
    DESCENDING
  }

  public enum OrderBy {
    PRICE,
    NAME,
    GROUP
  }

}

