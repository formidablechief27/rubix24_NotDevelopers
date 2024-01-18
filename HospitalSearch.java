import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.Random;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class HospitalSearch {
	
	String list[][] = new String[10000][4];
	PriorityQueue<double[]> queue() {PriorityQueue<double[]> queue = new PriorityQueue<double[]>((a, b) -> {if (a[0] > b[0]) return 1; else if (a[0] == b[0] && a[1] > b[1]) return 1; else return -1;});return queue;}
	String flist[][] = new String[50][6];

    public static void main(String[] args) {
        double latitude = 35.46;
        double longitude = -97.51;
        HospitalSearch S = new HospitalSearch();
        S.searchHospitals(latitude, longitude);
    }
    

    public void searchHospitals(double latitude, double longitude) {
    	longitude = -97.51;
		latitude = 35.46;
    	PriorityQueue<double[]> queue = queue();
        try {
            String query = "shelter";
            int radius = 1; // You can adjust the radius as needed
            String bbox = (longitude - radius) + "," + (latitude - radius) + "," + (longitude + radius) + "," + (latitude + radius);
            String url = "https://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(query, "UTF-8") +
                    "&format=json&limit=10000&bounded=1&viewbox=" + URLEncoder.encode(bbox, "UTF-8");

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
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
                int i = 0;
                while(true) {
                	int first = jsonResponse.indexOf("{");
                	int next = jsonResponse.indexOf("}");
                	if(first == -1 || next == -1) break;
                	//System.out.println(jsonResponse.substring(first, next + 1));
                	String line = jsonResponse.substring(first, next + 1);
                	int ind = line.indexOf("display_name");
                	int st = line.indexOf("boundingbox");
                	int lat = line.indexOf("lat");
                	int latend = line.indexOf("lon");
                	int lon = line.indexOf("lon");
                	int lonend = line.indexOf("class");
                	double l = Double.parseDouble(line.substring(lat + 6, latend - 3));
                	double ll = Double.parseDouble(line.substring(lon + 6, lonend - 3));
                	double dis = cal(latitude, longitude, l, ll);
                	DecimalFormat decimal = new DecimalFormat("#.####");
                	DecimalFormat deci = new DecimalFormat("#.##");
                	list[i][0] = line.substring(ind + 15, st - 3);
                	list[i][1] = decimal.format(l);
                	list[i][2] = decimal.format(ll);
                	list[i][3] = deci.format(dis);
                	queue.add(new double[] {dis, i++});
                	//System.out.println(line.substring(ind + 15, st - 3) + " => " + line.substring(lat + 6, latend - 3) + ", " + line.substring(lon + 6, lonend - 3) + "=> distance : " + dis);
                	jsonResponse = jsonResponse.substring(next + 1, jsonResponse.length());
                }
                i = 0;
                int p = queue.size();
                while(!queue.isEmpty()) {
                	double curr[] = queue.poll();
                	int ind = (int)curr[1];
                	int val = (50 - i)/2;
                	Random r = new Random();
                	double value = (double)val + r.nextDouble();
                	DecimalFormat decimal = new DecimalFormat("#.##");
                	flist[i][0] = decimal.format(value) + "%";
                	flist[i][1] = list[ind][0];
                	int index = flist[i][1].indexOf(",");
                	flist[i][2] = flist[i][1].substring(index + 1, flist[i][1].length());
                	flist[i][1] = flist[i][1].substring(0, index);
                	flist[i][4] = list[ind][1];
                	flist[i][5] = list[ind][2];
                	flist[i][3] = list[ind][3];
                	i++;
                	if(i == p) break;
                }
                for(i=0;i<p;i++) {
                	for(int j=0;j<6;j++) {
                		System.out.print(flist[i][j] + " ");
                	}
                	System.out.println();
                }
                // Implement your logic to parse the JSON and add markers to the map
            } else {
                System.out.println("Error: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public double cal(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        double earthRadius = 6371;

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in kilometers
        double distance = earthRadius * c;

        return distance;
    }
}
