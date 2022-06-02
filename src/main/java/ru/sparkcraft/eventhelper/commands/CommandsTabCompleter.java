package ru.sparkcraft.eventhelper.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandsTabCompleter implements TabCompleter {

    private static final String[] COMMANDS = {"create", "add", "list", "delete", "clear", "remove", "select", "info"};

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
            Collections.sort(completions);
        }
        else if (args.length == 2) {
          //  if (args[0].equals("create")) {
          //  }
          //  StringUtil.copyPartialMatches(args[1], Arrays.asList(COMMANDS), completions);
        }
        Collections.sort(completions);
        return completions;
    }
}
