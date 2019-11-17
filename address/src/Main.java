import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            String data = "";
            data = new String(Files.readAllBytes(Paths.get("E:\\Pittsburgh School\\school.json")));
            JSONObject schoolsList = new JSONObject(data);
            System.out.println(data);
            Connection connection = createConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("Select * from address where elementary_school is null");
            while (!resultSet.isLast())
            {
                resultSet.next();
                Long serialNumber = resultSet.getLong("SERIAL_NUMBER");
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
                streetName = streetName.toLowerCase();
                if (streetName.contains("blvd"))
                {
                    System.out.println(streetName);
                    streetName = streetName.replace("blvd", "Boulevard");
                }
                String streetAddress = streetName.toLowerCase();
                if (!streetType.equalsIgnoreCase(""))
                {
                    streetAddress = streetAddress + " " + streetType;
                }
                JSONObject addressJSON = getSchoolForAddress(house, streetAddress, city, zip);
                String searchParams = getSearchParams(addressJSON);
                if (!searchParams.equalsIgnoreCase(""))
                {
                    updateSchoolDetails(schoolsList, searchParams, serialNumber);
                }
                connection.close();
            }
        } catch (Exception e)
        {
            System.out.println("Exception in main " + e.getMessage());
        }
    }

    private static void updateSchoolDetails(JSONObject requestJSON, String searchParams, Long serialNumber)
            throws SQLException, IOException, ClassNotFoundException
    {
        System.out.println("inside updateSchoolDetails");
        String[] schoolIdArray = searchParams.split(":");
        JSONObject schoolJSON = requestJSON.optJSONObject("schools");
        String primarySchool = "";
        String middleSchool = "";
        String highSchool = "";
        for (String schoolId : schoolIdArray)
        {
            Set<String> schoolIds = schoolJSON.keySet();
            for (String schoolType : schoolIds)
            {
                JSONArray schoolList = schoolJSON.getJSONArray(schoolType);
                for (int i = 0; i < schoolList.length(); i++)
                {
                    JSONObject schoolTempJSON = schoolList.getJSONObject(i);
                    System.out.println("schooltempjson " + schoolTempJSON.toString());
                    if (schoolId.equalsIgnoreCase(String.valueOf(schoolTempJSON.getInt("SchoolID"))))
                    {
                        String schoolName = schoolTempJSON.getString("SchoolName");
                        switch (schoolType)
                        {
                            case "K-5 Schools":
                            case "K-8 Schools":
                                primarySchool = schoolName;
                                break;
                            case "6-8 Schools":
                                middleSchool = schoolName;
                                break;
                            case "9-12 Schools":
                            case "6-12 Schools":
                                highSchool = schoolName;
                                break;
                        }
                    }
                }
            }
        }

        if (middleSchool.equalsIgnoreCase(""))
        {
            middleSchool = primarySchool;
        }
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        String query =
                "Update address set ELEMENTARY_SCHOOL = '" + primarySchool + "' where SERIAL_NUMBER = " + serialNumber;
        System.out.println("query: " + query);
        statement.executeUpdate(
                "Update address set ELEMENTARY_SCHOOL = '" + primarySchool + "' where SERIAL_NUMBER = " + serialNumber);
        statement.executeUpdate(
                "Update address set MIDDLE_SCHOOL = '" + middleSchool + "' where SERIAL_NUMBER = " + serialNumber);
        statement.executeUpdate(
                "Update address set HIGH_SCHOOL = '" + highSchool + "' where SERIAL_NUMBER = " + serialNumber);
        connection.close();
    }

    private static String getSearchParams(JSONObject addressJSON)
    {
        String searchString = "";
        if (addressJSON.has("id"))
        {
            JSONObject idJSON = addressJSON.optJSONObject("id");
            JSONObject buildingsJSON = idJSON.optJSONObject("buildings");
            if (buildingsJSON != null && !buildingsJSON.isEmpty())
            {
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
            }
        }

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

    private static Connection createConnection() throws IOException, ClassNotFoundException, SQLException
    {
        String host = "jdbc:postgresql://localhost:5432/pittsburgh";
        String username = "postgres";
        String password = "admin";
        Connection connection = DriverManager.getConnection(host, username, password);
        System.out.println("CONNECTION: " + connection);
        return connection;
    }

    private static JSONObject getSchoolForAddress(String house, String street, String city, String zipcode)
            throws IOException, InterruptedException
    {
        Thread.sleep(700);
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

        int responseCode = con.getResponseCode();
        if (responseCode == 200)
        {
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
        }
        return addressJSON;
    }

    private static JSONObject getSchoolJSON(String searchParam) throws IOException, InterruptedException
    {
        Thread.sleep(300);
        JSONObject addressJSON = new JSONObject();
        URL url = new URL(searchUrl + "/" + searchParam);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

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
