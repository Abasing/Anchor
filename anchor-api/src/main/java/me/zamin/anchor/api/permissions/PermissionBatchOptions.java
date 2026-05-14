package me.zamin.anchor.api.permissions;

/**
 * Options for batch permission mutation.
 *
 * @param rollbackOnFailure whether Anchor should attempt best-effort rollback
 *                          when a batch grant or revoke fails after prior
 *                          success
 * @param continueOnFailure whether Anchor should continue processing after a
 *                          failure when rollback is disabled
 */
public record PermissionBatchOptions(
    boolean rollbackOnFailure,
    boolean continueOnFailure
) {

    private static final PermissionBatchOptions DEFAULTS = new PermissionBatchOptions(false, false);

    /**
     * Returns the default batch options.
     * <p>
     * By default Anchor stops on the first failure and does not attempt
     * rollback.
     *
     * @return default batch options
     */
    public static PermissionBatchOptions defaults() {
        return DEFAULTS;
    }
}
