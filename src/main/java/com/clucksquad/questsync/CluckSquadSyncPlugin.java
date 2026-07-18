package com.clucksquad.questsync;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * CluckSquad Quest Sync — uploads the player's quest states and skill levels to the
 * CluckSquad Quest Calculator so they auto-tick on the website (clucksquad.com/quest-calculator/).
 *
 * There is no public OSRS quest-completion API, so this companion plugin is how the
 * website can know which quests you've done — exactly like the wiki's own WikiSync.
 */
@Slf4j
@PluginDescriptor(
	name = "CluckSquad Quest Sync",
	description = "Uploads your completed quests and skill levels to the CluckSquad Quest Calculator.",
	tags = {"clucksquad", "quest", "osrs", "sync", "cape"}
)
public class CluckSquadSyncPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private CluckSquadSyncConfig config;
	@Inject private OkHttpClient okHttpClient;
	@Inject private Gson gson;
	@Inject private ScheduledExecutorService executor;

	private ScheduledFuture<?> periodic;
	private ScheduledFuture<?> pending;
	private long lastSyncMs = 0L;

	@Provides
	CluckSquadSyncConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CluckSquadSyncConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			scheduleSync(15);
		}
		startPeriodic();
	}

	@Override
	protected void shutDown()
	{
		if (pending != null) { pending.cancel(false); }
		if (periodic != null) { periodic.cancel(false); }
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && config.syncOnLogin())
		{
			scheduleSync(20);
		}
	}

	private void startPeriodic()
	{
		if (periodic != null) { periodic.cancel(false); }
		int mins = config.periodicMinutes();
		if (mins <= 0) { return; }
		periodic = executor.scheduleWithFixedDelay(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) { collectAndSend(); }
		}, mins, mins, TimeUnit.MINUTES);
	}

	private void scheduleSync(int delaySeconds)
	{
		if (pending != null && !pending.isDone()) { return; }
		pending = executor.schedule(this::collectAndSend, delaySeconds, TimeUnit.SECONDS);
	}

	/** Read quest states + levels on the client thread, then POST off-thread. */
	private void collectAndSend()
	{
		// simple debounce: at most once per 30 seconds
		long now = System.currentTimeMillis();
		if (now - lastSyncMs < 30_000L) { return; }
		lastSyncMs = now;

		clientThread.invoke(() -> {
			if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null) { return; }
			final String rsn = client.getLocalPlayer().getName();
			if (rsn == null || rsn.isEmpty()) { return; }

			final Map<String, String> quests = new LinkedHashMap<>();
			for (Quest q : Quest.values())
			{
				try { quests.put(q.getName(), q.getState(client).name()); }
				catch (Exception ex) { /* a few quests may not resolve; skip them */ }
			}

			final Map<String, Integer> levels = new LinkedHashMap<>();
			for (Skill s : Skill.values())
			{
				try { levels.put(s.getName(), client.getRealSkillLevel(s)); }
				catch (Exception ex) { /* skip */ }
			}

			final Map<String, Object> body = new LinkedHashMap<>();
			body.put("quests", quests);
			body.put("levels", levels);
			final String payload = gson.toJson(body);

			executor.execute(() -> post(rsn, payload));
		});
	}

	private void post(String rsn, String payload)
	{
		final String key = config.syncKey() == null ? "" : config.syncKey().trim();
		if (key.isEmpty())
		{
			log.debug("CluckSquad sync: no sync key configured — set it in the plugin settings.");
			return;
		}

		final Request request = new Request.Builder()
			.url(config.endpoint())
			.post(new FormBody.Builder()
				.add("action", "cs_quest_sync")
				.add("key", key)
				.add("rsn", rsn)
				.add("payload", payload)
				.build())
			.build();

		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("CluckSquad sync failed", e);
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (Response r = response)
				{
					log.debug("CluckSquad sync response: {}", r.code());
				}
			}
		});
	}
}
