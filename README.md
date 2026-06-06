# MaceDamageCap

A lightweight Paper plugin that caps the maximum damage dealt by the vanilla Minecraft Mace. Built for Paper 1.21+ SMP servers where Mace smashes are unbalanced in PvP, raid farms, or any scenario where high fall-distance damage can one-shot players or destroy farms instantly.

Made with love by **salatube** &lt;3

---

## Features

- **Caps the maximum Mace damage** at a configurable value (default `14.0` = 7 hearts), regardless of fall distance, enchantments (Density, Breach, Smite, etc.), critical hits, Strength effects, or any other damage modifier.
- **Preserves all vanilla Mace mechanics** — knockback, sounds, particles, and enchantment effects still work normally; only the damage value is reduced when it exceeds the cap.
- **Per-player bypass system** stored on disk using UUIDs, so it survives restarts and works for both online and offline players.
- **Hot-reloadable configuration** — change the cap in-game and it is written straight to `config.yml`.
- **Tab completion** for every subcommand and argument.
- **Colored, professional chat messages** with a configurable prefix.
- **Zero NMS / zero dependencies** — uses only the Paper API and `net.kyori.adventure`.
- **Production-ready and SMP-safe** — single high-priority event listener, no per-tick work.

---

## Installation

1. Download the latest `MaceDamageCap-*.jar` from the [Releases](#) tab.
2. Drop it into your server's `plugins/` folder.
3. Start (or restart) the server.
4. Edit `plugins/MaceDamageCap/config.yml` to tune the cap, prefix, and limits.

Requires **Paper 1.21+** and **Java 21+**.

---

## Configuration

File: `plugins/MaceDamageCap/config.yml`

```yaml
# MaceDamageCap - Main Configuration
# Made with love by salatube <3

# The maximum damage allowed for any vanilla Mace attack.
# 1 damage = 0.5 heart. Default 14 = 7 hearts.
damage-cap: 14.0

# Minimum allowed cap. Commands that would set a value below this are rejected.
min-cap: 0.0

# Maximum allowed cap. Commands that would set a value above this are rejected.
max-cap: 1000.0

# The chat prefix used by all plugin messages. Supports &-color codes.
messages-prefix: "&6&lMaceDamageCap &8&l\u00BB &r"

# Whether to log cap changes to the server console.
log-changes: true
```

All settings persist across restarts. Changes made via `/mdc set` are saved automatically.

Bypass data is stored separately in `plugins/MaceDamageCap/bypass.yml` as a list of player UUIDs.

---

## Commands

| Command                                          | Description                                | Permission              |
| ------------------------------------------------ | ------------------------------------------ | ----------------------- |
| `/macedamagecap`                                 | Show plugin info, current cap, and help.   | `macedamagecap.admin`   |
| `/macedamagecap help`                            | Show the help menu.                        | `macedamagecap.admin`   |
| `/macedamagecap get`                             | Display the current damage cap.            | `macedamagecap.admin`   |
| `/macedamagecap set <damage>`                    | Change the maximum Mace damage cap.        | `macedamagecap.admin`   |
| `/macedamagecap reload`                          | Reload `config.yml` and `bypass.yml`.      | `macedamagecap.admin`   |
| `/macedamagecap bypass add <player>`             | Grant bypass to a player (UUID-based).     | `macedamagecap.admin`   |
| `/macedamagecap bypass remove <player>`          | Revoke bypass from a player.               | `macedamagecap.admin`   |
| `/macedamagecap bypass list`                     | List every player with bypass enabled.     | `macedamagecap.admin`   |
| `/macedamagecap bypass check <player>`           | Check whether a player has bypass enabled. | `macedamagecap.admin`   |

Aliases: `/mdc`, `/macecap`

Player arguments accept either a player name (online or previously joined) or a raw UUID.

---

## Permissions

| Permission                | Description                                                | Default |
| ------------------------- | ---------------------------------------------------------- | ------- |
| `macedamagecap.admin`     | Grants access to all `/macedamagecap` subcommands.         | `op`    |
| `macedamagecap.bypass`    | Bypasses the Mace damage cap. Auto-attached via commands.  | `false` |

You may also grant `macedamagecap.bypass` to a player manually through your permissions plugin (LuckPerms, etc.) and they will bypass the cap — no `/mdc` command required.

---

## How it works

- The plugin listens to `EntityDamageByEntityEvent` at `HIGH` priority.
- If the damager is a player holding a `Material.MACE` in their main hand, the final computed damage (which already includes fall distance, enchantments, crits, and all other vanilla modifiers) is compared against `damage-cap`.
- If the value is higher, `event.setDamage(cap)` is called — the hit keeps all of its knockback, sound, and particle effects, but deals exactly the capped damage.
- Players whose UUID appears in `bypass.yml`, or who have the `macedamagecap.bypass` permission, are skipped entirely.

### Example

- `damage-cap: 14.0`
- Normal Mace hit: `8.0` damage &rarr; remains `8.0`
- Mace smash (35 damage): `35.0` damage &rarr; becomes `14.0`
- Mace smash by bypass player: `35.0` damage &rarr; remains `35.0`
- Mace smash by bypass player with Density V: remains uncapped

---

## Building from source

```bash
git clone https://github.com/peroxide-dev/macedamagecap
cd MaceDamageCap
mvn clean package
```

The compiled jar will be in `target/MaceDamageCap-<version>.jar`.

---

## Credits

Made with love by **salatube** &lt;3
