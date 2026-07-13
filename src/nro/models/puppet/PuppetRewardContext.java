package nro.models.puppet;

/**
 * Marks the synchronous damage/reward call stack of a puppet. ItemMap uses it
 * to force every resulting drop (including special boss drops with owner -1)
 * to the puppet owner.
 */
public final class PuppetRewardContext {

    private static final ThreadLocal<Long> OWNER_ID = new ThreadLocal<>();

    private PuppetRewardContext() {
    }

    public static Long begin(long ownerId) {
        Long previous = OWNER_ID.get();
        OWNER_ID.set(ownerId);
        return previous;
    }

    public static void end(Long previousOwnerId) {
        if (previousOwnerId == null) {
            OWNER_ID.remove();
        } else {
            OWNER_ID.set(previousOwnerId);
        }
    }

    public static Long getOwnerId() {
        return OWNER_ID.get();
    }
}
