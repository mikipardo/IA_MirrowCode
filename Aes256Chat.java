/*Claro, para utilizar claves AES de 256 bits, necesitas modificar algunas partes de tu código para que funcione con esta longitud de clave. La encriptación y desencriptación AES-256 requiere el uso de claves de 256 bits (32 bytes). Aquí tienes una versión modificada de tu código original para trabajar con AES-256:*/

```java
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES_Utils_AES256 {

    public static void main(String[] args) throws Exception {
        System.out.println(generate_cmac_aes(args));
        System.out.println(getPinBlock(args));
        System.out.println(getPin(args));
    }

    public static String generate_cmac_aes(String[] args) {
        String message = args[0];
        String clave_aes = args[1];
        byte[] msgByte;
        byte[] keyByte;
        String resultado;

        msgByte = DatatypeConverter.parseHexBinary(message);
        keyByte = DatatypeConverter.parseHexBinary(clave_aes);

        // Asegúrate de que la clave sea de 256 bits (32 bytes)
        if (keyByte.length != 32) {
            throw new IllegalArgumentException("La clave AES debe tener 32 bytes (256 bits) para AES-256.");
        }

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

        System.out.println(resultado);
        return resultado;
    }

    // Resto de los métodos (getPinClaro, getPanClaro, getPin, encryptAES, desencryptAES, hexStringToByteArray,
    // xorArrayBytes, xorString, byteArrayToHex) se mantienen sin cambios.
}
```

/*Esta versión del código realiza las operaciones criptográficas utilizando una clave AES de 256 bits. Asegúrate de proporcionar una clave de 256 bits (32 bytes) como argumento cuando llames a las funciones que utilizan la clave AES. Ten en cuenta que debes asegurarte de manejar y almacenar de forma segura las claves criptográficas, ya que la seguridad es fundamental en estas operaciones.*/
