package org.example.jbcreditshop.ecobits;

import com.willfp.ecobits.currencies.Currencies;
import com.willfp.ecobits.currencies.Currency;
import com.willfp.ecobits.currencies.CurrencyUtils;
import java.math.BigDecimal;
import org.bukkit.OfflinePlayer;

public class EcoBitsCurrency {

  private final OfflinePlayer player;
  private static final String CURRENCY_NAME = "credits";
  private final Currency currency;

  public EcoBitsCurrency(OfflinePlayer player) {
    this.player = player;
    currency = Currencies.getByID(CURRENCY_NAME);
  }

  public BigDecimal getCurrentBalance() {
    return CurrencyUtils.getBalance(player, currency);
  }

  public void adjustCurrentBalance(BigDecimal amount) {
    CurrencyUtils.adjustBalance(player, currency, amount);
  }

  public void add(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      return;
    }
    adjustCurrentBalance(amount);
  }

  public void remove(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      return;
    }
    adjustCurrentBalance(amount.multiply(BigDecimal.valueOf(-1)));
  }
}
