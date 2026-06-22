package nro.models.server;

import java.util.concurrent.Executors;
import nro.models.services.Service;
import nro.models.utils.Logger;

/**
 *
 * @author By Mr Blue
 *
 */
public class Maintenance extends Thread {

    private static Maintenance instance;
    private int timeInSeconds;
    public static boolean isRunning = false;
// bảo trì game false
    private Maintenance() {
    }

    public static Maintenance gI() {
        if (instance == null) {
            instance = new Maintenance();
        }
        return instance;
    }

    public void startCountdown() {
        if (!isRunning) {
            isRunning = true;
            this.timeInSeconds = 60;
            this.start();
        }
    }

    public void startSeconds(int seconds) {
        if (!isRunning) {
            isRunning = true;
            this.timeInSeconds = seconds;
            this.start();
        }
    }

    public void startImmediately() {
        if (!isRunning) {
            isRunning = true;
            Logger.log(Logger.YELLOW, "BẮT ĐẦU BẢO TRÌ NGAY\n");
            ServerManager.gI().close();
        }
    }

    @Override
    public void run() {
        Logger.log(Logger.YELLOW, "Bắt đầu đếm ngược 60s bảo trì");

        while (timeInSeconds > 0) {
            try {
                sendRemainingTime();
                Thread.sleep(6000);
                timeInSeconds--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Logger.log(Logger.YELLOW, "BẢO TRÌ BẮT ĐẦU\n");
        ServerManager.gI().close();
    }

    private void sendRemainingTime() {
        String msg = "BẢO TRÌ SAU " + timeInSeconds + " GIÂY.\nOUT GAME MAU LÊN.";
        Service.gI().sendThongBaoAllPlayer(msg);
        Logger.log(Logger.YELLOW, msg);
    }
}
