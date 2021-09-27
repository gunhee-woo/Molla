package com.dlog.molla;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * This class functions to encrypt and decrypt messages using AES256 encryption.
 * For example:
 * <pre>
 *     MessageEncryption messageEncryption = new MessageEncryption(password);
 *     messageEncryption.AES_Encode(string);
 *     messageEncryption.AES_Decode(string);
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class MessageEncryption {
    /**
     * 사용할 알고리즘을 나타낸다
     */
    private static final String mAlgorithm = "AES";
    /**
     * 사용할 암호화 방식을 나타낸다
     */
    private static final String mBlockNPadding = mAlgorithm + "/CBC/PKCS5Padding";

    /**
     * 비밀키를 나타낸다
     */
    private Key mKeySpec;
    /**
     * 초기화 벡터를 나타낸다
     */
    public static byte[] mIv = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };// 초기화 벡터 => 첫 블록을 암호화할때 사용하는 값

    /**
     * MessageEncryption 생성자
     * @param key 사용자가 입력한 패스워드
     * @throws UnsupportedEncodingException 지원되지 않는 인코딩 사용시 발생
     */
    public MessageEncryption(String key) throws UnsupportedEncodingException {
        String extra = "qazpl,wsxokmedij"; // 16자리 이상의 패스워드를 생성하기 위해 임의의 문자를 사용
        key = key + extra; // 사용자가 입력한 패스워드를 임의의 16자리 문자열 앞에 더함
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int length = b.length;

        if(length > keyBytes.length)
            length = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, length);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, mAlgorithm);
        this.mKeySpec = keySpec;
    }

    /**
     * 문자열 암호화
     * @param str 메시지
     * @return 암호화한 문자열을 반환
     */

    public String AES_Encode(String str) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(mBlockNPadding);
            cipher.init(Cipher.ENCRYPT_MODE, mKeySpec, new IvParameterSpec(mIv));
            byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
            String enstr = new String(Base64.encodeBase64(encrypted));
            return enstr;
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 이미지 암호화
     * @param bytes 이미지 바이트
     * @return 암호화한 이미지 바이트를 반환
     */

    public byte[] AES_Encode(byte[] bytes) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(mBlockNPadding);
            cipher.init(Cipher.ENCRYPT_MODE, mKeySpec, new IvParameterSpec(mIv));
            byte[] encrypted = cipher.doFinal(bytes);
            return encrypted;
        } catch (Exception e) {
            return bytes;
        }
    }

    /**
     * 문자열 복호화
     * @param str 암호화된 문자열
     * @return 복호화된 문자열 반환
     */

    public String AES_Decode(String str)  {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(mBlockNPadding);
            cipher.init(Cipher.DECRYPT_MODE, mKeySpec, new IvParameterSpec(mIv));
            byte[] decoded = Base64.decodeBase64(str.getBytes());
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 이미지 복호화
     * @param bytes 암호화된 이미지 바이트
     * @return 복호화된 이미지 바이트를 반환
     */

    public byte[] AES_Decode(byte[] bytes) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(mBlockNPadding);
            cipher.init(Cipher.DECRYPT_MODE, mKeySpec, new IvParameterSpec(mIv));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            return bytes;
        }
    }
}
