import org.json.JSONObject;

import java.net.URL;

public class Main
{
    public static void main(String[] args)
    {
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
}
