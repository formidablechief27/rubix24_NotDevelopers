package com.example.disaster;

import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Controller
public class PredictionController {
	
	@GetMapping("/start")
	public String shuru(Model model) {
		return "index.html";
	}
	
	PriorityQueue<double[]> queue() {PriorityQueue<double[]> queue = new PriorityQueue<double[]>((a, b) -> {if (a[0] < b[0]) return 1; else if (a[0] == b[0] && a[1] > b[1]) return 1; else return -1;});return queue;}
	
	@GetMapping("/launch")
	public String start(Model model) {
		String csvFilePath = "C:/Users/Dhrumil/OneDrive/Desktop/us.csv";
        String url = "https://www.faa.gov/air_traffic/publications/atpubs/cnt_html/appendix_a.html";
        HashMap<String, String> abb = new HashMap<>();
        String disaster[] = new String[6];
        State st[] = new State[59];
        try {
            Document document = Jsoup.connect(url).get();
            Elements tdElements = document.select("td");
            String s[] = {"", ""};
            for (Element td : tdElements) {
            	if(s[0].equals("")) {
            		s[0] = td.text();
            	}
            	else if (s[1].equals("")) {
            		s[1] = td.text();
            		abb.put(s[1], s[0]);
            		s[0] = "";
            		s[1] = "";
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] nextLine = null;
            HashSet<String> states = new HashSet<>();
            HashMap<String, HashMap<String, HashSet<String>>> map = new HashMap<>();
            HashMap<String, HashMap<String, ArrayList<String>>> dates = new HashMap<>();
            HashMap<String, HashMap<String, ArrayList<Integer>>> list = new HashMap<>();
            TreeMap<Integer, String> sorted = new TreeMap<>(Collections.reverseOrder());
            while ((nextLine = reader.readNext()) != null) {
            	if(!nextLine[2].trim().equals("state")) {
            		String state = nextLine[2];
            		states.add(state);
            		String activity = nextLine[7].trim();
            		String date = nextLine[12].substring(0, nextLine[12].indexOf('T'));
            		String put = "";
            		if(activity.contains("Tornado")) {
            			put = "Tornado";
            			fill(map, put, date, state, dates, list);
            		}
            		if(activity.contains("Floods")) {
            			put = "Flood";
            			fill(map, put, date, state, dates, list);
            		}
            		if(activity.contains("Hurricane")) {
            			put = "Hurricane";
            			fill(map, put, date, state, dates, list);
            		}
            		if(activity.contains("Storm")) {
            			put = "Storm";
            			fill(map, put, date, state, dates, list);
            		}
            		if(activity.contains("Drought")) {
            			put = "Drought";
            			fill(map, put, date, state, dates, list);
            		}
            		if(activity.contains("Earthquake")){
            			put = "Earthquake";
            			fill(map, put, date, state, dates, list);
            		}
            	}
            }
            int i = 0;
            for(String stat : states) st[i++] = new State(stat);
            HashMap<String, Integer> indx = new HashMap<>();
            for(i=0;i<59;i++) indx.put(st[i].name, i); 
            int index = 0;
            for(Entry<String, HashMap<String, HashSet<String>>> entry : map.entrySet()){
            	sorted = new TreeMap<>(Collections.reverseOrder());
            	System.out.println(entry.getKey());
            	HashMap<String, HashSet<String>> bmap = entry.getValue();
            	for(Entry<String, HashSet<String>> en : bmap.entrySet()) sorted.put(en.getValue().size(), en.getKey());
            	for(Entry<Integer, String> entry2 : sorted.entrySet()) {
            		String current_state = entry2.getValue();
            		int ind = indx.get(current_state);
            		System.out.print(entry2.getKey() + " : " + abb.get(entry2.getValue()));
            		String find = entry2.getValue();
            		ArrayList<String> arr = dates.get(entry.getKey()).get(find);
            		System.out.println(" => " + arr.get(1) + " : " + arr.get(2) + " : " + arr.get(0));
            		st[ind].last[index] = arr.get(0);
            		ArrayList<Integer> p = list.get(entry.getKey()).get(find);
            		Collections.sort(p);
            		st[ind].intervals[index] = p;
            		System.out.println(p);
            		System.out.println("");
            		disaster[index] = entry.getKey();
            	}
            	index++;
            	System.out.println("\n");
            }

        } catch (IOException e) {
        } catch (CsvValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        double chance[][] = new double[59][7];
        String probs[][] = new String[56][7];
        int ind = 0;
        PriorityQueue<double[]> queue = queue();
        for(int i=0;i<59;i++) {
        	State state = st[i];
        	double prob = 0.0;
        	String name = abb.get(st[i].name);
        	chance[i][0] = ind;
        	double max = 0.0;
        	for(int j=0;j<6;j++) {
        		chance[i][j+1] = 0.0D;
        		double occurences = st[i].intervals[j].size() + 1;
        		String last = st[i].last[j];
        		if(last != null) {
        			LocalDate today = LocalDate.now();
        	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        	        String date = today.format(formatter);
        	        LocalDate date1 = LocalDate.parse(last, formatter);
        	        LocalDate date2 = LocalDate.parse(date, formatter);
        	        int daysDifference = (int)(date2.toEpochDay() - date1.toEpochDay());
        	        ArrayList<Integer> list = new ArrayList<>();
        	        for(int ele : st[i].intervals[j]) {if(ele > 100) break; list.add(Math.abs(daysDifference - ele));}
        	        Collections.sort(list);
        	        chance[i][j+1] = occurences/200.0;
    	        	prob = 0.0;
    	        	int num = 0;
    	        	for(int val : list) {prob += 100 - val/100.0; num++; if(num == 10) break;}
    	        	prob /= 1000.0;
    	        	chance[i][j+1] += prob;
    	        	chance[i][j+1] /= 2.0;
        		}
        		DecimalFormat decimal = new DecimalFormat("#.##");
        		chance[i][j+1]*=100;
        		String print = decimal.format(chance[i][j+1]);
        		if(chance[i][j+1] > max) max = chance[i][j+1];
        	}
        	queue.add(new double[] {max, i});
        	ind++;
        }
        int index = 0;
        while(!queue.isEmpty()) {
        	double curr[] = queue.poll();
        	ind = (int)curr[1];
        	DecimalFormat decimal = new DecimalFormat("#.##");
        	if(abb.containsKey(st[(int)chance[ind][0]].name)) {
        		probs[index][0] = abb.get(st[(int)chance[ind][0]].name);
            	for(int j=0;j<6;j++) probs[index][j+1] = decimal.format(chance[ind][j+1]);
            	for(int j=0;j<6;j++) System.out.print(probs[index][j+1] + " ");
            	index++;
            	System.out.println();
        	}
        }
        
        model.addAttribute("probs", probs);	
		return "weather.html";
	}
	
	public void fill(HashMap<String, HashMap<String, HashSet<String>>> map, String activity, String date, String state, Map<String, HashMap<String, ArrayList<String>>> dates, HashMap<String, HashMap<String, ArrayList<Integer>>> list) {
		if(map.containsKey(activity)) {
			HashMap<String, HashSet<String>> bmap = map.get(activity);
			HashMap<String, ArrayList<String>> datemap = dates.get(activity);
			HashMap<String, ArrayList<Integer>> lists = list.get(activity);
			if(bmap.containsKey(state)) {
				bmap.get(state).add(date);
				String prev = datemap.get(state).get(0);
				LocalDate date1 = LocalDate.parse(prev, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		        LocalDate date2 = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		        long days = date2.toEpochDay() - date1.toEpochDay();
		        if(days > 0) {
		        	long min = Long.parseLong(datemap.get(state).get(1));
			        long max = Long.parseLong(datemap.get(state).get(2));
			        if(days < min) min = days;
			        if(days > max) max = days;
			        datemap.get(state).set(0, date);
			        datemap.get(state).set(1, min + "");
			        datemap.get(state).set(2, max + "");
			        lists.get(state).add((int)(days));
		        }
			}
			else {
				HashSet<String> d = new HashSet<>();
				d.add(date);
				bmap.put(state, d);
				ArrayList<String> arr = new ArrayList<>();
				arr.add(date);
				arr.add("1000000000");
				arr.add("0");
				datemap.put(state, arr);
				lists.put(state, new ArrayList<Integer>());
			}
			map.put(activity, bmap);
			dates.put(activity, datemap);
			list.put(activity, lists);
		}
		else {
			HashMap<String, HashSet<String>> bmap = new HashMap<>();
			HashMap<String, ArrayList<String>> datemap = new HashMap<>();
			HashMap<String, ArrayList<Integer>> lists = new HashMap<>();
			HashSet<String> d = new HashSet<>();
			d.add(date);
			bmap.put(state, d);
			ArrayList<String> arr = new ArrayList<>();
			arr.add(date);
			arr.add("1000000000");
			arr.add("0");
			datemap.put(state, arr);
			lists.put(state, new ArrayList<Integer>());
			map.put(activity, bmap);
			dates.put(activity, datemap);
			list.put(activity, lists);
		}
	}
	
	class State {
		String name;
		ArrayList<Integer> intervals[];
		String last[];
		
		State(String name){
			this.name = name;
			intervals = new ArrayList[6];
			for(int i=0;i<6;i++) intervals[i] = new ArrayList<>();
			last = new String[6];
		}
	}
	
}
