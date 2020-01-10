package me.totalfreedom.totalfreedommod;

import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.util.FSync;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntiSpam extends FreedomService
{

    public static final int MSG_PER_CYCLE = 4;
    public static final int TICKS_PER_CYCLE = 2 * 10;
    List<String> markedfordeath = new ArrayList<>();
    //
    public BukkitTask cycleTask = null;

    public AntiSpam(TotalFreedomMod plugin)
    {
        super(plugin);
    }

    @Override
    protected void onStart()
    {
        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                cycle();
            }
        }.runTaskTimer(plugin, TICKS_PER_CYCLE, TICKS_PER_CYCLE);
    }

    @Override
    protected void onStop()
    {
        FUtil.cancel(cycleTask);
    }

    private void cycle()
    {
        for (Player player : server.getOnlinePlayers())
        {
            final FPlayer playerdata = plugin.pl.getPlayer(player);

            // TODO: Move each to their own section
            playerdata.resetMsgCount();
            playerdata.resetBlockDestroyCount();
            playerdata.resetBlockPlaceCount();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();

        if (plugin.al.isAdmin(player))
        {
            return;
        }

        String message = event.getMessage().trim();

        final FPlayer playerdata = plugin.pl.getPlayerSync(player);

        // Check for spam
        if (playerdata.incrementAndGetMsgCount() > MSG_PER_CYCLE)
        {   
            if(!markedfordeath.contains(player.getName()))
            {
                markedfordeath.add(player.getName());
                FSync.bcastMsg(player.getName() + " was automatically kicked for sending too many messages.", ChatColor.RED);
                FSync.autoEject(player, "Kicked for spamming chat.");

                playerdata.resetMsgCount();

                event.setCancelled(true);
                return;
            } else {
                // Prevent eject from being called like 2000 times when someone sends a shitload of messages
                return;
            }
        } else if(playerdata.incrementAndGetMsgCount() > MSG_PER_CYCLE / 2)
        {
            FUtil.playerMsg(player, "Please refrain from spamming chat.", ChatColor.GRAY);
            event.setCancelled(true);
        }
        
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();
        final Player player = event.getPlayer();
        final FPlayer fPlayer = plugin.pl.getPlayer(player);
        fPlayer.setLastCommand(command);

        if (fPlayer.allCommandsBlocked())
        {
            FUtil.playerMsg(player, "Your commands have been blocked by an admin.", ChatColor.RED);
            event.setCancelled(true);
            return;
        }

        if (plugin.al.isAdmin(player))
        {
            return;
        }

        if (fPlayer.incrementAndGetMsgCount() > MSG_PER_CYCLE)
        {
            FUtil.bcastMsg(player.getName() + " was automatically kicked for spamming commands.", ChatColor.RED);
            plugin.ae.autoEject(player, "Kicked for spamming commands.");

            fPlayer.resetMsgCount();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerKick(Player player)
    {
        markedfordeath.remove(player.getName());
    }
}
