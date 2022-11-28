package de.flokyy.aurora.utils;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.flokyy.aurora.mysql.MySQLStatements;
import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CollectionData {

	public static int percent(int a, int b) {
		float result = 0;
		result = ((b - a) * 100) / a;

		return (int) result;
	}

		
	
	//Uses the MagicEden API to get the current floor price of a collection
	public static Double getCollectionFP(String collection) {
		try {
		 
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			Request request = new Request.Builder().url("https://api-mainnet.magiceden.dev/v2/collections/" + collection).method("GET", null).build();
			Response response = null;
			
			
			try {
				response = client.newCall(request).execute();
			} catch (IOException e2) {
				return 0.0;
			}
			String s = null;
			try {
				s = response.body().string();
			} catch (IOException e2) {
				return 0.0;
			}
		
			JSONParser parser = new JSONParser();

			JSONObject json = (JSONObject) parser.parse(s);

			JSONObject jsonObject = (JSONObject) json;

	
			
			Object fp = null;
			
			try {
				fp = (Long) jsonObject.get("floorPrice");
			}
			catch(Exception e) {
				return 0.0;
			}
				
				
				
			Long fpLong = (Long) fp;
			long length = (long) (Math.log10(fpLong) + 1);
			double fpREAL = 0;

			// VOLUME
			if (length != 0) {

				if (length >= 9) {
					fpREAL = (fpLong.doubleValue() / 1000000000);

				}

				if (length <= 8) {
					fpREAL = (fpLong.doubleValue() / 10000000000L);

				}
			}

		
			return fpREAL;
			
			
		}
		catch(Exception e) {
			System.out.println("Couldnt fetch FP for: " + collection);
			return 0.0;
		}

	}

}
