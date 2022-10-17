package vip.floatationdevice.elizabot;

import vip.floatationdevice.eliza.Eliza;
import vip.floatationdevice.g4jbot.G4JBot;
import vip.floatationdevice.g4jbot.GuildedCommandExecutor;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import static vip.floatationdevice.elizabot.Main.*;

public class ElizaCommandExecutor implements GuildedCommandExecutor
{

    @Override
    public String getCommandName()
    {
        return "eliza";
    }

    @Override
    public void onCommand(G4JBot bot, ChatMessage msg, String[] args)
    {
        if(args.length == 1)
        {
            if(args[0].equals("start"))
            {
                if(!sessions.containsKey(msg.getCreatorId()))
                {
                    System.out.println("[BOT] Creating Eliza session for " + msg.getCreatorId());
                    sessions.put(msg.getCreatorId(), new Eliza());
                    sendGuildedMessage(msg.getId(), Eliza.WELCOME_MSG);
                    // create a session expiration timer
                    SessionTimer timer = new SessionTimer(msg.getCreatorId());
                    timers.put(msg.getCreatorId(), timer);
                    timer.start();
                }
            }
            else if(args[0].equals("stop"))
            {
                if(sessions.containsKey(msg.getCreatorId()))
                {
                    System.out.println("[BOT] Stopping Eliza session for " + msg.getCreatorId());
                    sessions.remove(msg.getCreatorId());
                    sendGuildedMessage(msg.getId(), "Goodbye!");
                    // remove timer
                    timers.get(msg.getCreatorId()).interrupt();
                    timers.remove(msg.getCreatorId());
                }
            }
        }
        else sendGuildedMessage(msg.getId(), "Usage: `/eliza start` or `/eliza stop`");
    }
}
