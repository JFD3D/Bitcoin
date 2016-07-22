package me.savant.bitcoin;

import me.savant.bitcoin.cex.Balance;
import me.savant.bitcoin.cex.CexAPI;
import me.savant.bitcoin.ui.UIManager;

public class Bitcoin
{
	/**
	 * CEX.IO
	 * 
	 * Buying - Buy Low
	 * Selling - Sell High
	 * 
	 * ask - sell
	 * bid - buy
	 */
	
	static BTCStats price;
	static CexAPI cex;
	
	public static void main(String[] args)
	{
		cex = new CexAPI("up102956815", "GDAdyYllI0NJgLsEqD1r6egAgZc", "zGG9AXEgM85Dc3zjhvBqiAHj78");
		UIManager manager = new UIManager(cex);
		price = new BTCStats(manager, cex);
	}
	
	public static void setCex(String name, String apiKey, String apiSecret)
	{
		cex.updateSettings(name, apiKey, apiSecret);
	}
}
