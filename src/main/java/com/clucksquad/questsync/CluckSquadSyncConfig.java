package com.clucksquad.questsync;
 
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
 
@ConfigGroup("clucksquadsync")
public interface CluckSquadSyncConfig extends Config
{
	@ConfigItem(
		keyName = "endpoint",
		name = "Endpoint URL",
		description = "CluckSquad sync endpoint. Leave as-is unless you host the site elsewhere.",
		position = 1
	)
	default String endpoint()
	{
		return "https://clucksquad.com/wp-admin/admin-ajax.php";
	}
 
	// Clan sync key baked in so members don't have to paste anything.
	// This is the real CluckSquad clan key (read from the site). Members can still override it below.
	String DEFAULT_SYNC_KEY = "eO5wqmtizeeTNv6jXgWiMtFQBH34";
 
	@ConfigItem(
		keyName = "syncKey",
		name = "Sync key",
		description = "Pre-filled with the CluckSquad clan key. Only change this if an admin tells you to.",
		position = 2
	)
	default String syncKey()
	{
		return DEFAULT_SYNC_KEY;
	}
 
	@ConfigItem(
		keyName = "syncOnLogin",
		name = "Sync after login",
		description = "Automatically upload your quests and levels shortly after you log in.",
		position = 3
	)
	default boolean syncOnLogin()
	{
		return true;
	}
 
	@ConfigItem(
		keyName = "periodicMinutes",
		name = "Re-sync every (minutes)",
		description = "Also re-upload periodically while logged in. 0 disables periodic syncing.",
		position = 4
	)
	default int periodicMinutes()
	{
		return 15;
	}
}
