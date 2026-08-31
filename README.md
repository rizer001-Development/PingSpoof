# PingSpoof

A client-side Fabric mod for Minecraft 26.2 that lets you set a custom ping value shown on the server.

The server measures your ping by timing how long the client takes to answer keep-alive packets. This mod intercepts the keep-alive handling on the client and delays the reply by the configured value, so the ping shown in the player tab matches the value you set.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.158.0+26.2 or newer
- Java 25

## Installation

1. Download `pingspoof-1.0.0.jar` from `build/libs/` in this repository (a compiled jar is committed so it can be downloaded directly).
2. Place the jar into the `mods` folder of your Fabric client.
3. Launch the game.

## Usage

The mod registers the `/ps` command. `/pingspoof` works as an alias.

| Command      | Action                                              |
| ------------ | --------------------------------------------------- |
| `/ps`        | Show the current status                             |
| `/ps <ms>`   | Set ping to `<ms>` (0-60000) and enable the mod; `0` replies instantly |
| `/ps on`     | Enable the mod with the stored value                |
| `/ps off`    | Disable the mod                                     |

Examples:

```
/ps 0     reply instantly, ping as low as possible
/ps 50    simulate a ping of about 50 ms
/ps off   disable the mod
```

Note: the server measures the round-trip time of the connection, so the configured value is added on top of your real network latency. The ping cannot go below your actual RTT.

## Configuration

Settings are saved to `config/pingspoof.json` in your `.minecraft` folder:

```json
{
  "enabled": true,
  "pingMs": 50
}
```

## Building from source

Requires JDK 25.

```
./gradlew build
```

The compiled jar is written to `build/libs/pingspoof-1.0.0.jar`.

## Project structure

- `src/main/java/com/pingspoof/PingSpoofMod.java` - common entrypoint.
- `src/client/java/com/pingspoof/client/PingSpoofClient.java` - client entrypoint; loads the config and registers the `/ps` command.
- `src/client/java/com/pingspoof/client/config/PingSpoofConfig.java` - JSON config.
- `src/client/java/com/pingspoof/client/mixin/ClientPacketListenerMixin.java` - mixin on `ClientCommonPacketListenerImpl#handleKeepAlive` (common packets, including keep-alive, are handled there in 26.2).

## License

AGPL-3.0
