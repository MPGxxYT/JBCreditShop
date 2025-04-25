package org.example.jbcreditshop.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import me.mortaldev.YAML;
import org.bukkit.configuration.file.FileConfiguration;
import org.example.jbcreditshop.Main;

public class YamlReader {
  
  private static class Singleton {
    private static final YamlReader INSTANCE = new YamlReader();
  }
  
  public static synchronized YamlReader getInstance() {
    return Singleton.INSTANCE;
  }
  
  private YamlReader() {}


  public Set<FileConfiguration> getConfigs(String path) {
    File dir = new File(Main.getInstance().getDataFolder() + path);
    if (!dir.exists()) {
      return new HashSet<>();
    }
    File[] files = dir.listFiles();
    if (files == null) {
      return new HashSet<>();
    }
    Set<FileConfiguration> configs = new HashSet<>();
    for (File file : files) {
      YAML yaml = YAML.getInstance();
      yaml.setMain(Main.getInstance());
      FileConfiguration config = yaml.getConfig(path + file.getName());
      if (config == null) {
        continue;
      }
      configs.add(config);
    }
    Main.log("Found " + configs.size() + " config(s) in " + path);
    return configs;
  }
  
}
