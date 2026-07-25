package nro.models.Bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nro.models.map.service.ChangeMapService;
import nro.models.server.ServerManager;

public class BotManager implements Runnable {

    public static BotManager i;

    public List<Bot> bot = Collections.synchronizedList(new ArrayList<>());

    public static BotManager gI() {
        if (i == null) {
            i = new BotManager();
        }
        return i;
    }

    public List<Bot> getBotsSnapshot() {
        synchronized (bot) {
            return new ArrayList<>(bot);
        }
    }

    public Bot removeBot(int index) {
        Bot removed;
        synchronized (bot) {
            if (index < 0 || index >= bot.size()) {
                return null;
            }
            removed = bot.remove(index);
        }
        removeBotFromMap(removed);
        return removed;
    }

    public int removeAllBots() {
        List<Bot> removedBots;
        synchronized (bot) {
            removedBots = new ArrayList<>(bot);
            bot.clear();
        }
        for (Bot removed : removedBots) {
            removeBotFromMap(removed);
        }
        return removedBots.size();
    }

    private void removeBotFromMap(Bot removed) {
        if (removed != null && removed.zone != null) {
            ChangeMapService.gI().exitMap(removed);
        }
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();

                for (Bot bot : getBotsSnapshot()) {
                    if (bot != null) {
                        bot.update();
                    }
                }

                long timeLeft = 150 - (System.currentTimeMillis() - st);
                if (timeLeft > 0) {
                    Thread.sleep(timeLeft);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
