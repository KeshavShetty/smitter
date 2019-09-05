package ljmu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ProcessTweet {
	
	private static final String BASE_PATH="D:\\projects\\datascience\\stocknet-dataset-master\\tweet\\raw";	
	
	ScriptEngine engine = null;
	
	private void initJavaScriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		this.engine = factory.getEngineByName("JavaScript");
        // evaluate JavaScript code from given file - specified by first argument
        try {
        	//engine.eval(new java.io.FileReader("jquery.js"));
			engine.eval(new java.io.FileReader("afinn_en.js"));
			engine.eval(new java.io.FileReader("afinn_emoticon.js"));
	        engine.eval(new java.io.FileReader("sentiment.js"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
	}
	
	protected String javascriptExecutor(String methodName, String inputData) {
		String retValue = "";
		try {			
	        Invocable inv = (Invocable) engine;
	        Object retObj = inv.invokeFunction(methodName, inputData );
	        System.out.println(retValue);
	        retValue = retObj.toString();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return retValue;
	}
	
	private Map<String, String> processTweetString(String phrase) {
		
		Map<String, String> jsonReturnMap = new HashMap<String, String>(); 
				
		Object resp = javascriptExecutor("sentiment", phrase);
		
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject)parser.parse(resp.toString());
			
			jsonReturnMap.put("verdict", "" + (String)obj.get("verdict"));
			jsonReturnMap.put("score", "" + (Long)obj.get("score"));
			
			Object comp_obj = obj.get("comparative");
			if (comp_obj instanceof Double) jsonReturnMap.put("comparative", "" + (Double)obj.get("comparative"));
			else if (comp_obj instanceof Long) jsonReturnMap.put("comparative", "" + (Long)obj.get("comparative"));
			jsonReturnMap.put("no_of_positive_words", "" + (Long)obj.get("no_of_positive_words"));
			jsonReturnMap.put("no_of_negative_words", "" + (Long)obj.get("no_of_negative_words"));
			jsonReturnMap.put("cleaned_phrase", "" + (String)obj.get("cleaned_phrase"));
			
			String positiveWords = "";
			JSONArray jsonArray = (JSONArray) ((JSONArray)obj.get("positive")).iterator().next();
			Iterator iter = jsonArray.iterator();
			while(iter.hasNext()) {
				String nextWord = (String) iter.next();
				if (positiveWords.length()>0) positiveWords = positiveWords + ",";
				positiveWords = positiveWords + nextWord;
				//System.out.println(nextWord);
			}
			
			String negativeWords = "";
			jsonArray = (JSONArray) ((JSONArray)obj.get("negative")).iterator().next();
			iter = jsonArray.iterator();
			while(iter.hasNext()) {
				String nextWord = (String) iter.next();
				if (negativeWords.length()>0) negativeWords = negativeWords + ",";
				negativeWords = negativeWords + nextWord;
				//System.out.println(nextWord);
			}
			
			jsonReturnMap.put("positive", "" + positiveWords);
			jsonReturnMap.put("negative", "" + negativeWords);
			
			
			//System.out.println("score="+ jsonReturnMap.get("negative"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonReturnMap;
	}
	
	public static void main(String[] args) {
		
		ProcessTweet pocessTweet = new ProcessTweet();
		pocessTweet.initJavaScriptEngine();
		
		pocessTweet.processTweetString("Warning Bells Are Sounding Ever Louder For Facebook's Long-Term Future http://t.co/7Dr4xz3ORB $TWTR $FB");
		
		SimpleDateFormat urlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Calendar cal = Calendar.getInstance();		
		cal.set(2014, 0, 1);		
		Date fromDate = cal.getTime();
		
		cal.set(2016, 2, 31);
		cal.add(Calendar.HOUR_OF_DAY, 24);
		Date toDate = cal.getTime();
		
		String[] companyList = new String[] {"AAPL","ABB","ABBV","AEP","AGFS","AMGN","AMZN","BA","BABA","BAC","BBL","BCH","BHP","BP","BRK-A","BSAC","BUD","C","CAT","CELG","CHL","CHTR","CMCSA","CODI","CSCO","CVX","D","DHR","DIS","DUK","EXC","FB","GD","GE","GOOG","HD",
				"HON","HRG","HSBC","IEP","INTC","JNJ","JPM","KO","LMT","MA","MCD","MDT","MMM","MO","MRK","MSFT","NEE","NGG","NVS","ORCL","PCG","PCLN","PEP","PFE","PG","PICO","PM","PPL","PTR","RDS-B","REX","SLB","SNP","SNY","SO","SPLP",
				"SRE","T","TM","TOT","TSM","UL","UN","UNH","UPS","UTX","V","VZ","WFC","WMT","XOM"};
		
		OutputStreamWriter csvOutput = null;
		try {
			//csvOutput = new FileWriter("D:\\Downloads\\tweets_final.csv", false);
			csvOutput = new OutputStreamWriter(new FileOutputStream("D:\\\\Downloads\\\\tweets_final.csv"), StandardCharsets.UTF_8);
			csvOutput.write("tweetId,created_at,lang,isRetweet,parent_tweet_created_at,tweet_userId,followers_count,company_name," +
				"afinn_verdict,afinn_score,afinn_comparative,no_of_positive_words,no_of_negative_words,positive_words,negative_words,cleaned_text,raw_text\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Calendar start = Calendar.getInstance();
		start.setTime(fromDate);
		Calendar end = Calendar.getInstance();
		end.setTime(toDate);

		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			
			// Do your job here with `date`.
		    //System.out.print(date);
		    
			for(int i=0; i<companyList.length; i++) { // companyList.length
				String finalPath = BASE_PATH + "\\" + companyList[i] + "\\" + urlDateFormat.format(date);
				
				
				File tweetFile = new File(finalPath); 
		        if (tweetFile.exists()) {
					try {
						BufferedReader in = new BufferedReader(new FileReader(finalPath));
						String strLine = null;
						while ((strLine = in.readLine()) != null)   { // Each line is a tweet
							JSONParser parser = new JSONParser();
							JSONObject obj = (JSONObject)parser.parse(strLine);
							
							Long tweetId = ((Long)obj.get("id"));
							//System.out.println(tweetId);
							
							String tweetText = ((String)obj.get("text"));
							//tweetText = tweetText.replaceAll("\"", "'");
							//System.out.println(tweetText);
							
							String created_at = ((String)obj.get("created_at"));
							//System.out.println(created_at);
							
							String lang = ((String)obj.get("lang"));
							//System.out.println(lang);
							
							Boolean isRetweet = false;
							String parent_tweet_created_at = created_at;
							if (obj.get("retweeted_status")!=null) {
								isRetweet = true;
								parent_tweet_created_at = (String)((JSONObject)obj.get("retweeted_status")).get("created_at");							
							} 
							//System.out.println(isRetweet);
							//System.out.println("parent_tweet_created_at="+parent_tweet_created_at);
							
							JSONObject user = (JSONObject)obj.get("user");
							
							Long tweet_userId = ((Long)user.get("id"));
							//System.out.println(tweet_userId);
							
							Long followers_count = ((Long)user.get("followers_count"));
							//System.out.println(followers_count);
							if (lang.equals("en")) {
								Map<String, String> jsonMap =  pocessTweet.processTweetString(tweetText);
								writeToCsv(csvOutput, companyList[i], tweetId, tweetText, created_at, lang, isRetweet, parent_tweet_created_at, tweet_userId, followers_count, jsonMap);
							}
							
						}
						in.close();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        } else {
		        	System.out.println("File not found:" + finalPath);
		        }
				//System.out.println(finalPath);
			}
		}
		try {
			csvOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void  writeToCsv(OutputStreamWriter csvOutput, String company_name, Long tweetId, String tweetText, String created_at, String lang, boolean isRetweet, String parent_tweet_created_at, Long tweet_userId, Long followers_count, Map<String, String> jsonMap) {
		try {
			csvOutput.write(tweetId + "," + created_at + "," + lang + "," + isRetweet + "," + parent_tweet_created_at + "," + tweet_userId + "," + followers_count + "," + company_name );
			// afinn_verdict, afinn_score, afinn_comparative, no_of_positive_words, no_of_negative_words, positive_words, negative_words, cleaned_text, raw+text 
			csvOutput.write("," + (String)jsonMap.get("verdict")); // afinn_verdict
			csvOutput.write("," + (String)jsonMap.get("score")); // afinn_score
			csvOutput.write("," + (String)jsonMap.get("comparative")); // afinn_comparative
			csvOutput.write("," + (String)jsonMap.get("no_of_positive_words")); // no_of_positive_words
			csvOutput.write("," + (String)jsonMap.get("no_of_negative_words")); // no_of_negative_words
			csvOutput.write(",\"" + (String)jsonMap.get("positive") + "\""); // 
			csvOutput.write(",\"" + (String)jsonMap.get("negative") + "\""); // 
			csvOutput.write(",\"" + (String)jsonMap.get("cleaned_phrase") + "\""); // cleaned_phrase
			csvOutput.write(",\"" + tweetText.replaceAll("\n", "").replaceAll("\\n", "").replaceAll("\r", "").replaceAll("\"", "") + "\""); // cleaned_phrase
			csvOutput.write("\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
