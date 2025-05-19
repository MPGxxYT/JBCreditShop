package me.mortaldev.jbcreditshop.modules.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.transaction.data.Transaction;

/**
 * @param transactions <timestamp, transaction>
 */
public record TransactionLog(
    @JsonProperty("date") LocalDateTime date, HashMap<String, Transaction> transactions)
    implements CRUD.Identifiable {

  @JsonCreator
  public TransactionLog(
      @JsonProperty("date") LocalDateTime date,
      @JsonProperty("transactions") HashMap<String, Transaction> transactions) {
    this.date = date == null ? Main.getLocalDateTime() : date;
    this.transactions = transactions == null ? new HashMap<>() : transactions;
  }

  public void addTransaction(Transaction transaction) {
    transactions.put(getTimestamp(), transaction);
  }

  public void removeTransaction(String timestamp) {
    transactions.remove(timestamp);
  }

  public void removeTransaction(Transaction transaction) {
    transactions.entrySet().removeIf(entry -> entry.getValue().equals(transaction));
  }

  @Override
  public LocalDateTime date() {
    return date;
  }

  @JsonIgnore
  public String getTimestamp() {
    return Main.getLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
  }

  @Override
  @JsonIgnore
  public String getID() {
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
