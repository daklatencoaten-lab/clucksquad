# Getting this into the RuneLite Plugin Hub

Goal: members open RuneLite → **Configuration → Plugin Hub**, search "CluckSquad", and click
install — no dev-mode, no side-loading. That's the safe, in-client path you want.

There are three steps. Steps 1–2 we can do together in the browser (you log into GitHub);
step 3 is RuneLite's review, which is out of our hands.

## Step 1 — Put this plugin in a public GitHub repo
1. Create a free GitHub account (or use yours) and a **public** repo, e.g. `clucksquad-quest-sync`.
2. Upload everything in this folder (the `src/` tree, `build.gradle`, `settings.gradle`,
   `LICENSE`, `README.md`, `runelite-plugin.properties`).
3. Copy the **full commit hash** of your latest commit (40 characters).

## Step 2 — Submit to the Plugin Hub
1. Go to https://github.com/runelite/plugin-hub and click **Fork**.
2. In your fork, add a new file `plugins/clucksquad-quest-sync` (no file extension) with:

```
repository=https://github.com/YOUR-USERNAME/clucksquad-quest-sync.git
commit=PASTE_YOUR_40_CHAR_COMMIT_HASH
authors=CluckSquad
tags=clan,quest,sync
warning=This plugin uploads your quest completion and skill levels to the CluckSquad website (clucksquad.com) so they show on the clan's Quest Calculator and highscores. Only enable it if you're OK with that.
```

3. Open a **Pull Request** from your fork to `runelite/plugin-hub`.
   The RuneLite bot will build it automatically; fix anything it flags (I can help).

## Step 3 — RuneLite review
A RuneLite developer reviews and merges. Once merged (usually days, sometimes longer), the
plugin appears in every player's **Plugin Hub** list.

### Honest heads-up
- RuneLite **requires** the `warning=` line above because the plugin sends data to an external
  site. Keep it — it's what makes reviewers (and members) comfortable.
- Plugins that upload data to a third-party site get extra scrutiny. Similar plugins exist
  (the wiki's WikiSync, Discord Rich Presence, etc.), so it's realistic — but acceptance and
  timing are RuneLite's call, not guaranteed.
- Until it's merged, members can still use the **side-load** path in the main README (works,
  but needs RuneLite's "external plugins" toggle — that's the part that feels less safe).

Want me to do Steps 1–2 with you in the browser once you're logged into GitHub? Say the word.
