import java.io.Serializable;
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt implements Serializable {
  private static final long serialVersionUID = 5486865543976730876L;
  private static final String key = "08wR?!5!S6_WO&-v$f#0RUdrEfRoclTh";// claves para encriptar
  private static final String salt = "huwlzO@a*&t8tr83e$l6hiy#k+vl!0cr";// clave para desencriptar
  
// ADC2111111110101ADC2111111110202ADC2111111110303ADC2111111110404
  // D6C90D7E5F57FC31F6C09C89981BBF9BDB23CD6E4BB6A0C6D6C9C53D763C92D375478C501A5ECBB1477FDE913CCA419F8D742389AEDBF4EAF70EC410
  private SecretKey secretKeyTemp;

  public static void main(String[] args) {
    
    Security.setProperty("crypto.policy", "unlimited");
    Encrypt encrypt=new Encrypt();
    
    System.out.println(encrypt.getAES("Hola Mundo"));
    
    System.out.println(encrypt.getAESDecrypt("EYFlz12WQacpWWNOHBktZg=="));
    
    }
  
  public Encrypt() {
    SecretKeyFactory secretKeyFactory;
    KeySpec keySpec;
    try {
      secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
      // es una funcion de derivación de claves con HMAC en SHA 
      keySpec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
      // le pedimos por método PBEkeySpec le pasamos la llave en array, salt para que no repita (aleatorio)
      // las iteraciones y la longitud de cadena
      secretKeyTemp = secretKeyFactory.generateSecret(keySpec);
      // generamos llave secreta [se reduce velocidad de SW,  ojo es la misma llave y salt siempre, si fueran diferentes hay que ponerlas dinamicas]
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  public String getAES(String data) {
    // encriptar
    byte[] iv = new byte[16];
    try {
      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
      // especifica parametro de inicializacion
      SecretKeySpec secretKey = new SecretKeySpec(secretKeyTemp.getEncoded(), "AES");
      // obtenemos codificada la llave temporal en AES
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      // longitud de longitud de bloque (podemos probar en 7)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
      
      return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
      
    } catch (Exception e) {
      
      e.printStackTrace();
    }
    return null;
  }
  
  public String getAESDecrypt (String data) {
    // para desencriptar es igual que el anterior pero a la inversa con el  decoder
    byte[] iv = new byte[16];
    try {
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
    SecretKeySpec secretKey = new SecretKeySpec (secretKeyTemp.getEncoded(), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher .DECRYPT_MODE, secretKey, ivParameterSpec);
    return new String(cipher.doFinal(Base64.getDecoder() .decode(data)));
    } catch (Exception e) {
    e.printStackTrace();
    }
    return null;
  }
}




