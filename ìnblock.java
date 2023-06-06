
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PinBlockGenerator {
    public static void main(String[] args) throws Exception {
        // Datos de entrada
        String pin = "1234";
        String accountNumber = "1234567890123456";
        String key = "0123456789ABCDEF0123456789ABCDEF";

        // Generar el PIN block
        String pinBlock = generatePinBlock(pin, accountNumber, key);
        System.out.println("PIN Block: " + pinBlock);
    }

    public static String generatePinBlock(String pin, String accountNumber, String key) throws Exception {
        // Formatear el PIN en el formato ISO 9564-1 formato 4
        String formattedPin = "04" + pin + "FFFFFFFF";

        // Concatenar el PIN formateado con los dígitos más a la derecha del número de cuenta
        String data = formattedPin + accountNumber.substring(accountNumber.length() - 12);

        // Obtener la clave de cifrado
        byte[] keyBytes = hexStringToByteArray(key);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Crear un objeto de cifrado AES ECB sin relleno
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Cifrar los datos
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Obtener los primeros 8 bytes del resultado cifrado como el PIN block
        byte[] pinBlockBytes = Arrays.copyOf(encryptedData, 8);

        // Convertir los bytes del PIN block a una cadena hexadecimal
        String pinBlock = bytesToHexString(pinBlockBytes);

        return pinBlock;
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return byteArray;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
