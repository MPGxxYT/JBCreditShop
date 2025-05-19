package me.mortaldev.jbcreditshop.yaml;

import java.io.File;
import java.util.HashMap;
import me.mortaldev.YAML;
import me.mortaldev.jbcreditshop.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class YamlReader {

  private static class Singleton {
    private static final YamlReader INSTANCE = new YamlReader();
  }

  public static synchronized YamlReader getInstance() {
    return Singleton.INSTANCE;
  }

  private YamlReader() {}

  public HashMap<FileConfiguration, String> getConfigs(String path) {
    File dir = new File(Main.getInstance().getDataFolder() + path);
    if (!dir.exists()) {
      return new HashMap<>();
    }
    File[] files = dir.listFiles();
    if (files == null) {
      return new HashMap<>();
    }
    HashMap<FileConfiguration, String> configs = new HashMap<>();
    for (File file : files) {
      YAML yaml = Main.getYAML();
      FileConfiguration config = yaml.getConfig(path + file.getName());
      if (config == null) {
        continue;
      }
      configs.put(config, path + file.getName());
    }
    Main.log("Found " + configs.size() + " config(s) in " + path);
    return configs;
  }
}
