package me.mortaldev.jbcreditshop.modules.shopstats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.shopstats.data.ShopItemStat;
import me.mortaldev.jbcreditshop.modules.shopstats.data.StatsHolder;

public record ShopStats(HashMap<LocalDate, StatsHolder> stats) {
  @JsonCreator
  public ShopStats(@JsonProperty("stats") HashMap<LocalDate, StatsHolder> stats) {
    this.stats = stats == null ? new HashMap<>() : stats;
  }

  @JsonIgnore
  public StatsHolder getStats(LocalDate date) {
    if (!stats.containsKey(date)) {
      return null;
    }
    return stats.get(date);
  }

  public void addPurchase(String itemID) {
    LocalDate localDate = Main.getLocalDateTime().toLocalDate();
    StatsHolder statsHolder;
    if (stats.containsKey(localDate)) {
      statsHolder = stats.get(localDate);
    } else {
      statsHolder = new StatsHolder(new HashSet<>());
      stats.put(localDate, statsHolder);
    }
    ShopItemStat stat = statsHolder.getStat(itemID);
    stat.addPurchaseCount(1);
    statsHolder.addStat(stat);
    stats.put(localDate, statsHolder);
    ShopStatsCRUD.getInstance().save(this);
  }
}
