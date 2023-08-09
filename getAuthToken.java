  public static String getAuthToken(String tanantId, String clientId, String client_secret)
      throws ClientProtocolException, IOException {
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost loginPost =
        new HttpPost("https://login.microsoftonline.com/" + tanantId + "/oauth2/v2.0/token");
    String scopes = "https://outlook.office365.com/.default";

    String encodedBody = "client_id=" + clientId + "&scope=" + scopes + "&client_secret="
        + client_secret + "&grant_type=client_credentials";
    loginPost.setEntity(new StringEntity(encodedBody, ContentType.APPLICATION_FORM_URLENCODED));
    loginPost.addHeader(new BasicHeader("cache-control", "no-cache"));
    CloseableHttpResponse loginResponse = client.execute(loginPost);
    InputStream inputStream = loginResponse.getEntity().getContent();
    // para Java superior a 8
    // byte[] response = readAllBytes(inputStream);
    // byte[] response = inputStream.toString().getBytes(StandardCharsets.UTF_8);
    byte[] response = IOUtils.toByteArray(inputStream);
    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.constructType(objectMapper.getTypeFactory()
        .constructParametricType(Map.class, String.class, String.class));
    // --activar e, caso normal
    // Map<String, String> parsed = new ObjectMapper().readValue(response, type);
    // return parsed.get("access_token");
    // --activar en caso de ejecutar y exportar jar

    String auxPrueba = new ObjectMapper().readValue(response, type).toString();

    return Tools.extraerToken(auxPrueba);
  }
