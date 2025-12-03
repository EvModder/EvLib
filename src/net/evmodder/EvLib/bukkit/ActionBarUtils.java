package net.evmodder.EvLib.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.util.ReflectionUtils;

public final class ActionBarUtils{
	// Get playerConnection of EntityPlayer
	private static final Class<?> classEntityPlayer = ReflectionUtils.getClass("{nms}.EntityPlayer", "{nm}.server.level.EntityPlayer");
	private static final Class<?> classPlayerConnection = ReflectionUtils.getClass("{nms}.PlayerConnection", "{nm}.server.network.PlayerConnection");
	private static final Field fieldPlayerConnection = ReflectionUtils.findField(classEntityPlayer, classPlayerConnection);

	// Get conn.sendPacket(Packet<?> p);
	private static final Class<?> classPacket = ReflectionUtils.getClass("{nms}.Packet", "{nm}.network.protocol.Packet");
	private static final Method method_Packet_sendPacket = ReflectionUtils.findMethod(classPlayerConnection, /*isStatic=*/false, Void.TYPE, classPacket);

	// Get EntityPlayer from Player
	private static final Class<?> classCraftPlayer = ReflectionUtils.getClass("{cb}.entity.CraftPlayer");
	private static final Method method_CraftPlayer_getHandle = ReflectionUtils.getMethod(classCraftPlayer, "getHandle");


	private static final Class<?> classIChatBaseComponent = ReflectionUtils.getClass("{nms}.IChatBaseComponent", "{nm}.network.chat.IChatBaseComponent");

	// pre-1.19
	private static Object chatMessageType = null;
	private static Constructor<?> cnstr_ChatComponentText = null;
	private static Constructor<?> cnstr_PacketPlayOutChat = null;
	private static UUID SENDER_UUID = null;

	// post-1.19
	private static Constructor<?> cnstr_LiteralContents = null;//pre 1.21
	private static Method method_createIChatMutableComponent = null;
	private static Constructor<?> cnstr_ClientboundSystemChatPacket = null;
	private static boolean useBool = false;
	//post-1.21
	private static Method method_createLiteralContents = null;
	static{
		try{
			Class<?> classChatComponentText = ReflectionUtils.getClass("{nms}.ChatComponentText", "{nm}.network.chat.ChatComponentText");
			Class<?> classPacketPlayOutChat = ReflectionUtils.getClass("{nms}.PacketPlayOutChat", "{nm}.network.protocol.game.PacketPlayOutChat");
			Class<?> classChatMessageType = ReflectionUtils.getClass("{nms}.ChatMessageType", "{nm}.network.chat.ChatMessageType");
			for(Object enumVal : classChatMessageType.getEnumConstants()){
				if(enumVal.toString().equals("GAME_INFO")){
					chatMessageType = enumVal;
					break;
				}
			}
			cnstr_ChatComponentText = classChatComponentText.getConstructor(String.class);
			cnstr_PacketPlayOutChat = classPacketPlayOutChat.getConstructor(classIChatBaseComponent, classChatMessageType, UUID.class);
			SENDER_UUID = UUID.randomUUID();
		}
		catch(RuntimeException | NoSuchMethodException e1){//class not found implies 1.19+
			Class<?> classLiteralContents = ReflectionUtils.getClass("{nm}.network.chat.contents.LiteralContents");
			Class<?> classComponentContents = ReflectionUtils.getClass("{nm}.network.chat.ComponentContents");
			Class<?> classIChatMutableComponent = ReflectionUtils.getClass("{nm}.network.chat.IChatMutableComponent");
			Class<?> classClientboundSystemChatPacket = ReflectionUtils.getClass("{nm}.network.protocol.game.ClientboundSystemChatPacket");
			try{cnstr_LiteralContents = classLiteralContents.getConstructor(String.class);}
			catch(RuntimeException | NoSuchMethodException e2){//class not found implies 1.21+
				method_createLiteralContents = ReflectionUtils.findMethod(classLiteralContents, /*isStatic=*/true, classLiteralContents, String.class);
			}
			method_createIChatMutableComponent = ReflectionUtils.findMethod(classIChatMutableComponent, /*isStatic=*/true, classIChatMutableComponent, classComponentContents);
			try{cnstr_ClientboundSystemChatPacket = classClientboundSystemChatPacket.getConstructor(classIChatBaseComponent, int.class);}//1.19.0-1.19.1
			catch(RuntimeException | NoSuchMethodException e3){
				try{cnstr_ClientboundSystemChatPacket = classClientboundSystemChatPacket.getConstructor(classIChatBaseComponent, boolean.class);}
				catch(NoSuchMethodException | SecurityException e){e.printStackTrace();}
				useBool = true;
			}
		}
	}

	public static void sendToPlayer(String message, Player... ppl){
		try{
			final Object packet;
			if(cnstr_PacketPlayOutChat != null){
				Object chatComp = cnstr_ChatComponentText.newInstance(message);
				packet = cnstr_PacketPlayOutChat.newInstance(chatComp, chatMessageType, SENDER_UUID);
			}
			else{
				Object comp;
				if(method_createLiteralContents != null) comp = ReflectionUtils.callStatic(method_createLiteralContents, message);
				else comp = cnstr_LiteralContents.newInstance(message);
				Object chatComp = ReflectionUtils.callStatic(method_createIChatMutableComponent, comp);
				packet = cnstr_ClientboundSystemChatPacket.newInstance(chatComp, useBool ? true : /*typeId=*/2); // 2=GAME_INFO, I think
			}
			for(Player p : ppl){
				Object entityPlayer = ReflectionUtils.call(method_CraftPlayer_getHandle, p);
				Object playerConnection = fieldPlayerConnection.get(entityPlayer);
				ReflectionUtils.call(method_Packet_sendPacket, playerConnection, classPacket.cast(packet));
			}
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			e.printStackTrace();
		}
	}
}