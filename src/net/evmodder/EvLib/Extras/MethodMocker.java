package net.evmodder.EvLib.extras;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MethodMocker{
	public static Object getProxy(final Object s,
			final Map<String, Function> overwrites, final boolean callDefaultMethod){
		final ClassLoader classLoader = s.getClass().getClassLoader();
		final Class<?>[] interfaces = new Class[]{s.getClass()};
		final InvocationHandler invocationHandler = new InvocationHandler() {
			@Override
			public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
				Function addedFunction = overwrites.get(method.getName());
				if(addedFunction != null){
					if(callDefaultMethod) addedFunction.apply(args);
					else return addedFunction.apply(args);
				}
				return method.invoke(s, args);
			}
		};
		return Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
	}

	public static class MessageInterceptor{
		final ArrayList<String> msgs;
		final CommandSender proxy;

		public MessageInterceptor(final CommandSender p, boolean hideInterceptedMessages){
			msgs = new ArrayList<String>();
			final ClassLoader classLoader = p.getClass().getClassLoader();
			final Class<?>[] interfaces = new Class<?>[]{
				p instanceof Player ? Player.class :
				p instanceof ConsoleCommandSender ? ConsoleCommandSender.class :
				p.getClass().getSuperclass() };
			final InvocationHandler invocationHandler = new InvocationHandler(){
				@Override
				public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
					if(method.getName().equals("sendMessage")){
						System.out.println("sendMessage(args="+args.length+") intercepted: "+args[0]);
						for(Object arg : args) msgs.add((String)arg);
						System.out.println("New msgs.size(): "+msgs.size());
						if(hideInterceptedMessages) return null;
					}
					return method.invoke(p, args);
				}
			};
			proxy = (CommandSender)Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
		}
		public ArrayList<String> getMessages(){return msgs;}
		public String lastMessage(){return msgs.get(msgs.size() - 1);}
		public CommandSender getProxy(){return proxy;}
	}
}