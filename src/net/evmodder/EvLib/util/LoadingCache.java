package net.evmodder.EvLib.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class LoadingCache<K, V>{
	private final HashMap<K, V> cache;
	//TODO: loading duration tracking?
	private final HashMap<K, Thread> loading = new HashMap<>();
	private final V V_LOADING;
	boolean alwaysLoadSync;
	public LoadingCache(final HashMap<K, V> initMap, final V loading){cache = initMap; V_LOADING = loading;}
	public LoadingCache(final V loading){this(new HashMap<>(), loading);}
	public LoadingCache(){this(new HashMap<>(), null);}

	protected abstract V load(final K k);
	protected V loadSyncOrNull(final K k){return null;}
	public final V remove(final K k){synchronized(cache){return cache.remove(k);}}
	public final int size(){synchronized(cache){return cache.size();}}
	public final boolean contains(final K k){synchronized(cache){return cache.containsKey(k);}}
	public final V getCached(final K k){synchronized(cache){return cache.get(k);}}
	public final Map<K, V> getCache(){return Collections.unmodifiableMap(cache);} // Only accessed by EpearlLookup

	public final V putIfAbsent(final K k, final V v){
		final V oldV;
		synchronized(cache){oldV = cache.putIfAbsent(k, v);}
		if(oldV != null) return oldV;
		final Thread t;
		synchronized(loading){t = loading.remove(k);}
		if(t != null) t.interrupt();
		return null;
	}
	public final V put(final K k, final V v){
		final V oldV;
		synchronized(cache){oldV = cache.put(k, v);}
		if(oldV != null) return oldV;
		final Thread t;
		synchronized(loading){t = loading.remove(k);}
		if(t != null) t.interrupt();
		return null;
	}

//	public final boolean containsKey(final K k){return cache.containsKey(k);}
	private final void loadValue(final K k, final Consumer<V> callback){
		synchronized(loading){
			if(!loading.containsKey(k)){
				final Thread t = new Thread(()->{
					final V v = load(k);
					if(v == null ? V_LOADING == null : v.equals(V_LOADING)){
						synchronized(loading){if(loading.containsKey(k)) loading.put(k, null);} // null out this Thread so it can be garbage collected
						return; // Assume the caller will call putIfAbsent() once a value is loaded
					}
					synchronized(cache){cache.put(k, v);}
					synchronized(loading){loading.remove(k);}
					if(callback != null) callback.accept(v);
				});
				loading.put(k, t);
				t.start();
			}
		}
	}
	public final V get(final K k, final Consumer<V> callback){
		{
			final V v = loadSyncOrNull(k);
			if(v != null){
				assert v != V_LOADING : "loadSyncOrNull() failed to do what its name implies (didn't load, yet didn't return null)";
				synchronized(cache){cache.put(k, v);}
				if(callback != null) callback.accept(v);
				return v;
			}
		}
		final V v;
		synchronized(cache){
			if(!cache.containsKey(k)){
				loadValue(k, callback);
				return V_LOADING;
			}
			v = cache.get(k);
		}
		if(callback != null) callback.accept(v);
		return v;
	}
	public final V getSync(final K k){
		{
			final V v = loadSyncOrNull(k);
			if(v != null){
				assert v != V_LOADING : "loadSyncOrNull() failed to do what its name implies (didn't load, yet didn't return null";
				synchronized(cache){cache.put(k, v);}
				return v;
			}
		}
		synchronized(cache){if(cache.containsKey(k)) return cache.get(k);}

		final boolean iAmTheLoader;
//		assert Thread.currentThread() != null; // iirc, main thread id=1
		synchronized(loading){iAmTheLoader = loading.putIfAbsent(k, Thread.currentThread()) == null;}
		if(iAmTheLoader){
			final V v = load(k);
			if(!v.equals(V_LOADING)){
				synchronized(cache){cache.put(k, v);}
				synchronized(loading){loading.remove(k);}
				return v;
			}
		}
		while(true){
			// Did consider Thread.onSpinWait(), but I think Thread.yield() is actually slightly better here.
			while(!contains(k)) Thread.yield(); // Wait for loader thread or for someone to call putIfAbsent()
			synchronized(cache){
				// Perhaps insanity, but just to be safe, double-check that the cache still contains this key
				if(cache.containsKey(k)){
					if(iAmTheLoader) synchronized(loading){loading.remove(k);}
					return cache.get(k);
				}
			}
		}
	}
}