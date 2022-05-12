package coalmc.net.playerlimiter;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public final class PlayerLimiter extends JavaPlugin implements Listener, CommandExecutor {

    public static String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "PlayerLimiter" + ChatColor.DARK_GRAY + "]";
    public long PlayerLimit;
    public static Permission permission = null;
    public boolean ToggleChat = true;
    public boolean ToggleEvent = false;
    public List<UUID> ChatOverrides = new ArrayList<>();
    public List<String> Specials = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage( prefix + ChatColor.GREEN + " Enabled!");
        PlayerLimit = 10;
        Specials = Arrays.asList("ItsZantle", "GOSmikeyYT", "Lthelostsoul", "ohXess", "AnEnbyPerson", "che5sterttv", "peanutdragonn", "OmegaMasterChad");
        setupPermissions();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage( prefix + ChatColor.RED + " Shutting Down...");
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    public Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (ToggleEvent) {
            if (!permission.playerHas(e.getPlayer(), "coalmc.override")) {
                e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), 312, 113, 2));
                e.getPlayer().sendMessage(ChatColor.RED + "Event has already started!");
            }
        }

        if (Specials.contains(e.getPlayer().getName())) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + e.getPlayer().getName() + " parent set special");
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        ChatOverrides.remove(e.getPlayer().getUniqueId());
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (ToggleChat) {
            return;
        }
        if (permission.playerHas(e.getPlayer(), "coalmc.override")) {
            return;
        }
        if (ChatOverrides.contains(e.getPlayer().getUniqueId())) {
            return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage(ChatColor.RED + "Chat is currently disabled!");
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent e) {
        e.setRespawnLocation(new Location(e.getPlayer().getWorld(), 312, 113, 2));
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e) {
        try {
            if ((long) this.getServer().getOnlinePlayers().size() >= PlayerLimit) {
                if (!permission.playerHas("world", Bukkit.getOfflinePlayer(e.getUniqueId()), "coalmc.override")) {
                    e.setKickMessage(ChatColor.RED + "Server is currently full");
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_FULL);
                }
            }
        } catch (Exception exception) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + " " + exception.getMessage());
            e.setKickMessage(ChatColor.RED + "Server is currently full");
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_FULL);
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("setplayerlimit")) {
            if (args.length != 0) {
                Integer i = parseIntOrNull(args[0]);
                if (i != null) {
                    PlayerLimit = i;
                    sender.sendMessage(ChatColor.RED + "PlayerLimit " + ChatColor.WHITE + "set to " + ChatColor.AQUA + i);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Must specify a valid number!");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "You must specify an amount!");
        }
        else if (command.getName().equalsIgnoreCase("togglechat")) {
            if (args.length != 0) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player != null) {
                    if (ChatOverrides.contains(player.getUniqueId())) {
                        ChatOverrides.remove(player.getUniqueId());
                        sender.sendMessage(ChatColor.RED + args[0] + "'s" + ChatColor.WHITE + " chat has been toggled" + ChatColor.RED + " OFF");
                    } else {
                        ChatOverrides.add(player.getUniqueId());
                        sender.sendMessage(ChatColor.RED + args[0] + "'s" + ChatColor.WHITE + " chat has been toggled" + ChatColor.GREEN + " ON");
                    }

                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Invalid Player!");
                return true;
            } else {
                if (ToggleChat) {
                    ToggleChat = false;
                    ChatOverrides = new ArrayList<>();
                    sender.sendMessage(ChatColor.RED + "Chat " + ChatColor.WHITE + "was toggled " + ChatColor.RED + "OFF");
                    return true;
                }
                ToggleChat = true;
                sender.sendMessage(ChatColor.RED + "Chat " + ChatColor.WHITE + "was toggled " + ChatColor.GREEN + "ON");
                return true;
            }


        }
        else if (command.getName().equalsIgnoreCase("toggleevent")) {
            if (ToggleEvent) {
                ToggleEvent = false;
                sender.sendMessage(ChatColor.RED + "Event " + ChatColor.WHITE + "was toggled " + ChatColor.RED + "OFF");
                return true;
            }
            ToggleEvent = true;
            sender.sendMessage(ChatColor.RED + "Event " + ChatColor.WHITE + "was toggled " + ChatColor.GREEN + "ON");
            return true;

        }
        return true;
    }
}
