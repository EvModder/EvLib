package net.evmodder.EvLib.extras;

import org.bukkit.entity.Player;
import io.netty.channel.Channel;
import net.evmodder.EvLib.extras.ReflectionUtils.RefClass;
import net.evmodder.EvLib.extras.ReflectionUtils.RefField;
import net.evmodder.EvLib.extras.ReflectionUtils.RefMethod;

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

	private final static RefClass craftPlayerClazz = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	private final static RefMethod playerGetHandleMethod = craftPlayerClazz.getMethod("getHandle");
	private final static RefClass entityPlayerClazz = ReflectionUtils.getRefClass("{nms}.EntityPlayer", "{nm}.server.level.EntityPlayer");
	private final static RefClass playerConnectionClazz = ReflectionUtils.getRefClass("{nms}.PlayerConnection", "{nm}.server.network.PlayerConnection");
	private final static RefField playerConnectionField = entityPlayerClazz.findField(playerConnectionClazz);
	private final static RefClass networkManagerClazz = ReflectionUtils.getRefClass("{nms}.NetworkManager", "{nm}.network.NetworkManager");
	private final static RefField networkManagerField;
	static{
		RefField field;
		try{
			field = playerConnectionClazz.findField(networkManagerClazz);
		}
		catch(RuntimeException ex){
			field = ReflectionUtils.getRefClass("{nm}.server.network.ServerCommonPacketListenerImpl").findField(networkManagerClazz);
		}
		networkManagerField = field;
	}
	private final static RefField channelField = networkManagerClazz.findField(Channel.class);

	public static Channel getPlayerChannel(Player player){
		final Object playerEntity = playerGetHandleMethod.of(player).call();
		final Object playerConnection = playerConnectionField.of(playerEntity).get();
		final Object networkManager = networkManagerField.of(playerConnection).get();
		return (Channel)channelField.of(networkManager).get();
	}

	private final static RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet", "{nm}.network.protocol.Packet");
	private final static RefMethod sendPacketMethod = playerConnectionClazz.findMethod(/*isStatic=*/false, Void.TYPE, classPacket);
	public static void sendPacket(Player player, Object packet){
		Object entityPlayer = playerGetHandleMethod.of(player).call();
		Object playerConnection = playerConnectionField.of(entityPlayer).get();
		Object castPacket = classPacket.getRealClass().cast(packet);
		sendPacketMethod.of(playerConnection).call(castPacket);
	}
}
