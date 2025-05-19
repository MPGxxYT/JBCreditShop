package me.mortaldev.jbcreditshop.modules.transaction;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.transaction.data.Transaction;

public class TransactionLogManager extends CRUDManager<TransactionLog> {
  private static class Singleton {
    private static final TransactionLogManager INSTANCE = new TransactionLogManager();
  }

  public static synchronized TransactionLogManager getInstance() {
    return Singleton.INSTANCE;
  }

  private TransactionLogManager() {}

  @Override
  public CRUD<TransactionLog> getCRUD() {
    return TransactionLogCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }

  public TransactionLog getTodayLog() {
    return getByID(Main.getLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
        .orElse(new TransactionLog(Main.getLocalDateTime(), new HashMap<>()));
  }

  public void addTransaction(Transaction transaction) {
    TransactionLog todayLog = getTodayLog();
    todayLog.addTransaction(transaction);
    update(todayLog, true);
  }
}
