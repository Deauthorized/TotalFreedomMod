package me.totalfreedom.totalfreedommod;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.net.InetAddress;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import me.totalfreedom.totalfreedommod.util.FLog;
import org.bukkit.ChatColor;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;

public class ConnectionAuditor extends FreedomService
{
    public ConnectionAuditor(TotalFreedomMod plugin)
    {
        super(plugin);
    }

    @Override
    protected void onStart()
    {
    }

    @Override
    protected void onStop()
    {
    }

    // wikipedia is a cool source for identifying proxies for pretty much free
    public static final String apiurl = "https://en.wikipedia.org/w/api.php?action=query&list=blocks&format=json&bkip=";
    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        if (!ConfigEntry.BLOCK_PROXY.getBoolean())
        {
            return;
        }
        final String eventaddress = event.getAddress().toString().substring(1);
        try
        {
            FLog.info("Checking if " + eventaddress + " is a proxy connection");
            final URL checkURL = new URL(apiurl + eventaddress);
            final HttpURLConnection CCON = (HttpURLConnection)checkURL.openConnection();
            CCON.setRequestProperty("User-Agent", "");
            BufferedReader in = new BufferedReader(new InputStreamReader(CCON.getInputStream()));
            if ("host".equalsIgnoreCase(in.readLine()) || "vpn".equalsIgnoreCase(in.readLine()) || "proxy".equalsIgnoreCase(in.readLine()) || "tor".equalsIgnoreCase(in.readLine()))
            {
                FLog.info(eventaddress + " | proxy connection identified, disallowing");
                event.disallow(Result.KICK_OTHER, ChatColor.RED + "" + ChatColor.BOLD + "Connection rejected."
                    + (ChatColor.GOLD + "\n\nYour IP Address is currently blocked due to suspicion of being a VPN or proxy."
                    + (ChatColor.YELLOW + "\nWe usually allow these connections, but they are currently blocked due to recent abuse. Sorry about that.")
                    + (ChatColor.GOLD + "\nPlease disable your VPN or proxy and try again.")));
            }
            else
            {
                FLog.info(eventaddress + " is not a proxy");
                event.allow();
                return;
            }
            
        }
        catch (IOException e)
        {
            FLog.severe("Could not determine if IP Address (" + eventaddress + ") is proxied. Going to assume it is not.");
        }
    }


}
