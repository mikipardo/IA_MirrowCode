import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES256Example {

    public static void main(String[] args) throws Exception {
        String message = "D6C90D7E5F57FC31F6C09C89981BBF9BDB23CD6E4BB6A0C6D6C9C53D763C92D375478C501A5ECBB1477FDE913CCA419F8D742389AEDBF4EAF70EC410";
        String clave_aes = "ADC2111111110101ADC2111111110202ADC2111111110303ADC2111111110404";

        // Convertir la clave de cifrado de hexadecimal a bytes
        byte[] keyBytes = DatatypeConverter.parseHexBinary(clave_aes);

        // Crear una instancia de la clave secreta a partir de los bytes de la clave
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Crear un cifrador AES
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Convertir el mensaje de hexadecimal a bytes
        byte[] messageBytes = DatatypeConverter.parseHexBinary(message);

        // Cifrar el mensaje
        byte[] encryptedBytes = cipher.doFinal(messageBytes);

        // Convertir el resultado del cifrado a una cadena hexadecimal
        String encryptedMessage = DatatypeConverter.printHexBinary(encryptedBytes);

        System.out.println("Mensaje Cifrado: " + encryptedMessage);
    }
}


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES256Example {

    public static void main(String[] args) throws Exception {
        String mensajeCifrado = "C94328E4C40822D8A2A60388C6A0C7265BBFE929FFD31F0A08E6C940EBF2028A";

        String clave_aes = "ADC2111111110101ADC2111111110202ADC2111111110303ADC2111111110404";

        // Convertir la clave de cifrado de hexadecimal a bytes
        byte[] keyBytes = DatatypeConverter.parseHexBinary(clave_aes);

        // Crear una instancia de la clave secreta a partir de los bytes de la clave
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Crear un cifrador AES
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Convertir el mensaje cifrado de hexadecimal a bytes
        byte[] mensajeCifradoBytes = DatatypeConverter.parseHexBinary(mensajeCifrado);

        // Descifrar el mensaje
        byte[] mensajeDescifradoBytes = cipher.doFinal(mensajeCifradoBytes);

        // Convertir el resultado del descifrado a una cadena de texto
        String mensajeDescifrado = new String(mensajeDescifradoBytes);

        System.out.println("Mensaje Descifrado: " + mensajeDescifrado);
    }
}
