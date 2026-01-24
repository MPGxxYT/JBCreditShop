package me.mortaldev.jbcreditshop.modules.transaction;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.logging.Logger;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.crudapi.loading.CRUDRegistry;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.modules.transaction.data.Transaction;

public class TransactionLogManager extends CRUDManager<TransactionLog> {
  private final Logger logger;

  public TransactionLogManager(CRUD<TransactionLog> crud, CRUDRegistry registry, Logger logger) {
    super(crud, registry);
    this.logger = logger;
  }

  @Override
  public TransactionLog getNewInstance(String string) {
    return new TransactionLog(Main.getLocalDateTime(), new HashMap<>());
  }

  @Override
  public void log(String string) {
    logger.info(string);
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
