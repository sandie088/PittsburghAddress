import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Main
{
    private static String searchUrl = "http://discoverpps.org/api/search";
    private static String HOUSE = "house";
    private static String STREET = "street";
    private static String CITY = "city";
    private static String ZIP = "zip";

    public static void main(String[] args)
    {
        try
        {
            Connection connection = createConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("Select * from address");
//            while (!resultSet.isLast())
            {
                resultSet.next();
                String house = resultSet.getString("HOUSE_NUMBER");
                String streetName = resultSet.getString("STREET_NAME");
                String streetType = resultSet.getString("STREET_TYPE");
                if (streetType == null)
                {
                    streetType = "";
                }
                streetType = getStreetTypeAbbreviation(streetType);
                String city = resultSet.getString("MUNICIPALITY");
                String zip = resultSet.getString("ZIP_CODE");
                JSONObject addressJSON = getSchoolForAddress(house, streetName.toLowerCase() + " " + streetType, city, zip);
                String getSearchParams = getSearchParams(addressJSON);
            }
        } catch (Exception e)
        {
            System.out.println("Exception in main " + e.getMessage());
        }
    }

    private static String getSearchParams(JSONObject addressJSON)
    {
        String searchString = "";
        JSONObject idJSON = addressJSON.optJSONObject("id");
        JSONObject buildingsJSON = idJSON.optJSONObject("buildings");
        JSONArray tempJSONArray = buildingsJSON.getJSONArray(buildingsJSON.keys().next());
        System.out.println(tempJSONArray);

        for (int i = 0; i < tempJSONArray.length(); i++)
        {
            searchString = searchString + ":" + tempJSONArray.getString(i);
        }

        if (!searchString.equalsIgnoreCase(""))
        {
            searchString = searchString.substring(1, searchString.length());
        }
        System.out.println(searchString);

        return searchString;
    }

    private static String getStreetTypeAbbreviation(String street)
    {
        switch (street)
        {
            case "ST":
                street = "Street";
                break;
            case "DR":
                street = "Drive";
                break;
            case "AVE":
                street = "Avenue";
                break;
            case "WAY":
                street = "Way";
                break;
            case "LN":
                street = "Lane";
                break;
            case "PL":
                street = "Place";
                break;
            case "TER":
                street = "Terrace";
                break;
            case "RD":
                street = "Road";
                break;
            case "BLVD":
                street = "Boulevard";
                break;
            case "SQ":
                street = "Square";
                break;
            case "CT":
                street = "Court";
                break;
            case "EXT":
                street = "Extension";
                break;
            case "GRN":
                street = "Green";
                break;
            case "CIR":
                street = "Circle";
                break;
            case "RDWY":
                street = "RoadWay";
                break;
        }
        return street;
    }

    public JSONObject getAddress() throws Exception
    {
        try
        {
            URL url = new URL(searchUrl);
            JSONObject responseJSON = new JSONObject();
            return responseJSON;
        } catch (Exception e)
        {
            throw e;
        }
    }

    private static Connection createConnection() throws IOException, ClassNotFoundException, SQLException
    {
        Properties prop = new Properties();
        String host = "jdbc:postgresql://localhost:5432/pittsburgh";
        String username = "postgres";
        String password = "admin";
        Connection connection = DriverManager.getConnection(host, username, password);
        System.out.println("CONNECTION: " + connection);
        return connection;
    }

    private static JSONObject getSchoolForAddress(String house, String street, String city, String zipcode)
            throws IOException
    {
        JSONObject addressJSON = new JSONObject();
        URL url = new URL(searchUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        JSONObject jsonInputString = new JSONObject();
        jsonInputString.put(HOUSE, house);
        jsonInputString.put(STREET, street);
        jsonInputString.put(CITY, city);
        jsonInputString.put(ZIP, zipcode);
        System.out.println(jsonInputString);
        try (OutputStream os = con.getOutputStream())
        {
            byte[] input = jsonInputString.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)))
        {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null)
            {
                response.append(responseLine.trim());
            }
            System.out.println("Printing response:" + response.toString());
            addressJSON = new JSONObject(response.toString());
        }
        return addressJSON;
    }
}
