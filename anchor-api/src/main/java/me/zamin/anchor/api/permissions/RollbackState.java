package me.zamin.anchor.api.permissions;

import java.util.List;

record RollbackState(
    List<String> rolledBackPermissions,
    List<PermissionOperationResult> rollbackFailures
) {
}
