# CluckSquad Quest Sync (RuneLite plugin)

Uploads each player's **quest completion** and **skill levels** to the CluckSquad Quest
Calculator (`https://clucksquad.com/quest-calculator/`). On the website, hit
**"↻ Load my completed quests"** on the QP Tracker and every finished quest ticks itself.

This exists because **there is no public OSRS quest-completion API** — the only way a website
can know which quests you've done is if a RuneLite plugin reads them from the game client and
uploads them. This is the same approach the OSRS Wiki uses (their plugin is called *WikiSync*).

---

## What it does
- On login (and every 15 min, configurable) it reads all quest states and your real skill
  levels, then POSTs them to the site's sync endpoint.
- The site stores them keyed by your RuneScape name, protected by a shared **sync key**.
- Nothing else is collected — just quest states and skill levels.

## Get the sync key (site admin)
1. Log into `clucksquad.com` as an admin and open `https://clucksquad.com/quest-calculator/`.
2. Press **F12** → Console tab → type `CS_QUEST_CFG.syncKey` and press Enter. Copy the value.
   (The key only appears for logged-in admins.)
3. Share that key with members, **or** hardcode it as the default in
   `CluckSquadSyncConfig.java` (`syncKey()` return value) before you build, so members need
   zero configuration.

## Build
Requires JDK 11 and the Gradle wrapper (or a local Gradle 7.x).

```bash
cd clucksquad-quest-sync
./gradlew shadowJar
# → build/libs/clucksquad-quest-sync-1.0.0-all.jar
```

If you don't have the Gradle wrapper files, run `gradle wrapper --gradle-version 7.6.4` once,
or build with a system Gradle 7.x using `gradle shadowJar`.

## Install (side-load)
RuneLite loads external plugin jars from its `sideloaded-plugins` folder:
- Windows: `%USERPROFILE%\.runelite\sideloaded-plugins\`
- macOS/Linux: `~/.runelite/sideloaded-plugins/`

Copy the built `*-all.jar` there and restart RuneLite. Then:
1. Open the plugin's config (search "CluckSquad" in the RuneLite plugin list).
2. Paste the **Sync key** (unless you hardcoded it).
3. Log into the game — after ~20s your data uploads. Reload the Quest Calculator and click
   **↻ Load my completed quests**.

> Side-loading requires RuneLite's *"Enable loading of external plugins"* developer setting,
> or distribution via the official **Plugin Hub** (open-source submission + review at
> https://github.com/runelite/plugin-hub). Plugin Hub reaches everyone without dev-mode, but
> approval is up to the RuneLite team.

## Notes / limitations
- Quest names must match the calculator's dataset. Almost all do; a handful counted as
  sub-quests on the site (e.g. Recipe for Disaster parts) won't auto-tick and can be ticked
  by hand.
- The sync key is a light guard against random writes, not high security — it's fine for a
  clan tool. Anyone with the key can post quest data for any name.
- Server side lives in the `cs-quest-calc` WordPress plugin (actions `cs_quest_sync` /
  `cs_quest_synced`).
