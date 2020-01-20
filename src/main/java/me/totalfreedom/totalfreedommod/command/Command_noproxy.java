package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.apache.commons.lang3.StringUtils;
import java.lang.Integer;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;


@CommandPermissions(level = Rank.SUPER_ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Prohibits connections from VPN providers or other anonymizing services. This should only be used when all other methods of keeping serial ban bypassers out have failed.", usage = "/<command> <on/off>", aliases = "ecap")
public class Command_noproxy extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 1)
        {
            return false;
        }

        if ("on".equals(args[0]))
        {
            msg(playerSender, "Enabling the proxy blocker will noticably slow down connection speeds. This should ONLY be enabled if all other attempts to thwart serial ban bypassers has failed (captcha, repeated banning, lockup, etc). Once you've read this, you may do /noproxy on-confirm to enable the proxy blocker.");
        }
        else if ("on-confirm".equals(args[0]))
        {
            FUtil.adminAction(sender.getName(), " Blocking proxy connections", true);
            ConfigEntry.BLOCK_PROXY.setBoolean(true);
            msg(playerSender, "Connections from proxies are now blocked.");
            return true;
        }
        else if ("off".equals(args[0]))
        {
            FUtil.adminAction(sender.getName(), " Unblocking proxy connections", true);
            ConfigEntry.BLOCK_PROXY.setBoolean(false);
            msg(playerSender, "Connections from proxies are no longer blocked.");
            return true;
        } 
        else
        {
            return false;
        }
        return true;
    }

}
