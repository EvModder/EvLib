package net.evmodder.EvLib.extras;

import java.util.UUID;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.extras.ReflectionUtils.*;

public class ActionBarUtils{
	// Get playerConnection of EntityPlayer
	private static final RefClass classEntityPlayer = ReflectionUtils.getRefClass("{nms}.EntityPlayer", "{nm}.server.level.EntityPlayer");
	private static final RefClass classPlayerConnection = ReflectionUtils.getRefClass("{nms}.PlayerConnection", "{nm}.server.network.PlayerConnection");
	private static final RefField fieldPlayerConnection = classEntityPlayer.findField(classPlayerConnection);

	// Get conn.sendPacket(Packet<?> p);
	private static final RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet", "{nm}.network.protocol.Packet");
	private static final RefMethod methodSendPacket = classPlayerConnection.findMethod(/*isStatic=*/false, Void.TYPE, classPacket);

	// Get EntityPlayer from Player
	private static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	private static final RefMethod methodGetHandle = classCraftPlayer.getMethod("getHandle");


	private static final RefClass classIChatBaseComponent =
			ReflectionUtils.getRefClass("{nms}.IChatBaseComponent", "{nm}.network.chat.IChatBaseComponent");

	// pre-1.19
	private static Object chatMessageType = null;
	private static RefConstructor makeChatComponentText = null;
	private static RefConstructor makePacketPlayOutChat = null;
	private static UUID SENDER_UUID = null;

	// post-1.19
	private static RefConstructor makeLiteralContents = null;
	private static RefMethod makeIChatMutableComponent = null;
	private static RefConstructor makeClientboundSystemChatPacket = null;
	static{
		try{
			RefClass classChatComponentText = ReflectionUtils.getRefClass("{nms}.ChatComponentText", "{nm}.network.chat.ChatComponentText");
			RefClass classPacketPlayOutChat = ReflectionUtils.getRefClass("{nms}.PacketPlayOutChat", "{nm}.network.protocol.game.PacketPlayOutChat");
			RefClass classChatMessageType = ReflectionUtils.getRefClass("{nms}.ChatMessageType", "{nm}.network.chat.ChatMessageType");
			for(Object enumVal : classChatMessageType.getRealClass().getEnumConstants()){
				if(enumVal.toString().equals("GAME_INFO")){
					chatMessageType = enumVal;
					break;
				}
			}
			makeChatComponentText = classChatComponentText.getConstructor(String.class);
			makePacketPlayOutChat = classPacketPlayOutChat.getConstructor(classIChatBaseComponent, classChatMessageType, UUID.class);
			SENDER_UUID = UUID.randomUUID();
		}
		catch(RuntimeException ex){//class not found implies 1.19+
			RefClass classLiteralContents = ReflectionUtils.getRefClass("{nm}.network.chat.contents.LiteralContents");
			RefClass classComponentContents = ReflectionUtils.getRefClass("{nm}.network.chat.ComponentContents");
			RefClass classIChatMutableComponent = ReflectionUtils.getRefClass("{nm}.network.chat.IChatMutableComponent");
			RefClass classClientboundSystemChatPacket = ReflectionUtils.getRefClass("{nm}.network.protocol.game.ClientboundSystemChatPacket");
			makeLiteralContents = classLiteralContents.getConstructor(String.class);
			makeIChatMutableComponent = classIChatMutableComponent.findMethod(/*isStatic=*/true, classIChatMutableComponent, classComponentContents);
			makeClientboundSystemChatPacket = classClientboundSystemChatPacket.getConstructor(classIChatBaseComponent, int.class);
		}
	}

	public static void sendToPlayer(String message, Player... ppl){
		Object packet;
		if(makePacketPlayOutChat != null){
			Object chatComp = makeChatComponentText.create(message);
			packet = makePacketPlayOutChat.create(chatComp, chatMessageType, SENDER_UUID);
		}
		else{
			Object chatComp = makeIChatMutableComponent.call(makeLiteralContents.create(message));
			packet = makeClientboundSystemChatPacket.create(chatComp, /*typeId=*/2); // 2=GAME_INFO, I think
		}
		for(Player p : ppl){
			Object entityPlayer = methodGetHandle.of(p).call();
			Object playerConnection = fieldPlayerConnection.of(entityPlayer).get();
			Object castPacket = classPacket.getRealClass().cast(packet);
			methodSendPacket.of(playerConnection).call(castPacket);
		}
	}
}