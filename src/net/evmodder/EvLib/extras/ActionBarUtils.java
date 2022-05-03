package net.evmodder.EvLib.extras;

import java.util.UUID;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.extras.ReflectionUtils.*;

public class ActionBarUtils{
	private static final RefClass classEntityPlayer = ReflectionUtils.getRefClass("{nms}.EntityPlayer", "{nm}.server.level.EntityPlayer");
	private static final RefClass classPlayerConnection = ReflectionUtils.getRefClass("{nms}.PlayerConnection", "{nm}.server.network.PlayerConnection");
	private static final RefClass classIChatBaseComponent = ReflectionUtils.getRefClass(
			"{nms}.IChatBaseComponent", "{nm}.network.chat.IChatBaseComponent");
	private static final RefClass classChatComponentText = ReflectionUtils.getRefClass(
			"{nms}.ChatComponentText", "{nm}.network.chat.ChatComponentText");
	private static final RefClass classChatMessageType = ReflectionUtils.getRefClass(
			"{nms}.ChatMessageType", "{nm}.network.chat.ChatMessageType");
	private static final RefClass classPacketPlayOutChat = ReflectionUtils.getRefClass(
			"{nms}.PacketPlayOutChat", "{nm}.network.protocol.game.PacketPlayOutChat");
	private static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	private static final RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet", "{nm}.network.protocol.Packet");
	private static Object chatMessageType = null;
	static{
		for(Object enumVal : classChatMessageType.getRealClass().getEnumConstants()){
			if(enumVal.toString().equals("GAME_INFO")){
				chatMessageType = enumVal;
				break;
			}
		}
	}
	private static final RefMethod methodSendPacket = classPlayerConnection.findMethod(/*isStatic=*/false, Void.TYPE, classPacket);
	private static final RefField fieldPlayerConnection = classEntityPlayer.findField(classPlayerConnection);
	private static final RefMethod methodGetHandle = classCraftPlayer.getMethod("getHandle");
	private static final RefConstructor makeChatComponentText = classChatComponentText.getConstructor(String.class);
	private static final RefConstructor makePacketPlayOutChat =
			classPacketPlayOutChat.getConstructor(classIChatBaseComponent, classChatMessageType, UUID.class);

	final static UUID SENDER_UUID = UUID.randomUUID();
	public static void sendToPlayer(String message, Player... ppl){
		Object chatCompontentText = makeChatComponentText.create(message);
		Object packet = makePacketPlayOutChat.create(chatCompontentText, chatMessageType, SENDER_UUID);
		for(Player p : ppl){
			Object entityPlayer = methodGetHandle.of(p).call();
			Object playerConnection = fieldPlayerConnection.of(entityPlayer).get();
			Object castPacket = classPacket.getRealClass().cast(packet);
			methodSendPacket.of(playerConnection).call(castPacket);
		}
	}

}