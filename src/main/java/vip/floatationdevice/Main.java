package vip.floatationdevice;

import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.g4jbot.G4JBot;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketWelcomeEvent;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Main
{
    static String botUserId = null;
    static Eliza e = new Eliza();

    public static void main(String[] args)
    {
        ConfigUtil.loadConfig();
        G4JBot b = new G4JBot(ConfigUtil.token);
        b.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 59909)));
        b.setVerbose(true)
                .setAutoReconnect(true)
                .setCommandPrefix("/eliza ")
                .registerEventListener(new Object()
                {
                    @Subscribe
                    public void onLogin(GuildedWebSocketWelcomeEvent ev)
                    {
                        botUserId = ev.getSelf().getId();
                        new Thread()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    b.getChatMessageManager().createChannelMessage(ConfigUtil.channelId,
                                            Eliza.WELCOME_MSG,
                                            null,
                                            null,
                                            null,
                                            null
                                    );
                                }
                                catch (Exception ex)
                                {
                                    System.err.println("Error creating channel message: " + ex);
                                }
                            }
                        }.start();
                    }

                    @Subscribe
                    public void onChat(ChatMessageCreatedEvent ev)
                    {
                        if (ev.getChatMessage().getChannelId().equals(ConfigUtil.channelId) && !ev.getChatMessage().getCreatorId().equals(botUserId))
                            new Thread()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        b.getChatMessageManager().createChannelMessage(ConfigUtil.channelId,
                                                e.getResponse(ev.getChatMessage().getContent()),
                                                null,
                                                new String[]{ev.getChatMessage().getId()},
                                                null,
                                                null
                                        );
                                    }
                                    catch (Exception ex)
                                    {
                                        System.err.println("Error creating channel message: " + ex);
                                    }
                                }
                            }.start();
                    }
                })
                .connectWebSocket();
    }
}
