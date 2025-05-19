package me.mortaldev.jbcreditshop.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import me.mortaldev.jbcreditshop.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
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

  @EventHandler
  public void onChat(AsyncChatEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (consumers.containsKey(uuid)) {
      Bukkit.getScheduler()
          .runTask(Main.getInstance(), () -> consumers.remove(uuid).accept(event.message()));
      event.setCancelled(true);
    }
  }
}
