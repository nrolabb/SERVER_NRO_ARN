package nro.models.services;

import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.utils.Logger;

public class SpineService {

    private static SpineService instance;

    public static SpineService gI() {
        if (instance == null) {
            instance = new SpineService();
        }
        return instance;
    }

    public void sendSpineSkillEffect(Player player, String skeletonPath, String animation, String skin, int durationMs) {
        if (player == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-48);
            msg.writer().writeByte(9);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF(skeletonPath);
            msg.writer().writeUTF(animation);
            msg.writer().writeUTF(skin);
            msg.writer().writeShort((short) durationMs);
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
            Logger.logException(SpineService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
