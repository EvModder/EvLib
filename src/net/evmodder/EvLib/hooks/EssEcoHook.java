package net.evmodder.EvLib.hooks;

import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class EssEcoHook {
	@SuppressWarnings("deprecation")
	public static double getBalance(OfflinePlayer p){
		try{return Economy.getMoneyExact(p.getName()).doubleValue();}
		catch(UserDoesNotExistException e){return 0D;}
	}

	@SuppressWarnings("deprecation")
	public static boolean hasAtLeast(OfflinePlayer p, double amount){
		try{return Economy.hasEnough(p.getName(), new BigDecimal(amount));}
		catch(UserDoesNotExistException e){return false;}
	}

	@Deprecated //Note: Deprecated only to discourage use in preference of serverToPlayer()
	public static boolean giveMoney(OfflinePlayer p, double amount){
		if(p == null) return false;
		try{Economy.add(p.getName(), new BigDecimal(amount));}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}
	@Deprecated //Note: Deprecated only to discourage use in preference of serverToPlayer()
	public static boolean giveMoney(OfflinePlayer p, BigDecimal amount){
		if(p == null) return false;
		try{Economy.add(p.getName(), amount);}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}

	@Deprecated //Note: Deprecated only to discourage use in preference of playerToServer()
	public static boolean setMoney(OfflinePlayer p, double amount){
		if(p == null) return false;
		try{Economy.setMoney(p.getName(), new BigDecimal(amount));}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){
			Bukkit.getLogger().warning(ChatColor.RED+"EssEcoHook: Failure in setMoney() - NoLoanPermittedException");
			return false;
		}
		catch(UserDoesNotExistException e){
			Bukkit.getLogger().warning(ChatColor.RED+"EssEcoHook: Failure in setMoney() - UserDoesNotExistException");
			return false;
		}
		return true;
	}

	@Deprecated //Note: Deprecated in preference of playerToServer()
	public static boolean chargeFee(OfflinePlayer p, double amount){
		// check money
		try{if(Economy.hasEnough(p.getName(), new BigDecimal(amount)) == false) return false;}
		catch(UserDoesNotExistException e){return false;}

		// take money
		try{Economy.substract(p.getName(), new BigDecimal(amount));}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}

	@Deprecated //Note: Deprecated in preference of playerToServer()
	public static boolean chargeFee(OfflinePlayer p, BigDecimal amount){
		// check money
		try{if(Economy.hasEnough(p.getName(), amount) == false) return false;}
		catch(UserDoesNotExistException e){return false;}

		// take money
		try{Economy.substract(p.getName(), amount);}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}
}