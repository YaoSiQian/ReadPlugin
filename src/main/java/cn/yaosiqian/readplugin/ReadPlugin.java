package cn.yaosiqian.readplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ReadPlugin extends JavaPlugin {

    private FileConfiguration config;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        getLogger().info("ReadPlugin has been enabled!");
        saveDefaultConfig();
        config = getConfig();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    @Override
    public void onDisable() {
        getLogger().info("ReadPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("read")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 1) {
                    String fileName = args[0];
                    File folder = new File(getDataFolder(), "files");
                    File file = new File(folder, fileName);
                    if (!file.exists()) {
                        player.sendMessage(messages.getString("file-not-exist"));
                        getLogger().warning(messages.getString("file-not-exist"));
                        return true;
                    }
                    if (!file.isFile()) {
                        player.sendMessage(messages.getString("file-not-file"));
                        getLogger().warning(messages.getString("file-not-file"));
                        return true;
                    }
                    try {
                        String content = readFile(file, config);
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        meta.setTitle(fileName);
                        List<String> pages = new ArrayList<>();
                        String[] lines = content.split("\n");
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < lines.length; i++) {
                            String line = lines[i];
                            if (builder.length() + line.length() + 1 > 256) {
                                pages.add(builder.toString());
                                builder = new StringBuilder();
                            }
                            builder.append(line);
                            builder.append("\n");
                        }
                        if (builder.length() > 0) {
                            pages.add(builder.toString());
                        }
                        meta.setPages(pages);
                        book.setItemMeta(meta);
                        player.openBook(book);
                    } catch (IOException e) {
                        player.sendMessage(messages.getString("file-open-error"));
                        getLogger().warning(messages.getString("file-open-error"));
                        return true;
                    }
                    return true;
                } else {
                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta meta = (BookMeta) book.getItemMeta();
                    meta.setTitle(messages.getString("book-title"));
                    List<String> pages = new ArrayList<>();
                    File folder = new File(getDataFolder(), "files");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    if (!folder.isDirectory()) {
                        player.sendMessage(messages.getString("folder-create-error"));
                        getLogger().warning(messages.getString("folder-create-error"));
                        return true;
                    }
                    File[] files = folder.listFiles();
                    if (files != null) {
                        boolean addNumber = config.getBoolean("add-number");
                        for (int i = 0; i < files.length; i++) {
                            File file = files[i];
                            if (file.isFile() && file.getName().endsWith(".txt")) {
                                String title;
                                if (addNumber) {
                                    title = String.format("%d. %s", i + 1, file.getName());
                                } else {
                                    title = file.getName();
                                }
                                pages.add(title);
                            }
                        }
                    }
                    if (pages.isEmpty()) {
                        File file = new File(folder, "HelloWorld.txt");
                        try {
                            file.createNewFile();
                            FileWriter writer = new FileWriter(file);
                            writer.write("欢迎使用！");
                            writer.close();
                        } catch (IOException e) {
                            player.sendMessage(messages.getString("file-create-error"));
                            getLogger().warning(messages.getString("file-create-error"));
                            return true;
                        }
                    }
                    meta.setPages(pages);
                    book.setItemMeta(meta);
                    player.openBook(book);
                    return true;
                }
            }
        }
        return false;
    }

    public static String readFile(File file, FileConfiguration config) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), config.getString("encoding")));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }
        reader.close();
        return builder.toString();
    }

    @Override
    public void saveDefaultConfig() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            super.saveDefaultConfig();
            config = getConfig();
            config.options().copyDefaults(true);
            config.addDefault("encoding", "UTF-8");
            config.addDefault("add-number", true);
            saveConfig();
        }
        if (!new File(getDataFolder(), "messages.yml").exists()) {
            saveResource("messages.yml", false);
        }
    }
}