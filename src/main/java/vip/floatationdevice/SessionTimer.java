package vip.floatationdevice;

public class SessionTimer extends Thread
{
    private final String userId;

    public SessionTimer(String userId)
    {
        this.userId = userId;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("[TIMER] <" + getName() + "> Started session expiration timer of " + userId);
            sleep(10000L);
            System.out.println("[TIMER] <" + getName() + "> Eliza session for " + userId + " expired");
            Main.sessions.remove(userId);
            Main.timers.remove(userId);
        }
        catch(InterruptedException e)
        {
            System.out.println("[TIMER] <" + getId() + "> Stopped session expiration timer for " + userId);
        }
    }
}
