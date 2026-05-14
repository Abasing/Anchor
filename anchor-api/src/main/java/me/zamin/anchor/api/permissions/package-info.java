/**
 * Permission abstractions covering LuckPerms, Vault permissions, and Bukkit
 * fallback behavior, including world-aware checks and safe mutation results
 * where supported.
 * <p>
 * Permission checks are intended to be cheap and synchronous. Permission
 * mutation may involve provider storage or network work, so async mutation
 * methods are the preferred path for command handlers, migrations, and bulk
 * changes.
 */
package me.zamin.anchor.api.permissions;
