# MobsAreGone

**MobsAreGone** is a NeoForge 1.21.1 mod that lets you control which entities are allowed to spawn in your world.

## How it works

On first server start, a config file is created at:
```
config/mobsaregone-blacklist.json
```

Add any entity ID to the list to prevent it from spawning. Example:
```json
[
  "minecraft:phantom",
  "minecraft:creeper",
  "minecraft:enderman"
]
```

Restart the server after editing the file for changes to take effect.

Any valid entity ID works, including entities from other mods (e.g. `mymod:myentity`).
Unknown IDs are skipped with a warning in the log.

## Installation

Drop the `.jar` into your `mods/` folder. Requires NeoForge for Minecraft 1.21.1.

## Building from source

Requires JDK 21.

```bash
gradle build --no-daemon
```

Output: `build/libs/MobsAreGone-1.0.0.jar`

## License

MIT — © 2025 Blazelow
