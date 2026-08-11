package net.evmodder.EvLib.util;

import java.lang.reflect.Method;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Selects the host application's logger without adding Bukkit or SLF4J as EvLib dependencies. */
public final class LoggerUtils{
	private LoggerUtils(){}

	private static final Formatter MESSAGE_FORMATTER = new Formatter(){
		@Override public String format(final LogRecord record){return formatMessage(record);}
	};

	private static final class ForwardingLogger extends Logger{
		ForwardingLogger(final String name, final Handler handler){
			super(name, null);
			setLevel(Level.ALL);
			setUseParentHandlers(false);
			handler.setLevel(Level.ALL);
			addHandler(handler);
		}
	}

	private static final class Slf4jHandler extends Handler{
		private final Object logger;
		private final Method[] messageMethods = new Method[5], throwableMethods = new Method[5];

		Slf4jHandler(final Class<?> loggerType, final Object logger) throws NoSuchMethodException{
			this.logger = logger;
			final String[] levels = {"error", "warn", "info", "debug", "trace"};
			for(int i=0; i<levels.length; ++i){
				messageMethods[i] = loggerType.getMethod(levels[i], String.class);
				throwableMethods[i] = loggerType.getMethod(levels[i], String.class, Throwable.class);
			}
		}
		@Override public void publish(final LogRecord record){
			if(!isLoggable(record)) return;
			final int level = record.getLevel().intValue() >= Level.SEVERE.intValue() ? 0
					: record.getLevel().intValue() >= Level.WARNING.intValue() ? 1
					: record.getLevel().intValue() >= Level.CONFIG.intValue() ? 2
					: record.getLevel().intValue() >= Level.FINE.intValue() ? 3 : 4;
			try{
				final String message = MESSAGE_FORMATTER.format(record);
				if(record.getThrown() == null) messageMethods[level].invoke(logger, message);
				else throwableMethods[level].invoke(logger, message, record.getThrown());
			}
			catch(ReflectiveOperationException | RuntimeException ignored){}
		}
		@Override public void flush(){}
		@Override public void close(){}
	}

	private static Logger getBukkitLogger(){
		try{
			final Object logger = Class.forName("org.bukkit.Bukkit").getMethod("getLogger").invoke(null);
			return logger instanceof Logger ? (Logger)logger : null;
		}
		catch(ReflectiveOperationException | LinkageError | SecurityException ex){return null;}
	}
	private static Logger getSlf4jLogger(final String name){
		try{
			final Class<?> loggerType = Class.forName("org.slf4j.Logger");
			final Class<?> loggerFactory = Class.forName("org.slf4j.LoggerFactory");
			final String factoryName = loggerFactory.getMethod("getILoggerFactory").invoke(null).getClass().getName();
			if(factoryName.endsWith(".NOPLoggerFactory") || factoryName.endsWith(".SubstituteLoggerFactory")) return null;
			final Object logger = loggerFactory.getMethod("getLogger", String.class).invoke(null, name);
			return new ForwardingLogger(name, new Slf4jHandler(loggerType, logger));
		}
		catch(ReflectiveOperationException | LinkageError | SecurityException ex){return null;}
	}

	public static Logger getLogger(final String name){
		final Logger serverLogger = getBukkitLogger();
		if(serverLogger != null) return serverLogger;
		final Logger modLogger = getSlf4jLogger(name);
		return modLogger == null ? Logger.getLogger(name) : modLogger;
	}
}