package net.evmodder.EvLib.util;

public enum Command{
	PING(true),
	REQUEST_CLIENT_KEY(true),

	DB_PEARL_FETCH_BY_UUID(true),
	DB_PEARL_FETCH_BY_XZ(true),
	DB_PEARL_STORE_BY_UUID(true),
	DB_PEARL_STORE_BY_XZ(true),
	DB_PEARL_XZ_KEY_UPDATE(true), // Deprecated (unused)

	DB_MAPART_STORE(true),

	DB_PLAYER_FETCH_QUIT_TS(true),
	DB_PLAYER_STORE_QUIT_TS(false),
	DB_PLAYER_FETCH_JOIN_TS(true),
	DB_PLAYER_STORE_JOIN_TS(false),

	DB_PLAYER_STORE_IGNORE(true),
	DB_PLAYER_STORE_UNIGNORE(true),
	DB_PLAYER_FETCH_IGNORES(true),

	P2P_PEARL_REGISTER(true),
	P2P_PEARL_PULL(true),

	P2P_CHAT_AS(false), // Send chat/cmds as another player

	RESOLVE_SERVER_ID(true);

	private final boolean sendsResponse;
	Command(final boolean sendsResponse){this.sendsResponse = sendsResponse;}
	public boolean sendsResponse(){return sendsResponse;}
	public boolean requiresServerId(){return this != PING && this != REQUEST_CLIENT_KEY && this != RESOLVE_SERVER_ID;}


	// Commands (0-255)
//	public static final int PING = 0;
//	public static final int EPEARL_TRIGGER = 1;
//	public static final int EPEARL_OWNER_FETCH = 2;//key is pearl entity UUID
//	public static final int EPEARL_OWNER_STORE = 3;
//	public static final int SEND_CHAT_AS = 4;//TODO: send to server (which will pass on to other client) vs send directly to client. many similar such cmds
//	public static final int GET_LIST = 5;

//	// Remaining bytes - command variants
//	public static final int EPEARL_UUID = 0<<8;
//	public static final int EPEARL_XZ = 1<<8;
//	public static final int EPEARL_XZ_KEY_UPDATE = 2<<8;
//
//	public static final int P2P_SEND_CHAT_AS = 0<<8;
//	public static final int P2P_EPEARL_TRIGGER = 0<<8;
}