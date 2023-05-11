package com.selenium.test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OutlookOAuthExample {
    public static void main(String[] args) {
        // Configuración de la aplicación y la cuenta de Outlook
        String clientId = "TU_CLIENT_ID";
        String clientSecret = "TU_CLIENT_SECRET";
        String redirectUri = "TU_REDIRECT_URI";
        String scope = "https://outlook.office.com/mail.read";

        // Paso 1: Obtener el código de autorización
        String authorizationCode = obtainAuthorizationCode(clientId, redirectUri, scope);

        // Paso 2: Intercambiar el código de autorización por un token de acceso
        String accessToken = exchangeAuthorizationCodeForAccessToken(clientId, clientSecret, redirectUri, authorizationCode);

        System.out.println("Token de acceso: " + accessToken);
    }

    private static String obtainAuthorizationCode(String clientId, String redirectUri, String scope) {
        String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
        String responseType = "code";

        try {
            String encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8");
            String encodedScope = URLEncoder.encode(scope, "UTF-8");

            String authorizationUrl = String.format("%s?client_id=%s&response_type=%s&redirect_uri=%s&scope=%s",
                    authorizationEndpoint, clientId, responseType, encodedRedirectUri, encodedScope);

            System.out.println("Por favor, visita la siguiente URL para obtener el código de autorización:");
            System.out.println(authorizationUrl);

            System.out.println("Después de otorgar el permiso, ingresa el código de autorización:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String exchangeAuthorizationCodeForAccessToken(String clientId, String clientSecret,
                                                                  String redirectUri, String authorizationCode) {
        String tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        String grantType = "authorization_code";

        try {
            String encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8");

            String tokenUrl = String.format("%s?client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&grant_type=%s",
                    tokenEndpoint, clientId, clientSecret, authorizationCode, encodedRedirectUri, grantType);

            URL url = new URL(tokenUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();

                // Parsear la respuesta JSON para obtener el token de acceso
                // En este ejemplo, se asume que la respuesta es de la forma {"access_token": "TOKEN"}
                String accessToken = response.substring(response.indexOf(":") + 3, response.lastIndexOf("\""));
                return accessToken;
            } else {
                System.out.println("Error al intercambiar el código de autorización por el token de acceso. Código de respuesta: " + responseCode);
            }
        } catch (IOException e) {
           
