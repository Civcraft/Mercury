package vg.civcraft.mc.mercury;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class PlayerDetails {
	public static final Type LIST_TYPE = new TypeToken<List<PlayerDetails>>(){}.getType();

	public static PlayerDetails deserialize(String json) {
		Gson gson = new Gson();
		try {
			return (PlayerDetails) gson.fromJson(json, PlayerDetails.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

	public static String serializeList(List<PlayerDetails> detailsList) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			return gson.toJson(detailsList, LIST_TYPE);
		} catch (JsonIOException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<PlayerDetails> deserializeList(String json) {
		Gson gson = new Gson();
		try {
			return (List<PlayerDetails>) gson.fromJson(json, LIST_TYPE);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

	public PlayerDetails() {}

	public PlayerDetails(UUID aid, String pn, String sn) {
		reset(aid, pn, sn);
	}

	public void reset(UUID aid, String pn, String sn) {
		this.accountId = Preconditions.checkNotNull(aid);
		this.playerName = Preconditions.checkNotNull(pn);
		this.serverName = Preconditions.checkNotNull(sn);
	}

	public UUID getAccountId() {
		return this.accountId;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public String getServerName() {
		return this.serverName;
	}

	public String serialize() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			return gson.toJson(this);
		} catch (JsonIOException e) {
			return null;
		}
	}

	private UUID accountId;
	private String playerName;
	private String serverName;
}
