package me.mortaldev.jbcreditshop.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
  private static final HashMap<String, Integer> scheduledTasks = new HashMap<>();
  private static final HashMap<UUID, Consumer<Component>> consumers = new HashMap<>();

  public static void addConsumer(UUID uuid, Consumer<Component> consumer) {
    consumers.put(uuid, consumer);
  }

  public static void removeConsumer(UUID uuid) {
    consumers.remove(uuid);
  }

  public static boolean hasConsumer(UUID uuid) {
    return consumers.containsKey(uuid);
  }

  public static void makeRequest(Player player, Long timeOut, Consumer<Component> consumer) {
    if (hasConsumer(player.getUniqueId())) {
      removeConsumer(player.getUniqueId());
      Bukkit.getScheduler().cancelTask(scheduledTasks.get("task-" + player.getUniqueId()));
    }
    addConsumer(player.getUniqueId(), consumer);
    scheduledTasks.put(
        "task-" + player.getUniqueId(),
        Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
                Main.getInstance(),
                () -> {
                  if (!scheduledTasks.containsKey("task-" + player.getUniqueId())) {
                    return;
                  }
                  player.sendMessage(TextUtil.format("&cRequest timed out."));
                  removeConsumer(player.getUniqueId());
                },
                timeOut));
  }

  @EventHandler
  public void onChat(AsyncChatEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (consumers.containsKey(uuid)) {
      Bukkit.getScheduler()
          .runTask(
              Main.getInstance(),
              () -> {
                consumers.remove(uuid).accept(event.originalMessage());
                if (scheduledTasks.containsKey("task-" + uuid)) {
                  Bukkit.getScheduler().cancelTask(scheduledTasks.remove("task-" + uuid));
                }
              });
      event.setCancelled(true);
    }
  }
}
