
package es.redsys.testing.sis.automation.service.web.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import es.redsys.testing.sis.automation.service.web.constants.Global;
import es.redsys.testing.sis.automation.service.web.objects.Tarjeta;
import es.redsys.testing.sis.automation.service.web.properties.PropertyImpl;
import es.redsys.testing.sis.automation.service.web.utils.LogImplement;
import es.redsys.testing.sis.automation.service.web.utils.Tools;

/**
 * Clase para el manejo del correspondiente Email, y la extracciï¿½n del OTP mediante el tratamiento
 * de la bandeja de entrada y todos sus correos
 * 
 * @authors S6482CS -- Carlos Sanz Tomero
 * @authors S6607DG -- Daniel Grado Guerrero
 * 
 */

public class JavaMail {

  private static PropertyImpl proper = new PropertyImpl();
  private static Logger logger = LogImplement.getFactoryLog(JavaMail.class);
  private static String lastCode = "";

  /**
   * Realiza todas las configuraciones para poder acceder al correo, configura proxy etc...
   * 
   * @return las propiedades del proxy y puertos
   */
  public static Properties configProperties() {
    Properties props = new Properties();
    // solo propiedades
    props.put("mail.store.protocol", proper.getProperty(Global.MAIL_PROTOCOL_MAIL));// imaps
    props.put("mail.imaps.host", proper.getProperty(Global.MAIL_HOST));// outlook.office365.com
    props.put("mail.imaps.port", proper.getProperty(Global.MAIL_PORT));// 995
    props.put("mail.imaps.ssl.enable", "true");
    props.put("mail.imaps.starttls.enable", "true");
    props.put("mail.imaps.auth", "true");
    props.put("mail.imaps.auth.mechanisms", proper.getProperty(Global.MAIL_AUTH));// XOAUTH2
    props.put("mail.imaps.user", proper.getProperty(Global.MAIL_ADDRESS));
    props.put("mail.debug.auth", "true");
    props.put("mail.imaps.sasl.enable", "true");
    props.put("mail.imaps.sasl.mechanisms", proper.getProperty(Global.MAIL_AUTH));// XOAUTH2
    if(proper.getProperty(Global.MAIL_DEBUG).equalsIgnoreCase("true")) {// activar en N:../config.properties
      props.put("mail.debug", "true"); // Activar ante posible fallo de javaMail para datos
    }
    if(proper.getProperty(Global.MAIL_PROXY_ENABLE).equalsIgnoreCase("true")) {
      props.put("mail.imaps.proxy.host", proper.getProperty(Global.MAIL_PROXY_IP));// proxyfs
      props.put("mail.imaps.proxy.port", proper.getProperty(Global.MAIL_PROXY_PORT));// 80
    }
    

    return props;

  }

  /**
   * Configura el correo, la contraseï¿½a he inicia la sesion
   * 
   * @param
   * @return La session configurada
   * @throws IOException
   * @throws ClientProtocolException
   * @throws MessagingException
   * 
   */
  public static Store configSession()
      throws ClientProtocolException, IOException, MessagingException {
  
    // correo carga propiedades
    Properties props = configProperties();

    // open mailbox....
    String token = getAuthToken(proper.getProperty(Global.MAIL_TANANT_ID),
        proper.getProperty(Global.MAIL_CLIENT_ID), proper.getProperty(Global.MAIL_CLIENT_SECRET));
    Session session = Session.getInstance(props);
    Store store = session.getStore(proper.getProperty(Global.MAIL_PROTOCOL_MAIL));   
    store.connect(proper.getProperty(Global.MAIL_HOST), proper.getProperty(Global.MAIL_ADDRESS),
        token);
                   
                  

    if (!store.isConnected()) {
      logger.error("Error de conexión en JavaMail");
    }
    return store;
  }

  public static void limpiarCorreo(LocalDateTime programStartTime)
  
      throws ClientProtocolException, IOException {

    try {
      Store store = configSession();

      Folder emailFolder = store.getFolder("INBOX");
      emailFolder.open(Folder.READ_WRITE);
      Message[] messages = emailFolder.getMessages();
     
      for (Message message : messages) {

        message.getSentDate();
        if (LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
            .isBefore(programStartTime.minusHours(2))) {
          message.setFlag(Flags.Flag.DELETED, true);
 
        } 
      }
      logger.info("limpiando correo de " + store.getURLName().getUsername()); 
      
      emailFolder.close(true);
      store.close();
    } catch (MessagingException e) {
      e.printStackTrace();
    }

  }
  public static void limpiarCorreoConHora(LocalDateTime programStartTime,int hora)
  
      throws ClientProtocolException, IOException {

    try {
      Store store = configSession();

      Folder emailFolder = store.getFolder("INBOX");
      emailFolder.open(Folder.READ_WRITE);
      Message[] messages = emailFolder.getMessages();
     
      for (Message message : messages) {

        message.getSentDate();
        if (LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
            .isBefore(programStartTime.minusHours(hora))) {
          message.setFlag(Flags.Flag.DELETED, true);
 
        } 
      }
      logger.info("limpiando correo de " + store.getURLName().getUsername()); 
      
      emailFolder.close(true);
      store.close();
    } catch (MessagingException e) {
      e.printStackTrace();
    }

  }

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

  /**
   * Metodo para leer el OTP, llama configPropierties() y a configSession() para poder usar
   * 
   * @return el OTP
   */

  public static String leerOTP(String importe, LocalDateTime programStartTime)
      throws MessagingException, IOException {

    String otp = null;

    Store store = configSession();

    Folder emailFolder = store.getFolder("INBOX");

    emailFolder.open(Folder.READ_ONLY);
    Message[] messages = emailFolder.getMessages();

    for (int i = messages.length - 1; i >= 0; i--) {
      Message message = messages[i];
      if (LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
          .isBefore(programStartTime)) {
        break;
      }

      String subject = message.getSubject();
      // Tienes un nuevo SMS en el número SIM 1
      // original Nuevo mensaje SMS a SIM 1 Tienes un nuevo SMS en el n�mero SIM 1
      if (subject.indexOf("Tienes un nuevo SMS en el número SIM 1") >= 0
          || subject.indexOf("Nuevo_mensaje") >= 0) {
        otp = authenticationCodeNew(subject, message, importe);

        if (otp != null && !otp.isEmpty() && !lastCode.equals(otp)) {
          lastCode = otp;
          logger.info("***********************************");
          logger.info("Codigo encontrado: " + otp);
          logger.info("***********************************");
          break;
        }
      }
    }

    emailFolder.close();
    store.close();
   
    return otp;

  }

  public static String leerOTPChallenge(String importe, LocalDateTime programStartTime,
      Tarjeta tarjeta) throws MessagingException, IOException {

    String otp = null;
    Store store = configSession();

    Folder emailFolder = store.getFolder("INBOX");
    emailFolder.open(Folder.READ_ONLY);
    Message[] messages = emailFolder.getMessages();

    for (int i = messages.length - 1; i >= 0; i--) {
      Message message = messages[i];
      if (LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
          .isBefore(programStartTime)) {
        break;
      }

      String subject = message.getSubject();

      if (subject.indexOf("Tienes un nuevo SMS en el número SIM 1") >= 0
          || subject.indexOf("Nuevo_mensaje") >= 0) {

        otp = authenticationCodeChallenge(subject, message, importe, tarjeta);
        if (otp != null && !otp.isEmpty() && !lastCode.equals(otp)) {
          lastCode = otp;
          logger.info("***********************************");
          logger.info("Codigo encontrado: " + otp);
          logger.info("***********************************");

          break;
        }
      }
    }

    emailFolder.close();
    store.close();

    return otp;

  }

  /**
   * Metodo que comprueba el string del mensaje, y busca si es nuestro email a traves de la cantidad
   * y de la fecha en la que se ha realizado y extrae el otp
   * 
   * @param
   * @return el OTP obtenido del correo
   * @throws MessagingException
   * @throws IOException
   * 
   */

  public static String authenticationCode(String subject, Message message, String importe)
      throws MessagingException, IOException {
    String identidad = subject.replace("Mensaje de ", "");
    String fullMessage = message.getContent().toString();

    String otp = null;
    int identidadIndex = fullMessage.indexOf(identidad);
    if (identidadIndex >= 0) {

      String focusMessage =
          fullMessage.substring(identidadIndex + identidad.length() + 2, identidadIndex + 150);
      char[] chars = focusMessage.toCharArray();
      StringBuilder sb = new StringBuilder();
      StringBuilder code = new StringBuilder();

      if (ourAmount(identidad, fullMessage, importe)) {
        for (char c : chars) {

          if (Character.isDigit(c)) {
            sb.append(c); // va aï¿½adiendo al sb cuando es un numero
          } else {
            if ((sb.length() >= 5) && (sb.length() <= 9)) {
              code = sb;
              break;
            } else {
              sb.setLength(0);
            }
          }
        }
        otp = code.toString();
      }
    }
    return otp;
  }

  private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart)
      throws MessagingException, IOException {
    String result = "";
    int count = mimeMultipart.getCount();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      if (bodyPart.isMimeType("text/plain")) {
        result = result + "\n" + bodyPart.getContent();
        break; // without break same text appears twice in my tests
      } else if (bodyPart.isMimeType("text/html")) {
        String html = (String) bodyPart.getContent();
        result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
      } else if (bodyPart.getContent() instanceof MimeMultipart) {
        result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
      }
    }
    return result;
  }

  public static String authenticationCodeNew(String subject, Message message, String importe)
      throws MessagingException, IOException {

    String identidad = subject.replace("Mensaje de ", "");
    String fullMessage = message.getContent().toString();

    if (message.isMimeType("text/plain")) {
      fullMessage = message.getContent().toString();
    } else if (message.isMimeType("multipart/*")) {
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      fullMessage = getTextFromMimeMultipart(mimeMultipart);
    }

    String otp = null;
    int identidadIndex = fullMessage.indexOf("Mensaje:");
    if (identidadIndex >= 0) {

      String focusMessage = fullMessage.substring(identidadIndex, identidadIndex + 150);
      char[] chars = focusMessage.toCharArray();
      StringBuilder sb = new StringBuilder();
      StringBuilder code = new StringBuilder();

      if (ourAmount(identidad, fullMessage, importe)) {
        for (char c : chars) {
          if (Character.isDigit(c)) {
            sb.append(c); // vaï¿½adiendo al sb cuando es un numero
          } else {
            if ((sb.length() >= 5) && (sb.length() <= 9)) {
              code = sb;
              break;
            } else {
              sb.setLength(0);
            }
          }
        }
        otp = code.toString();
      }
    }
    return otp;
  }

  public static String authenticationCodeChallenge(String subject, Message message, String importe,
      Tarjeta tarjeta) throws MessagingException, IOException {
    String identidad = subject.replace("Mensaje de ", "");
    String fullMessage = message.getContent().toString();

    if (message.isMimeType("text/plain")) {
      fullMessage = message.getContent().toString();
    } else if (message.isMimeType("multipart/*")) {
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      fullMessage = getTextFromMimeMultipart(mimeMultipart);
    }

    String otp = null;
    int identidadIndex = fullMessage.indexOf("Mensaje:");
    if (identidadIndex >= 0) {

      String focusMessage = fullMessage.substring(identidadIndex, identidadIndex + 150);
      char[] chars = focusMessage.toCharArray();
      StringBuilder sb = new StringBuilder();
      StringBuilder code = new StringBuilder();


      int i = 0;

      if (ourAmount(identidad, fullMessage, importe)) {
        for (char c : chars) {
          if (Character.isDigit(c)) {
            sb.append(c); // vaï¿½adiendo al sb cuando es un numero
          } else {
            if ((sb.length() >= 5) && (sb.length() <= 9)) {
              code = sb;
              break;
            } else {
              sb.setLength(0);
            }
          }
          i++;
        }

        if (focusMessage.contains("Sume")) {
          int pos1 = -1, pos2 = -1;
          // Conseguimos quï¿½ posiciones son las que hay que sumar
          while (i < chars.length) {
            if (Character.isDigit(chars[i])) {
              if (pos1 == -1)
                pos1 = Character.getNumericValue(chars[i]);
              if (pos1 != -1 && pos2 == -1)
                pos2 = Character.getNumericValue(chars[i + 4]);
            }

            i++;
            if (pos1 != -1 && pos2 != -1) {
              break;
            }
          }
          int temp = Integer.parseInt(code.toString())
              + Character.getNumericValue(tarjeta.getPin().charAt(pos1 - 1))
              + Character.getNumericValue(tarjeta.getPin().charAt(pos2 - 1));
          otp = Integer.toString(temp);
        } else {
          otp = code.toString();
        }
      }
    }
    return otp;
  }

  /**
   * Metodo que compara la cantidad para comprobar que es el correo que buscamos
   * 
   * @return true si es nuestra cantidad, false en caso contrario
   * 
   */

  public static boolean ourAmount(String identidad, String fullMessage, String importe) {

    boolean amountOk;

    String importe2 = importe.replace(',', '.');
    String amount = amount(identidad, fullMessage);
    amount = amount.replace(',', '.');
    amountOk = amount.equals(importe2);
    return amountOk;

  }

  /**
   * Metodo para extraer el amount de nuestro mensaje
   * 
   * @return la cantidad del mensaje
   */

  public static String amount(String identidad, String fullMessage) {

    String amount = "";
    boolean pass1 = false;
    boolean pass2 = false;
    boolean pass3 = false;
    int count = 0;

    int identidadIndex = fullMessage.indexOf("Mensaje:");
    if (identidadIndex >= 0) {

      String focusMessage = fullMessage.substring(identidadIndex, identidadIndex + 150);
      char[] chars = focusMessage.toCharArray();
      StringBuilder sb = new StringBuilder();
      StringBuilder code = new StringBuilder();

      for (char c : chars) {
        if (Character.isDigit(c)) {
          sb.append(c); // va aï¿½adiendo al sb cuando es un numero
          pass1 = true;
          if (pass2 == true) {
            count++;
          }
        } else {
          if ((String.valueOf(c).equals(".") || String.valueOf(c).equals(",")) && (pass1 == true)) {
            sb.append(c);
            pass2 = true;
          } else {
            if (count >= 2) {
              code = sb;
              pass3 = true;
            } else {
              sb.setLength(0);
              pass1 = false;
            }
          }
        }
        if ((count >= 2) && (pass3 == true)) {
          break;
        }
      }
         logger.info(code.toString());
      amount = code.toString();
      amount.replace(',', '.');
      amount.replace(':', '.');
    }

    return amount;
  }// end amount

  public static String authenticationCodeCorrect(JsonArray autheticationCodeSonFull,
      String fullMessage) {

    String pru;
    pru = "";
    return pru;
  }

  public static String leerEnlace(LocalDateTime programStartTime)
      throws MessagingException, IOException {
    String enlace = null;

    Store store = configSession();
    Folder emailFolder = store.getFolder("INBOX");


    emailFolder.open(Folder.READ_ONLY);
    Message[] messages = emailFolder.getMessages();

    for (int i = messages.length - 1; i >= 0; i--) {
      Message message = messages[i];
      if (LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
          .isBefore(programStartTime)) {
        break;
      }

      String subject = message.getSubject();
      String fullMessage = message.getContent().toString();
      System.err.println(fullMessage);
      
      if (message.isMimeType("text/plain")) {
        fullMessage = message.getContent().toString();
      } else if (message.isMimeType("multipart/*")) {
        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
        fullMessage = getTextFromMimeMultipart(mimeMultipart);
      }
      System.out.println(fullMessage);

      if (subject.indexOf("Tienes un nuevo SMS en el número SIM 1") >= 0
          || subject.indexOf("Nuevo_mensaje") >= 0) {
        enlace = linkFinder(subject, message);
        if (enlace != null && !enlace.isEmpty() && !lastCode.equals(enlace)) {
          lastCode = enlace;
             logger.info("Enlace encontrado: " + enlace);
          break;
        }
      }
    }

    emailFolder.close();
    store.close();

    return enlace;
  }
  
  /**
   * Realiza una previa comparacion para ver si esta la url de la web  y descartar previamente para
   * que no hay conflicto
   * @param programStartTime
   * @param enlaceWeb
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  public static String leerEnlaceComparacion(LocalDateTime programStartTime,String enlaceWeb)
      throws MessagingException, IOException {
    String enlace = null;

    Store store = configSession();
    Folder emailFolder = store.getFolder("INBOX");


    emailFolder.open(Folder.READ_ONLY);
    Message[] messages = emailFolder.getMessages();

    for (int i = messages.length - 1; i >= 0; i--) {
      Message message = messages[i];
      if (LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
          .isBefore(programStartTime)) {
        break;
      }
      String fullMessage = message.getContent().toString();
      if (message.isMimeType("text/plain")) {
        fullMessage = message.getContent().toString();
      } else if (message.isMimeType("multipart/*")) {
        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
        fullMessage = getTextFromMimeMultipart(mimeMultipart);
      }
      if(fullMessage.contains(enlaceWeb)) {
        String subject = message.getSubject();
        if (subject.indexOf("Tienes un nuevo SMS en el número SIM 1") >= 0
            || subject.indexOf("Nuevo_mensaje") >= 0) {
          
          enlace = linkFinder(subject, message);
          if (enlace != null && !enlace.isEmpty() && !lastCode.equals(enlace)) {
            lastCode = enlace;
               logger.info("Enlace encontrado: " + enlace);
            break;
          }
        }
      }
    }

    emailFolder.close();
    store.close();

    return enlace;
  }

  private static String linkFinder(String subject, Message message)
      throws MessagingException, IOException {
    String fullMessage = message.getContent().toString();

    if (message.isMimeType("text/plain")) {
      fullMessage = message.getContent().toString();
    } else if (message.isMimeType("multipart/*")) {
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      fullMessage = getTextFromMimeMultipart(mimeMultipart);
    }

    String link = null;
    int identidadIndex = fullMessage.indexOf("Mensaje:");
    if (identidadIndex >= 0) {

      String focusMessage = fullMessage.substring(identidadIndex, identidadIndex + 170);

      if (focusMessage.contains("https://sis-i.redsys.es:25443/sis/")) {
        int beginIndex = focusMessage.indexOf("click: "), endIndex = 0;
        for (int i = focusMessage.length() - 1; i > beginIndex; i--) {
          if (focusMessage.charAt(i) == '(' && focusMessage.charAt(i - 1) == ' ') {
            endIndex = i - 2;
            break;
          }
        }
        link = focusMessage.substring(beginIndex + 7, endIndex + 1);
      }
    }
    return link;
  }
}


