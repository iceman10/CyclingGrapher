import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;


import org.json.JSONObject;



public class CyclingGrapherMain {

    static final double RADIUS_OF_EARTH = 6371; // Earth's radius in km

    public static double sum(ArrayList<Double> list) {
        double sum = 0;
        for (double num : list) {
            sum = sum + Math.round(num * 1000.0) / 1000.0; 
            // rounds to 1000th place. Added due to NaN return when more than 1500 points were calculated                                               
        }
        return sum;
    }

    public static double average(ArrayList<Double> list) {
        return sum(list) / list.size();

    }

    public static ArrayList<Double> strToDouble(ArrayList<String> strArr) {
        ArrayList<Double> strArrDouble = new ArrayList<Double>();
        for (String myInt : strArr)
        {
            if (myInt.equals("NA")) {
                myInt = "0"; // filler/dummy value for NAs. TODO Change this to
                             // an interpolation
            }
            strArrDouble.add(Double.valueOf(myInt));
        }

        return strArrDouble;
    }

    // returns distance between 2 coordinates in km
    public static double deltaDist(double lat1, double long1, double lat2, double long2) {
        // Convert lat and long from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double long1Rad = Math.toRadians(long1);
        double lat2Rad = Math.toRadians(lat2);
        double long2Rad = Math.toRadians(long2);

        double deltaDist = Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad)
                * Math.cos(lat2Rad) * Math.cos(long2Rad - long1Rad))
                * RADIUS_OF_EARTH;
        return deltaDist;
    }

    // takes latitude and longitude arraylists and returns an arraylist of the
    // distance in kilometers between coordinates
    public static ArrayList<Double> deltaDistArr(ArrayList<Double> lat, ArrayList<Double> lon) {

        ArrayList<Double> arrDeltaDist = new ArrayList<Double>();

        for (int i = 0; i <= lat.size() - 2; i++) {
            arrDeltaDist.add(deltaDist(lat.get(i), lon.get(i), lat.get(i + 1), lon.get(i + 1)));
        }
        return arrDeltaDist;
    }

    // returns arraylist in seconds between time points
    public static ArrayList<Double> deltaTimeArr(ArrayList<String> time) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        ArrayList<Double> arrDeltaTime = new ArrayList<Double>();

        for (int i = 0; i <= time.size() - 2; i++) {
            try {
                Date date1 = format.parse(time.get(i));
                Date date2 = format.parse(time.get(i + 1));
                double deltaTimeSeconds = (double) (date2.getTime() - date1.getTime()) / 1000;
                arrDeltaTime.add(deltaTimeSeconds);
            } catch (ParseException e) {
                System.out.println("Time Parseing Error!!");
                e.printStackTrace();
            }
        }
        return arrDeltaTime;
    }
    
    //takes an double arraylist and returns a moving average Arraylist
    //factor is the number of points in the moving average
    public static ArrayList<Double> movAvgArr(ArrayList<Double> list, int factor){
        
        ArrayList<Double> arrMovAvg = new ArrayList<Double>();
        int i=0;
        int iterations = 0;
        double sum=0.0;
        int outerEnd = list.size()-factor;
        
        for (int j =0; j<=outerEnd;){
            int innerEnd = i+factor;
            while (true){
                sum = sum+list.get(i);
                i++;
                if (i==innerEnd){
                    break;
                }
            }
            arrMovAvg.add(sum/factor);
            sum = 0.0;
            j++;
            i=j;
            iterations++;
        }
        System.out.println("Moving average iterations: "+iterations);
        return arrMovAvg;
        
    }
    
    //URL Request Method
    
    public static String excutePost(String targetURL, String urlParameters)
    {
      URL url;
      HttpURLConnection connection = null;  
      try {
        //Create connection
        url = new URL(targetURL);
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", 
             "application/x-www-form-urlencoded");
              
        connection.setRequestProperty("Content-Length", "" + 
                 Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");  
              
        connection.setUseCaches (false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
        wr.writeBytes (urlParameters);
        wr.flush ();
        wr.close ();

        //Get Response    
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer(); 
        while((line = rd.readLine()) != null) {
          response.append(line);
          response.append('\r');
        }
        rd.close();
        return response.toString();

      } catch (Exception e) {

        e.printStackTrace();
        return null;

      } finally {

        if(connection != null) {
          connection.disconnect(); 
        }
      }
    }

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        String filename = "6.6.14ModifiedFullPoints.csv";
        
        BufferedReader CSVFile = new BufferedReader(new FileReader(filename));

        String dataRow = CSVFile.readLine(); // Read first line.
        // The while checks to see if the data is null. If
        // it is, we've hit the end of the file. If not,
        // process the data.

        int i = 0;
        ArrayList<String> arrLat = new ArrayList<String>();
        ArrayList<String> arrLong = new ArrayList<String>();
        ArrayList<String> arrAlt = new ArrayList<String>();
        ArrayList<String> arrBear = new ArrayList<String>();
        ArrayList<String> arrAcc = new ArrayList<String>();
        ArrayList<String> arrSpd = new ArrayList<String>();
        ArrayList<String> arrTime = new ArrayList<String>();

        while (dataRow != null) {
            if (i == 7) {
                i = 0;
            }
            String[] dataArray = dataRow.split(",");
            for (String item : dataArray) {
                if (item.equals("")) {
                    item = "NA";
                }
                // System.out.print(item + "\t");

                if (i == 0) {
                    arrLat.add(item);
                } else if (i == 1) {
                    arrLong.add(item);
                } else if (i == 2) {
                    arrAlt.add(item);
                } else if (i == 3) {
                    arrBear.add(item);
                } else if (i == 4) {
                    arrAcc.add(item);
                } else if (i == 5) {
                    arrSpd.add(item);
                } else if (i == 6) {
                    arrTime.add(item);
                }
                i++;
            }
            //System.out.println(); // Print the data line.
            dataRow = CSVFile.readLine(); // Read next line of data.
        }
        
        // Close the file once all data has been read.
        CSVFile.close();
    
        // End the printout with a blank line.
        //System.out.println();
        
        // Convert string array lists to doubles for lat, long, alt, bear,
        // accuracy and speed
       
        
        ArrayList<Double> arrLatDouble = strToDouble(arrLat);
        ArrayList<Double> arrLongDouble = strToDouble(arrLong);
        ArrayList<Double> arrAltDouble = strToDouble(arrAlt);
        ArrayList<Double> arrBearDouble = strToDouble(arrBear);
        ArrayList<Double> arrAccDouble = strToDouble(arrAcc);
        ArrayList<Double> arrSpdDouble = strToDouble(arrSpd);

        ArrayList<Double> arrDeltaDist = deltaDistArr(arrLatDouble, arrLongDouble);
        ArrayList<Double> arrDeltaTime = deltaTimeArr(arrTime);

        System.out.println("Delta Distance Array Size: " + arrDeltaDist.size());
        System.out.println("latitude Array Size: " + arrLatDouble.size());
        System.out.println("Total Distance = " + sum(arrDeltaDist));
        System.out.println("Average Altitude: " + average(arrAltDouble));
        System.out.println("Average Bearing: " + average(arrBearDouble));
        System.out.println("Average Accuracy: " + average(arrAccDouble));
        System.out.println("Average speed = " + average(arrSpdDouble) + " m/s");
        System.out.println("Total Time = " + sum(arrDeltaTime) + "s");
        System.out.println("Average Speed Calculated = " + sum(arrDeltaDist) * 1000
                / sum(arrDeltaTime));
        
        ArrayList<Double> movAvg_5_Spd = movAvgArr(arrSpdDouble,5);
        System.out.println("5 Point moving average of Speed Array Size: "+ movAvg_5_Spd.size());
        System.out.println("Diff = "+(arrSpdDouble.size()-movAvg_5_Spd.size()));
        System.out.println("5 Point Avg. = "+average(movAvg_5_Spd));
        
        FileWriter saveFile = new FileWriter("TestSave.txt");
        
        for (int k=0; k <= movAvg_5_Spd.size()-1; k++){
            saveFile.write(String.valueOf(k)+"," +String.valueOf(movAvg_5_Spd.get(k)));
            saveFile.write("\n");
        }
        
        saveFile.flush();
        saveFile.close();
       
        // De-comment below to examine elements of an arraylist
        /*
         * for (double item:arrDeltaDist){ System.out.println(item); }
         */
        
        //URL Request 
        
        String API_KEY = "AIzaSyD7EDp31VN01zkyMOSLrvkb5ehfobXOTxI"; 
        
        String urlParameters = "fName=" + URLEncoder.encode("???", "UTF-8") +"&lName=" + URLEncoder.encode("???", "UTF-8");
        
        String url = "https://maps.googleapis.com/maps/api/elevation/json?path=29.554798,-98.657923|29.55473,-98.65764&samples=10&key="+API_KEY;
                
        //System.out.println(excutePost(url , urlParameters));
        
        //Parse JSON output using org.json 
        // http://theoryapp.com/parse-json-in-java/
        String str = excutePost(url , urlParameters);
        JSONObject obj = new JSONObject(str);
        JSONObject res = obj.getJSONArray("results").getJSONObject(0);
        System.out.println(res.getDouble("elevation"));
        
        System.out.println("Test Change");

    }

}
