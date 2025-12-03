package net.evmodder.EvLib.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;
import io.netty.channel.Channel;
import net.evmodder.EvLib.util.ReflectionUtils;

public class PacketUtils{
/*
	RefClass packetPlayInChat = ReflectionUtils.getRefClass("{nms}.PacketPlayInChat", "{nm}.network.protocol.game.PacketPlayInChat");
	RefClass packetPlayOutChat = ReflectionUtils.getRefClass("{nms}.PacketPlayOutChat", "{nm}.network.protocol.game.PacketPlayOutChat");
	private void injectPlayer(Player player){
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			@Override public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				if(packetPlayInChat.isInstance(packet)){
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"PACKET RECV: "+ChatColor.WHITE+packet.toString());
				}
				super.channelRead(context, packet);
			}
			@Override public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
				if(packetPlayOutChat.isInstance(packet)){
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"PACKET SENT: "+ChatColor.WHITE+packet.toString());
				}
				super.write(context, packet, promise);
			}
		};
		RefClass craftPlayerClazz = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
		RefMethod playerGetHandleMethod = craftPlayerClazz.getMethod("getHandle");
		RefClass entityPlayerClazz = ReflectionUtils.getRefClass("{nms}.EntityPlayer", "{nm}.server.level.EntityPlayer");
		RefClass playerConnectionClazz = ReflectionUtils.getRefClass("{nms}.PlayerConnection", "{nm}.server.network.PlayerConnection");
		RefField playerConnectionField = entityPlayerClazz.findField(playerConnectionClazz);
		RefClass networkManagerClazz = ReflectionUtils.getRefClass("{nms}.NetworkManager", "{nm}.network.NetworkManager");
		RefField networkManagerField = playerConnectionClazz.findField(networkManagerClazz);
		RefField channelField = networkManagerClazz.findField(Channel.class);
		
		Object playerEntityObj = playerGetHandleMethod.of(player).call();
		Object playerConnectionObj = playerConnectionField.of(playerEntityObj).get();
		Object networkManagerObj = networkManagerField.of(playerConnectionObj).get();
		Channel channelObj = (Channel)channelField.of(networkManagerObj).get();
		ChannelPipeline pipeline = channelObj.pipeline();
		pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
	}
	private void removePlayer(Player player){
		Channel channel = ((CraftPlayer) player).getHandle().playerCOnnection.networkManager.channel;
		channel.eventLoop().submit(()->{
			channel.pipeline().remove(player.getName());
			return null;
		});
	}
		
	pl.getServer().getPluginManager().registerEvents(new Listener(){
		@EventHandler public void onJoin(PlayerJoinEvent evt){injectPlayer(evt.getPlayer());}
		@EventHandler public void onQuit(PlayerQuitEvent evt){removePlayer(evt.getPlayer());}
	}, pl);*/

	private final static Class<?> classCraftPlayer = ReflectionUtils.getClass("{cb}.entity.CraftPlayer");
	private final static Method method_CraftPlayer_getHandle = ReflectionUtils.getMethod(classCraftPlayer, "getHandle");
	private final static Class<?> classEntityPlayer = ReflectionUtils.getClass("{nms}.EntityPlayer", "{nm}.server.level.EntityPlayer");
	private final static Class<?> classPlayerConnection = ReflectionUtils.getClass("{nms}.PlayerConnection", "{nm}.server.network.PlayerConnection");
	private final static Field fieldPlayerConnection = ReflectionUtils.findField(classEntityPlayer, classPlayerConnection);
	private final static Class<?> classNetworkManager = ReflectionUtils.getClass("{nms}.NetworkManager", "{nm}.network.NetworkManager");
	private final static Field fieldNetworkManager;
	static{
		Field field;
		try{
			field = ReflectionUtils.findField(classPlayerConnection, classNetworkManager);
		}
		catch(RuntimeException ex){
			field = ReflectionUtils.findField(ReflectionUtils.getClass("{nm}.server.network.ServerCommonPacketListenerImpl"), classNetworkManager);
		}
		fieldNetworkManager = field;
	}
	private final static Field fieldChannel = ReflectionUtils.findField(classNetworkManager, Channel.class);

	public static Channel getPlayerChannel(Player player){
		final Object playerEntity = ReflectionUtils.call(method_CraftPlayer_getHandle, player);
		final Object playerConnection = ReflectionUtils.get(fieldPlayerConnection, playerEntity);
		final Object networkManager = ReflectionUtils.get(fieldNetworkManager, playerConnection);
		return (Channel)ReflectionUtils.get(fieldChannel, networkManager);
	}

	private final static Class<?> classPacket = ReflectionUtils.getClass("{nms}.Packet", "{nm}.network.protocol.Packet");
	private final static Method method_PlayerConnection_sendPacket = ReflectionUtils.findMethod(classPlayerConnection, /*isStatic=*/false, Void.TYPE, classPacket);
	public static void sendPacket(Player player, Object packet){
		Object entityPlayer = ReflectionUtils.call(method_CraftPlayer_getHandle, player);
		Object playerConnection = ReflectionUtils.get(fieldPlayerConnection, entityPlayer);
		ReflectionUtils.call(method_PlayerConnection_sendPacket, playerConnection, classPacket.cast(packet));
	}
}
