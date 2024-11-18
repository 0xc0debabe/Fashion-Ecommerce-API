package hmw.ecommerce.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AESUtil {

    private static final String ALGORITHM = "AES";

    @Value("${spring.aes.secret}")
    private String secretKey;

    /**
     * 입력 문자열을 AES 알고리즘을 사용해 암호화합니다.
     *
     * @param input 암호화할 입력 문자열
     * @return 암호화된 문자열 (Base64로 인코딩된 값)
     * @throws Exception 암호화 중 발생할 수 있는 예외
     */
    public String encrypt(String input) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 암호화된 문자열을 AES 알고리즘을 사용해 복호화합니다.
     *
     * @param encryptedInput 암호화된 입력 문자열 (Base64로 인코딩된 값)
     * @return 복호화된 원본 문자열
     * @throws Exception 복호화 중 발생할 수 있는 예외
     */
    public String decrypt(String encryptedInput) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedInput);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

}
