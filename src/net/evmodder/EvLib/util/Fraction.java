package net.evmodder.EvLib.util;

public class Fraction implements Comparable<Fraction>{
	private int numer, denom;
	public Fraction(int a, int b){numer=a; denom=b;}
	private static int GCD(int a, int b){return b == 0 ? a : GCD(b, a % b);}
	private static int LCM(int a, int b){return (a * b) / GCD(a, b);}
	public void add(int a, int b){
		if(b != denom){
			int new_denom = LCM(denom, b);
			numer *= (new_denom / denom);
			a *= (new_denom / b);
			denom = new_denom;
		}
		numer += a;
	}
	public int take1s(){
		int whole = numer / denom;
		numer %= denom;
		return whole;
	}

	public int getNumerator(){return numer;}
	public int getDenominator(){return denom;}

	@Override public String toString(){
		return numer+"/"+denom;
	}

	@Override public boolean equals(Object o){
		return o != null && o instanceof Fraction && ((Fraction)o).numer == numer && ((Fraction)o).denom == denom;
	}

	@Override public int compareTo(Fraction o){
		long gcd = GCD(denom, o.denom);
		return Long.compare(numer*gcd, o.numer*gcd);
	}

	public static Fraction fromString(String str){
		int i = str.indexOf('/');
		if(i == -1) return null;
		try{
			return new Fraction(Integer.parseInt(str.substring(0, i)), Integer.parseInt(str.substring(i+1)));
		}
		catch(NumberFormatException ex){
			return null;
		}
	}
}