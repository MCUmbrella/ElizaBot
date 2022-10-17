package vip.floatationdevice;

import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.g4jbot.G4JBot;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketWelcomeEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Main
{
    static String botUserId = null;
    static ConcurrentHashMap<String, Eliza> sessions = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, SessionTimer> timers = new ConcurrentHashMap<>();
    static G4JBot b;

    static void sendGuildedMessage(String replyTo, String msg)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    b.getChatMessageManager().createChannelMessage(
                            ConfigUtil.channelId,
                            msg,
                            null,
                            replyTo != null ? new String[]{replyTo} : null,
                            null,
                            null
                    );
                }
                catch(Exception e)
                {
                    System.err.println("[BOT] Error sending message \"" + msg + "\":\n      " + e);
                }
            }
        }.start();
    }

    public static void main(String[] args)
    {
        try
        {
            ConfigUtil.loadConfig();
            System.out.println("[SYSTEM] Config check completed");
        }
        catch(Exception e)
        {
            System.err.println("[SYSTEM] Config check failed");
            e.printStackTrace();
            System.exit(-1);
        }
        b = new G4JBot(ConfigUtil.token);
        //b.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 59909)));
        b.setVerbose(false)
                .setAutoReconnect(true)
                .setCommandPrefix("/")
                .registerCommand(new ElizaCommandExecutor())
                .registerEventListener(new Object()
                {
                    @Subscribe
                    public void onLogin(GuildedWebSocketWelcomeEvent ev)
                    {
                        botUserId = ev.getSelf().getId();
                        System.out.println("[BOT] Eliza bot logged in");
                    }

                    @Subscribe
                    public void onChat(ChatMessageCreatedEvent ev)
                    {
                        if(ev.getChatMessage().getChannelId().equals(ConfigUtil.channelId) &&
                                sessions.containsKey(ev.getChatMessage().getCreatorId()) &&
                                !ev.getChatMessage().getContent().startsWith("/")
                        )
                        {
                            ChatMessage msg = ev.getChatMessage();
                            String response = sessions.get(msg.getCreatorId()).getResponse(msg.getContent());
                            System.out.println("[CHAT] <" + msg.getCreatorId() + "> " + msg.getContent());
                            System.out.println("[CHAT] <Eliza> " + response);
                            sendGuildedMessage(
                                    msg.getId(),
                                    response
                            );
                            // reset session expiration timer
                            System.out.println("[BOT] Resetting session expiration timer for " + msg.getCreatorId());
                            timers.get(msg.getCreatorId()).interrupt();
                            timers.remove(msg.getCreatorId());
                            SessionTimer timer = new SessionTimer(msg.getCreatorId());
                            timers.put(msg.getCreatorId(), timer);
                            timer.start();
                        }
                    }
                })
                .connectWebSocket(true, null);
        System.out.println("[SYSTEM] Websocket connection started");
        Scanner sc = new Scanner(System.in);
        for(; ; )
        {
            String s;
            try
            {
                s = sc.nextLine();
            }
            catch(NoSuchElementException e) // ^D
            {
                s = "q";
            }
            switch(s.toLowerCase())
            {
                case "q": // exit the program
                {
                    System.out.println("[BOT] Stopping");
                    b.disconnectWebSocket(true);
                    System.out.println("[BOT] Eliza bot stopped");
                    System.exit(0);
                }
                case "s": // list all sessions
                {
                    System.out.println("[BOT] " + sessions.size() + " session(s):\n      " + sessions.keySet());
                    break;
                }
                case "c": // close all sessions
                {
                    System.out.println("[BOT] Closing all sessions");
                    timers.values().forEach(new Consumer<SessionTimer>()
                    {
                        @Override
                        public void accept(SessionTimer t)
                        {
                            t.interrupt();
                        }
                    });
                    timers.clear();
                    sessions.clear();
                }
            }
        }
    }
}
