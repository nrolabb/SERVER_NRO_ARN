package nro.models.clan;

/**
 * Voice message trong chat bang hội.
 * Lưu trữ dữ liệu audio ghi âm của player gửi cho bang.
 */
public class ClanVoiceMessage {

    // Audio format constants
    public static final byte FORMAT_AMR = 0;
    public static final byte FORMAT_OPUS = 1;
    public static final byte FORMAT_PCM = 2;

    private Clan clan;

    public int id;
    public int playerId;
    public String playerName;
    public byte role;
    public int time;              // timestamp (seconds since epoch - 1000000000)
    public byte[] audioData;      // raw audio bytes
    public short audioDuration;   // thời lượng (giây)
    public byte audioFormat;      // FORMAT_AMR, FORMAT_OPUS, FORMAT_PCM

    public ClanVoiceMessage(Clan clan) {
        this.clan = clan;
        this.id = clan.clanMessageId++;
        this.time = (int) (System.currentTimeMillis() / 1000 - 1000000000);
    }

    /**
     * Kích thước dữ liệu audio (bytes)
     */
    public int getAudioSize() {
        return audioData != null ? audioData.length : 0;
    }

    public void dispose() {
        this.clan = null;
        this.playerName = null;
        this.audioData = null;
    }
}
