import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import java.security.Security;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES_Utils {

  /**
   * @author Paula Sowinski Esta clase .java ha sido creada para hacer implementaciones en Asset en
   *         lugar de codificarlas en este. Es importante tener en cuenta que cualquier función que
   *         vayamos a llamar desde Asset, debe recibir por parámetros un array de String, ya que no
   *         admite otra cosa y no nos imprimirá los resultados aunque el código nos funcione en
   *         Java. Todas las funciones tienen implementadas clases nativas de Java, excepto para
   *         generar la CMAC, que necesita la librería externa "bouncycastle".
   */

  public static void main(String[] args) throws Exception {

    System.err.println("Encrypt:");
    System.out.println(encryptAESConPadding(args));
    String encryptData = encryptAESConPadding(args);
    System.err.println("Desencypt:");
    System.out.println(desencryptAESConPadding(new String[] {encryptData, args[1]}));
  }

  // método para generar una CMAC cifrada en AES-128
  public static String generate_cmac_aes(String[] args) {

    String message = args[0];
    String clave_aes = args[1];
    byte[] msgByte;
    byte[] keyByte;
    String resultado;

    msgByte = DatatypeConverter.parseHexBinary(message);
    keyByte = DatatypeConverter.parseHexBinary(clave_aes);

    CipherParameters params = new KeyParameter(keyByte);
    BlockCipher aes = new AESEngine();
    CMac mac = new CMac(aes);
    mac.init(params);
    mac.update(msgByte, 0, msgByte.length);
    byte[] out = new byte[mac.getMacSize()];
    mac.doFinal(out, 0);
    StringBuilder sb = new StringBuilder();
    for (byte b : out) {
      sb.append(String.format("%02X", b));
    }
    resultado = sb.toString();

    // System.out.println(resultado);
    return resultado;
  }

  // método que recibe un PIN y lo transforma en un PIN en claro según formato 4 de ISO 9465-1: 2017
  // formato -> 4 L P P P P P/F P/F P/F P/F P/F P/F P/F P/F F F R R R R R R R R R R R R R R R R
  public static String getPinClaro(String pin) {
    if (pin.length() > 12) {
      pin = pin.substring(0, 12);
    }
    StringBuilder sb = new StringBuilder();
    sb.append("4");
    String longitudPinHex = Integer.toHexString(pin.length());
    sb.append(longitudPinHex.toUpperCase());
    sb.append(pin);
    for (int i = pin.length(); i < 12; i++) {
      sb.append("A");
    }
    sb.append("AA");
    Random randomNumber = new Random();
    for (int i = 0; i < 16; i++) {
      int randomN = randomNumber.nextInt(16);
      sb.append(Integer.toHexString(randomN).toUpperCase());
    }
    String bloquePINclaro = sb.toString();
    return bloquePINclaro;
  }

  // método que recibe un PAN y lo transforma en un PAN en claro según formato 4 de ISO 9465-1: 2017
  // formato -> M A A A A A A A A A A A A A/0 A/0 A/0 A/0 A/0 A/0 A/0 0 0 0 0 0 0 0 0 0 0 0 0
  public static String getPanClaro(String pan) {
    int M = 0;
    int longitud = 0;
    if (pan.length() > 19) {
      pan = pan.substring(0, 19);
    }
    // formateamos los bytes A que son los dígitos del PAN
    if (pan.length() > 12) {
      M = pan.length() - 12;
    } else {
      longitud = 12 - pan.length();
      for (int i = 0; i < longitud; i++) {
        pan = "0" + pan;
      }
    }
    if (pan.length() < 19) {
      pan = pan + "0";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(M);
    sb.append(pan);
    // se rellenan con 0 los bytes de Filler
    for (int i = sb.length(); i < 32; i++) {
      sb.append("0");
    }
    String bloquePANclaro = sb.toString();
    return bloquePANclaro;
  }

  // método para generar el PIN Block cifrado en AES a partir de un PIN en claro, PAN en claro y
  // clave AES
  public static String getPinBlock(String[] args) {
    String pin = args[0];
    String pan = args[1];
    String claveCifrado = args[2];
    String bloquePINclaro, bloquePANclaro, bloqueIntermedioA, bloqueIntermedioB, pinBlock;

    try {
      bloquePINclaro = getPinClaro(pin);
      bloquePANclaro = getPanClaro(pan);

      bloqueIntermedioA = encryptAES(bloquePINclaro, claveCifrado);
      // System.out.println("bloqueIntermedioA => " + bloqueIntermedioA);

      bloqueIntermedioB = xorString(bloqueIntermedioA, bloquePANclaro);
      // System.out.println("bloqueIntermedioB => " + bloqueIntermedioB);

      pinBlock = encryptAES(bloqueIntermedioB, claveCifrado);
      // System.out.println("pinBlock => " + pinBlock);

      return pinBlock;
    } catch (Exception e) {
      System.err.println("algo va mal" + e);
      return "";
    }
  }

  // método para generar un PIN en claro partiendo de un PIN Block cifrado en AES
  public static String getPin(String[] args) {
    String pinBlock = args[0];
    String pan = args[1];
    String claveCifrado = args[2];
    String bloquePANclaro, bloqueIntermedioB, bloqueIntermedioA, bloquePINclaro;

    try {
      bloquePANclaro = getPanClaro(pan);

      bloqueIntermedioB = desencryptAES(pinBlock, claveCifrado);
      // System.out.println("bloqueIntermedioB => " + bloqueIntermedioB);

      bloqueIntermedioA = xorString(bloqueIntermedioB, bloquePANclaro);
      // System.out.println("bloqueIntermedioA => " + bloqueIntermedioA);

      bloquePINclaro = desencryptAES(bloqueIntermedioA, claveCifrado);
      // System.out.println("bloquePINclaro => " + bloquePINclaro);

      return bloquePINclaro;
    } catch (Exception e) {
      return "";
    }
  }

  // método para cifrar un PIN con una clave AES
  public static String encryptAES(String bloquePINclaro, String claveCifrado) throws Exception {

    byte[] claveBytes = hexStringToByteArray(claveCifrado);

    SecretKeySpec secretKey = new SecretKeySpec(claveBytes, "AES");

    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

    byte[] pinBlockBytes = hexStringToByteArray(bloquePINclaro);
    byte[] pinCifradoBytes = cipher.doFinal(pinBlockBytes);

    String pinCifrado = byteArrayToHex(pinCifradoBytes);
    return pinCifrado;
  }

  // método para descifrar una cadena cifrada en AES (por ejemplo, un PIN) con una clave en AES
  public static String desencryptAES(String cadenaCifrada, String claveCifrado) throws Exception {

    byte[] claveBytes = DatatypeConverter.parseHexBinary(claveCifrado);

    SecretKeySpec secretKey = new SecretKeySpec(claveBytes, "AES");

    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, secretKey);

    byte[] cadenaCifradaBytes = DatatypeConverter.parseHexBinary(cadenaCifrada);
    byte[] cadenaClaroBytes = cipher.doFinal(cadenaCifradaBytes);

    StringBuilder sb = new StringBuilder();
    for (byte b : cadenaClaroBytes) {
      sb.append(String.format("%02X", b));
    }
    String cadena = sb.toString();
    return cadena;
  }

  // método para cifrar una cadena cifrada en AES (256)
  public static String encryptAESConPadding(String[] args) throws Exception {
    Security.setProperty("crypto.policy", "unlimited");
    String cadenaEnClaro = args[0];
    String claveCifrado = args[1];

    byte[] claveBytes = hexStringToByteArray(claveCifrado);

    SecretKeySpec secretKey = new SecretKeySpec(claveBytes, "AES");

    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

    byte[] cadeEnClaroBytes = hexStringToByteArray(cadenaEnClaro);
    byte[] cadenaCifradaBytes = cipher.doFinal(cadeEnClaroBytes);

    String cadenaCifrada = byteArrayToHex(cadenaCifradaBytes);
    return cadenaCifrada;
  }

  // método para descifrar una cadena cifrada en AES (256)
  public static String desencryptAESConPadding(String[] args) throws Exception {
    Security.setProperty("crypto.policy", "unlimited");
    String cadenaEnCifrada = args[0];
    String claveCifrado = args[1];


    byte[] claveBytes = DatatypeConverter.parseHexBinary(claveCifrado);

    SecretKeySpec secretKey = new SecretKeySpec(claveBytes, "AES");

    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, secretKey);

    byte[] cadenaCifradaBytes = DatatypeConverter.parseHexBinary(cadenaEnCifrada);
    byte[] cadenaClaroBytes = cipher.doFinal(cadenaCifradaBytes);

    StringBuilder sb = new StringBuilder();
    for (byte b : cadenaClaroBytes) {
      sb.append(String.format("%02X", b));
    }
    String cadena = sb.toString();
    return cadena;
  }

  // método para pasar una cadena hexadecimal a un array de bytes
  public static byte[] hexStringToByteArray(String hexString) {
    int length = hexString.length();
    byte[] byteArray = new byte[length / 2];
    for (int i = 0; i < length; i += 2) {
      byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
          + Character.digit(hexString.charAt(i + 1), 16));
    }
    return byteArray;
  }

  // método para hacer una operación lógica XOR entre dos arrays de bytes
  public static byte[] xorArrayBytes(byte[] operador1, byte[] operador2)
      throws IllegalArgumentException {
    if (operador1.length > operador2.length) {
      throw new IllegalArgumentException("El operador1 es de longitud mayor que el operador2");
    }
    byte[] resultado = new byte[operador1.length];
    for (int cont = 0; cont < operador1.length; cont++) {
      resultado[cont] = (byte) (operador1[cont] ^ operador2[cont]);
    }
    return resultado;
  }

  // método para transformar una cadena hexadecimal a un array de bytes y hacer un XOR entre ellos
  // llamando al método anterior creado
  public static String xorString(String str1, String str2) {
    byte[] str1Hex = DatatypeConverter.parseHexBinary(str1);
    byte[] str2Hex = DatatypeConverter.parseHexBinary(str2);
    byte[] resultado = xorArrayBytes(str1Hex, str2Hex);
    String resultadoHex = byteArrayToHex(resultado);
    return resultadoHex;
  }

  // método para transformar un array de bytes a una cadena hexadecimal
  public static String byteArrayToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }

}
