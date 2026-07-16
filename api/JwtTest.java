import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class JwtTest {
    public static void main(String[] args) throws Exception {
        // Login
        URL loginUrl = new URL("http://localhost:8080/api/sys/user/login");
        HttpURLConnection loginConn = (HttpURLConnection) loginUrl.openConnection();
        loginConn.setRequestMethod("POST");
        loginConn.setRequestProperty("Content-Type", "application/json");
        loginConn.setDoOutput(true);
        
        String loginBody = "{\"username\":\"admin\",\"password\":\"123456\"}";
        OutputStream os = loginConn.getOutputStream();
        os.write(loginBody.getBytes());
        os.flush();
        
        BufferedReader loginReader = new BufferedReader(new InputStreamReader(loginConn.getInputStream()));
        String loginResponse = "";
        String line;
        while ((line = loginReader.readLine()) != null) {
            loginResponse += line;
        }
        loginReader.close();
        System.out.println("Login response: " + loginResponse);
        
        // Extract token
        int tokenStart = loginResponse.indexOf("\"token\":\"") + 8;
        int tokenEnd = loginResponse.indexOf("\"", tokenStart);
        String token = loginResponse.substring(tokenStart, tokenEnd);
        System.out.println("Token: " + token);
        
        // Test with token
        URL dashboardUrl = new URL("http://localhost:8080/api/dashboard/stats");
        HttpURLConnection dashConn = (HttpURLConnection) dashboardUrl.openConnection();
        dashConn.setRequestMethod("GET");
        dashConn.setRequestProperty("token", token);
        
        System.out.println("Dashboard response code: " + dashConn.getResponseCode());
        
        BufferedReader dashReader = new BufferedReader(new InputStreamReader(dashConn.getInputStream()));
        String dashResponse = "";
        while ((line = dashReader.readLine()) != null) {
            dashResponse += line;
        }
        dashReader.close();
        System.out.println("Dashboard response: " + dashResponse);
    }
}
