import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

public class Main
{
    public static void main(String[] args)
    {
        try {
            Connection connection = createConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("Select * from address");
            while (!resultSet.isLast())
            {
                resultSet.next();
                String temp = resultSet.getString("FULL_ADDRESS");
                System.out.println(temp);
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception in main " + e.getMessage());
        }
    }
    
    public JSONObject getAddress() throws Exception
    {
        try
        {
            URL  url = new URL("https://www.google.co.in/maps/@");
            JSONObject responseJSON = new JSONObject();
            return responseJSON;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    private static Connection createConnection() throws IOException, ClassNotFoundException, SQLException {
        Properties prop = new Properties();
        String host = "jdbc:postgresql://localhost:5432/pittsburgh";
        String username = "postgres";
        String password = "admin";
        Connection connection = DriverManager.getConnection(host, username, password);
        System.out.println("CONNECTION: " + connection);
        return connection;
    }

    private static JSONObject getSchoolForAddress(String address)
    {
        JSONObject addressJSON = new JSONObject();
        return addressJSON;
    }
}
