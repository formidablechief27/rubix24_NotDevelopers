package com.example.disaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

@Controller
public class NewsController {
	
	@GetMapping("/fetch-news")
	public String news(Model model) {
		String url = "https://edition.cnn.com/interactive/2013/05/us/moore-oklahoma-tornado/";
		String news = "";
		try {
	        Document document = Jsoup.connect(url).get();
	        Elements tdElements = document.select("p");
	        for (Element td : tdElements) news += td.text() + "\n";
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		System.out.println();
		url = "https://www.eskp.de/en/natural-hazards/devastating-destruction-caused-by-tornado-in-moore-935196/";
		try {
	        Document document = Jsoup.connect(url).get();
	        Elements tdElements = document.select("p");
	        for (Element td : tdElements) news += td.text() + "\n";
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
		Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Create an Annotation object with the input text
        Annotation document = new Annotation(news);

        // Run the pipeline on the Annotation object
        pipeline.annotate(document);

        // Get sentences from the Annotation object
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        // Calculate importance scores (example: using sentence length)
        double[] importanceScores = new double[sentences.size()];
        for (int i = 0; i < sentences.size(); i++) {
            importanceScores[i] = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class).size();
        }
        
        System.out.println(news);

        // Rank sentences based on importance
        int[] rankedSentences = rankSentences(importanceScores);

        // Select top sentences for summary
        int summaryLength = Math.max(3, rankedSentences.length/5); // Adjust summary length as needed
        System.out.println("\n");
        System.out.println(summaryLength + " : " + rankedSentences.length);
        StringBuilder summary = new StringBuilder();
        String lines[] = new String[summaryLength];
        for (int i = 0; i < summaryLength; i++) {
        	String s = sentences.get(rankedSentences[i]).toString();
        	lines[i] = s;
        	System.out.println(sentences.get(rankedSentences[i]).toString());
            summary.append(sentences.get(rankedSentences[i]).toString()).append(" ");
        }
        String name = "";
        String date = "";
        String temp = "";
        String prec = "";
        String hum = "";
        String spd = "";
       
        String URL = "https://api.weatherbit.io/v2.0/current?lat=35.46&lon=-97.51&key=d06b86a6f1b64ba38830ff9b384bbf95";
        HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) new URL(URL).openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) response.append(inputLine);

                in.close();

                // Process the JSON response and add markers for each hospital
                String jsonResponse = response.toString();
                System.out.println(jsonResponse);
                int ind1 = jsonResponse.indexOf("city_name");
                int ind2 = jsonResponse.indexOf(",", ind1);
                name = jsonResponse.substring(ind1 + 12, ind2-1);
                int ind3 = jsonResponse.lastIndexOf("temp");
                int ind4 = jsonResponse.indexOf(",", ind3);
                temp = jsonResponse.substring(ind3 + 6, ind4);
                int ind5 = jsonResponse.indexOf("precip");
                int ind6 = jsonResponse.indexOf(",", ind5);
                prec = jsonResponse.substring(ind5 + 8, ind6);
                int ind7 = jsonResponse.indexOf("rh");
                int ind8 = jsonResponse.indexOf(",", ind7);
                hum = jsonResponse.substring(ind7 + 4, ind8);
                int ind9 = jsonResponse.indexOf("spd");
                spd = jsonResponse.substring(ind9 + 5, ind9 + 9);
                
            } else {
                System.out.println("Error: " + responseCode);
            }

            connection.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		prec += "%";
		hum += "%";
		spd += " m/s";
		model.addAttribute("spd", spd);
		model.addAttribute("hum", hum);
		model.addAttribute("prec", prec);
        model.addAttribute("name", name);
        model.addAttribute("temp", temp);
        model.addAttribute("list", lines);
		return "social.html";
	}
	
	public int[] rankSentences(double[] scores) {
		// Implement your ranking logic (e.g., sorting indices based on scores)
        // This example uses a simple approach; you may want to explore more sophisticated methods
        int[] indices = new int[scores.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        // Sort indices based on scores (descending order)
        for (int i = 0; i < scores.length - 1; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                if (scores[indices[j]] > scores[indices[i]]) {
                    int temp = indices[i];
                    indices[i] = indices[j];
                    indices[j] = temp;
                }
            }
        }

        return indices;
    }
}
