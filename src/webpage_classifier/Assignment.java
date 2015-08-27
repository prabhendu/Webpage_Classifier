package webpage_classifier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


public class Assignment {

	private static class textDetails {
		private String text;
		private int count;
		public textDetails (String text,int count) {
			this.text=text;
			this.count=count;
		}
	}
	
	private static HashMap<String,textDetails> impStrings = new HashMap<String,textDetails>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("BrightEdge Assignment\n----------------------");
		
		if(args.length != 1) {
			System.out.println("Incorrect number of arguments.\nProvide only URL.!!");
			return;
		}
		
		StringBuffer response = new StringBuffer();
		Assignment solution = new Assignment(); 
		
		/* parsing using Jsoup library */
		try {
			
			int recursionLevel=0;
			String webPage = args[0];
			System.out.println("URL entered is : " + args[0]);
			
			Queue<String> linkQueue = new LinkedList<String>();
			linkQueue.add(webPage);
			recursionLevel += 1;
			Document doc = Jsoup.connect(webPage).userAgent("Mozilla").get();
			/*Adding External Links of the start page to queue
			  Just one level of recursion used*/
			Elements links = doc.select("a[href]");
			for (Element link : links) {
				String linkHref = link.attr("href");
				if (link.text() != null) { 
					String[] str = link.text().split(" ");
					for (int i=0 ; i<str.length-2;i++) {
						String key = str[i]+" "+str[i+1]+" "+str[i+2];
						if (impStrings.containsKey(key)) {
							textDetails t1=new textDetails(key, impStrings.get(key).count+1);
							impStrings.put(key,t1);
						} else {
							textDetails t1=new textDetails(key, 1);
							impStrings.put(key,t1);
						}
					}
				}
				if (linkHref.startsWith("http")) {
				linkQueue.add(linkHref);
				}
			}
			
			while (!linkQueue.isEmpty()) {
				String page = linkQueue.poll();
				
				response = solution.httpGet(page);
				if(response == null) {
					continue;
				}
				
				doc = Jsoup.parse(response.toString());	

				//Title extraction
				String title = doc.title();
				String[] str = title.split(" ");
				for (int i=0 ; i<str.length-2;i++) {
					String key = str[i]+" "+str[i+1]+" "+str[i+2];
					if (impStrings.containsKey(key)) {
						textDetails t1=new textDetails(key, impStrings.get(key).count+1);
						impStrings.put(key,t1);
					} else {
						textDetails t1=new textDetails(key, 1);
						impStrings.put(key,t1);
					}
				}
				
				// Images' text extraction
				Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
				for (Element image : images) {
					if (image.attr("alt") != null && recursionLevel == 1) {
						str = image.attr("alt").split(" ");
						for (int i=0 ; i<str.length-2;i++) {
							String key = str[i]+" "+str[i+1]+" "+str[i+2];
							if (impStrings.containsKey(key)) {
								textDetails t1=new textDetails(key, impStrings.get(key).count+1);
								impStrings.put(key,t1);
							} else {
								textDetails t1=new textDetails(key, 1);
								impStrings.put(key,t1);
							}
						}

				}
				}
				
				//Headers' text extraction.
				Elements headers = doc.select("h0,h1,h2,h3");
				for (Element header : headers) {
					if (header.text() != null && recursionLevel == 1) {
						str = header.text().split(" ");
						for (int i=0 ; i<str.length-2;i++) {
							String key = str[i]+" "+str[i+1]+" "+str[i+2];
							if (impStrings.containsKey(key)) {
								textDetails t1=new textDetails(key, impStrings.get(key).count+1);
								impStrings.put(key,t1);
							} else {
								textDetails t1=new textDetails(key, 1);
								impStrings.put(key,t1);
							}
						}
					}
				}
				
				//MetaData content extraction
				Elements metaData = doc.select("meta[name~=(description|title|keywords)]");
				for (Element meta : metaData) {
					if (meta.attr("content") != null) {
						str = meta.attr("content").split(" ");
						for (int i=0 ; i<str.length-2;i++) {
							String key = str[i]+" "+str[i+1]+" "+str[i+2];
							if (impStrings.containsKey(key)) {
								textDetails t1=new textDetails(key, impStrings.get(key).count+1);
								impStrings.put(key,t1);
							} else {
								textDetails t1=new textDetails(key, 1);
								impStrings.put(key,t1);
							}
						}
					}
			}
				}
			
		} catch(Exception e) {
			System.out.println("Error in Jsoup fetch : " + e.getMessage());
			e.printStackTrace();
		}
		
		solution.getClassification(impStrings);
	}

	
	private void getClassification(HashMap<String,textDetails> impStrings) {
		int totalPhraseCount=0;
		for (textDetails str:impStrings.values()) {
			totalPhraseCount += str.count;
		}
		//Considering keyword (phrase) density to be between 0.3% to 3% to refine results
		//return results;
		System.out.println("Relevant phrases \n---------------------------------------");
		for (textDetails str:impStrings.values()) {
			double phraseDensity = (str.count * 300) / totalPhraseCount;
			if (phraseDensity <= 3 && phraseDensity >= 0.3) {	
				System.out.println(str.text + "\n");
			}
		}
		
	}
	
	private StringBuffer httpGet(String urlString){
		// TODO Get http data of the web-page
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			BufferedReader bufRead = new BufferedReader(
												new InputStreamReader(
												connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = bufRead.readLine()) != null) {
				response.append(inputLine);
			}
			return response;
		} catch (Exception e) {
			System.out.println("Error while fetching : " + urlString);
			System.out.println(e.getMessage());
			return null;
		}
	}
		
}