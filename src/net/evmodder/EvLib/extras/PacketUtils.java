package net.evmodder.EvLib.extras;

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
}
