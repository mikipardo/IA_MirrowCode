import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OutlookTokenExample {
    private static final String CLIENT_ID = "TU_CLIENT_ID";
    private static final String CLIENT_SECRET = "TU_CLIENT_SECRET";
    private static final String REDIRECT_URI = "TU_REDIRECT_URI";
    private static final String SCOPE = "https://outlook.office365.com/.default";

    public static void main(String[] args) {
        try {
            String accessToken = getAccessToken();
            if (accessToken != null) {
                // Utilizar el token de acceso en la configuración de propiedades para IMAP
                Properties properties = new Properties();
                properties.setProperty("mail.store.protocol", "imap");
                properties.setProperty("mail.imap.host", "outlook.office365.com");
                properties.setProperty("mail.imap.port", "993");
                properties.setProperty("mail.imap.ssl.enable", "true");
                properties.setProperty("mail.imap.auth.mechanisms", "OAUTHBEARER");
                properties.setProperty("mail.imap.auth.login.disable", "true");
                properties.setProperty("mail.imap.sasl.enable", "true");
                properties.setProperty("mail.imap.sasl.mechanisms", "OAUTHBEARER");
                properties.setProperty("mail.imap.sasl.oauth2.token", accessToken);
                
                // Utilizar las propiedades en tu código para conectarte a la cuenta de Outlook 365 mediante IMAP
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getAccessToken() throws IOException {
        String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
        String tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/token";

        // Construir la URL de autorización
        String authorizationUrl = authorizationEndpoint + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(SCOPE, "UTF-8");

        // Abrir la URL de autorización en el navegador
        System.out.println("Abre la siguiente URL en tu navegador:");
        System.out.println(authorizationUrl);

        // Leer el código de autorización ingresado por el usuario automáticamente
        String authorizationCode = getCodeAutomatically();

        // Construir la solicitud para obtener el token de acceso
        String tokenRequest = "client_id=" + CLIENT_ID
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                + "&code=" + authorizationCode
                + "&grant_type=authorization_code";

        // Realizar la solicitud POST al endpoint de token
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(tokenRequest.getBytes("UTF-8"));

        // Leer la respuesta del servidor
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        // Extraer el token de acceso de
