package me.mortaldev.jbcreditshop.modules.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.transaction.data.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class TransactionLog implements CRUD.Identifiable {

  private final LocalDate date;
  private final HashMap<String, Transaction> transactions; // <timestamp, transaction>

  @JsonCreator
  public TransactionLog(
      @JsonProperty("date") LocalDate date,
      @JsonProperty("transactions") HashMap<String, Transaction> transactions) {
    this.date = date == null ? Main.getLocalDate() : date;
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

  public String getTimestamp() {
    return Main.getLocalDate().format(DateTimeFormatter.ISO_LOCAL_TIME);
  }

  public HashMap<String, Transaction> getTransactions() {
    return transactions;
  }

  @Override
  public String getID() {
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
