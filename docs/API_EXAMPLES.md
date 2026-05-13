# API Examples

## Economy

```java
Anchor.api().economy().deposit(player.getUniqueId(), 100.0);
Anchor.api().economy().withdraw(player.getUniqueId(), 25.0);
boolean canAfford = Anchor.api().economy().has(player.getUniqueId(), 50.0);
```

## Permissions

```java
if (Anchor.api().permissions().has(player, "myplugin.admin")) {
    player.sendMessage("You have access.");
}
```

## Placeholders

```java
String text = Anchor.api().placeholders().parse(player, "Hello {player}");
Anchor.api().placeholders().register("rank", offlinePlayer -> "Member");
```

## Regions

```java
boolean canBuild = Anchor.api().regions().canBuild(player, location);
Set<String> ids = Anchor.api().regions().getRegionsAt(location);
```

## Items

```java
ItemStack tagged = Anchor.api().items().setString(item, "shop-id", "weapons");
Optional<String> shopId = Anchor.api().items().getString(tagged, "shop-id");
```

## GUI

```java
AnchorGui.builder()
    .title("Anchor Example")
    .rows(3)
    .item(13, item, event -> event.getPlayer().sendMessage("Clicked!"))
    .open(player);
```

## Scheduler

```java
Anchor.api().scheduler().runLater(() -> player.sendMessage("Later"), 20L);
```
