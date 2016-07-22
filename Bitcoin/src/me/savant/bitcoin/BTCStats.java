package me.savant.bitcoin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class BTCStats
{
	public final String API_URL = "http://api.coindesk.com/v1/bpi/currentprice.json";
	public final String OLD_API_URL = "http://api.coindesk.com/v1/bpi/historical/close.json?for=yesterday";
	public final String CEXIO_API_URL = "http://cex.io/api/last_price/BTC/USD";
	public final String VOLATILITY_API_URL = "http://btcvol.info/latest";
	
	private float price = 0;
	private float yesterdaysPrice = 0;
	private float cex_price = 0;
	private float volatility;
	private String difference = "";
	
	private Window window;
	private CexAPI cex;
	
	public BTCStats(Window window, CexAPI cex)
	{
		this.window = window;
		this.cex = cex;
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				update();
			}
		});
		thread.start();
	}
		
	void update()
	{
		price = getPrice();
		yesterdaysPrice = getYesterdaysPrice();
		difference = getDifference();
		volatility = getVolatility();
		cex_price = getExactPrice();
		
		window.updatePrice(price, yesterdaysPrice, difference, cex_price, volatility);
		window.setVisible(true);
		
		System.out.println("[BTCPrice - USD] CoinDesk Price: " + price + " Yesterday's CoinDesk Price: " + yesterdaysPrice + " CoinDesk Difference: " + difference + " CEX.io Price: " + cex_price + " btcvol.info Volatility: " + volatility);
		
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		update();
	}
	
	float getExactPrice()
	{
		JsonParser jp = new JsonParser();
		JsonElement root = jp.parse(cex.lastPrice("BTC", "USD"));
		return root.getAsJsonObject().get("lprice").getAsFloat();
	}
	
	float getVolatility()
	{
		try
		{
			URL url = new URL(VOLATILITY_API_URL);
			WebClient wc = new WebClient();
			
			wc.getBrowserVersion().setUserAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
			wc.getOptions().setUseInsecureSSL(true);
			HtmlPage page = wc.getPage(url);
			WebResponse response = page.getWebResponse();
			JsonParser jp = new JsonParser();
			JsonElement root = jp.parse(response.getContentAsString());
			return (float)roundFloat(root.getAsJsonObject().get("Volatility").getAsFloat());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0f;
	}
	
	float getPrice()
	{
		try
		{
			URL url = new URL(API_URL);
			
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();
			JsonParser jp = new JsonParser();
			JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
			return root.getAsJsonObject().getAsJsonObject("bpi").getAsJsonObject("USD").get("rate").getAsFloat();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0f;
	}
	
	float getYesterdaysPrice()
	{
		try
		{
			URL url = new URL(OLD_API_URL);
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();
			JsonParser jp = new JsonParser();
			JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
			return Float.parseFloat(root.getAsJsonObject().getAsJsonObject("bpi").toString().split(":")[1].replace("}", ""));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0f;
	}
	
	String getDifference()
	{
		String difference = "-";
		if(price > yesterdaysPrice)
		{
			double increase = roundFloat(price - yesterdaysPrice);
			difference = "+" + increase;
		}
		else
		{
			double decrease = roundFloat(yesterdaysPrice - price);
			difference = "-" + decrease;
		}
		return difference;
	}

	
	private double roundFloat(float value)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		Number n = value;
		return Double.parseDouble(df.format(n.doubleValue()));
	}
}