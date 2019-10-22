package net.evmodder.EvLib.extras;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.extras.ReflectionUtils.*;

public class ActionBarUtils{
	private static final RefClass classIChatBaseComponent = ReflectionUtils.getRefClass("{nms}.IChatBaseComponent");
	private static final RefClass classChatComponentText = ReflectionUtils.getRefClass("{nms}.ChatComponentText");
	private static final RefClass classChatMessageType = ReflectionUtils.getRefClass("{nms}.ChatMessageType");
	private static final RefClass classPacketPlayOutChat = ReflectionUtils.getRefClass("{nms}.PacketPlayOutChat");
	private static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
//	private static final RefClass classEntityPlayer = ReflectionUtils.getRefClass("{nms}.EntityPlayer");
//	private static final RefClass classPlayerConnection = ReflectionUtils.getRefClass("{nms}.PlayerConnection");
	private static final RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet");
	private static Object chatMessageType = null;
	static{
		for(Object enumVal : classChatMessageType.getRealClass().getEnumConstants()){
			if(enumVal.toString().equals("GAME_INFO")){
				chatMessageType = enumVal;
				break;
			}
		}
	}
	private static final RefMethod methodGetHandle = classCraftPlayer.getMethod("getHandle");
//	private static final RefMethod methodSendPacket = classPlayerConnection.getMethod("sendPacket", classPacket);
//	private static final RefField fieldPlayerConnection = classEntityPlayer.getField("playerConnection");
	private static final RefConstructor makeChatComponentText = classChatComponentText.getConstructor(String.class);
	private static final RefConstructor makePacketPlayOutChat =
			classPacketPlayOutChat.getConstructor(classIChatBaseComponent, classChatMessageType);
	
	public static void sendToPlayer(String message, Player... ppl){
		Object chatCompontentText = makeChatComponentText.create(message);
		Object packet = makePacketPlayOutChat.create(chatCompontentText, chatMessageType);
		for(Player p : ppl){
			/*
			Object entityPlayer = methodGetHandle.of(p).call();
			Object playerConnection = fieldPlayerConnection.of(entityPlayer);
			Object castPacket = classPacket.getRealClass().cast(packet);
			methodSendPacket.of(playerConnection).call(castPacket);
			*/
			Object entityPlayer = methodGetHandle.of(p).call();
			try{
				Field playerConnectionField = entityPlayer.getClass().getDeclaredField("playerConnection");
				Object playerConn = playerConnectionField.get(entityPlayer);
				Method sendPacketMethod = playerConn.getClass()
						.getDeclaredMethod("sendPacket", classPacket.getRealClass());
				sendPacketMethod.invoke(playerConn, packet);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchFieldException | SecurityException | NoSuchMethodException e){e.printStackTrace();}
		}
	}

}