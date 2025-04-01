package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class UpdateSubcommand extends AbstractSubCommand {

    public UpdateSubcommand(Main plugin) {
        super(plugin, "update", "serverprotector.update", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        checkAndUpdatePlugin(sender, plugin);
        return true;
    }

    public void checkAndUpdatePlugin(CommandSender sender, Main plugin) {
        plugin.runner.runAsync(() -> Utils.checkUpdates(plugin, version -> {
            var systemMessages = pluginConfig.systemMessages;
            sender.sendMessage(systemMessages.baselineDefault());

            var currentVersion = plugin.getDescription().getVersion();

            if (currentVersion.equals(version)) {
                sender.sendMessage(systemMessages.updateLatest());
            } else {
                var currentJarName = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                var downloadUrl = "https://github.com/Overwrite987/UltimateServerProtector/releases/download/" + version + "/" + "UltimateServerProtector.jar";
                try {
                    var updateFolder = Bukkit.getUpdateFolderFile();
                    var targetFile = new File(updateFolder, currentJarName);

                    downloadFile(downloadUrl, targetFile, sender);

                    sender.sendMessage(systemMessages.updateSuccess1());
                    sender.sendMessage(systemMessages.updateSuccess2());
                } catch (IOException ex) {
                    sender.sendMessage("Unable to download update: " + ex.getMessage());
                }
            }
            sender.sendMessage(systemMessages.baselineDefault());
        }));
    }

    public void downloadFile(String fileURL, File targetFile, CommandSender sender) throws IOException {
        var url = new URL(fileURL);
        var connection = url.openConnection();
        var fileSize = connection.getContentLength();

        try (var in = new BufferedInputStream(connection.getInputStream());
             var out = new FileOutputStream(targetFile)) {

            var data = new byte[1024];
            int bytesRead;
            var totalBytesRead = 0;
            var lastPercentage = 0;

            while ((bytesRead = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
                var progressPercentage = (int) ((double) totalBytesRead / fileSize * 100);

                if (progressPercentage >= lastPercentage + 10) {
                    lastPercentage = progressPercentage;
                    var downloadedKB = totalBytesRead / 1024;
                    var fullSizeKB = fileSize / 1024;
                    sender.sendMessage(downloadedKB + "/" + fullSizeKB + "KB) (" + progressPercentage + "%)");
                }
            }
        }
    }
}
