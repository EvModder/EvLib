package net.evmodder.EvLib.util;

public class Pair<T/* extends Comparable<T>*/, R/* extends Comparable<R>*/> implements Comparable<Pair<T, R>>{
	public final T a; public final R b;
	public Pair(T t, R r){a=t; b=r;}
	@Override public boolean equals(Object p){
		return p != null && p instanceof Pair && a.equals(((Pair<?, ?>)p).a) && b.equals(((Pair<?, ?>)p).b);
	}
	@Override public int hashCode(){
		return (a == null ? 0 : a.hashCode()) + (b == null ? 0 : b.hashCode());
	}

	@Override @SuppressWarnings({ "rawtypes", "unchecked" })
	public int compareTo(Pair<T, R> o){
		int tComp = ((Comparable)a).compareTo(o.a);
		return tComp != 0 ? tComp : ((Comparable)b).compareTo(o.b);
	}

/*	@Override
	public String toString(){
		return new StringBuilder().append(a).append(',').append(b).toString();
	}*/

	public T getFirst(){return a;}
	public R getSecond(){return b;}
}