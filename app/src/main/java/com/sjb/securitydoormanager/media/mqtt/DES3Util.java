package com.sjb.securitydoormanager.media.mqtt;


import com.sjb.securitydoormanager.codec.binary.Base64;


import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;

public class DES3Util {

    private final static String ENCODING = "utf-8";

    public static final String DEFAULT_KEY = "TWtzMUtpTTRiMTQxZEdSNlZqQXdjUT09";

    public static final String DEFAULT_INIT_VECTOR = "SGJCcyNjMEY";

    /**
     * 3DES加密
     *
     * @param plainText 普通文本
     * @return
     * @throws Exception
     */
    public static String des3Encode(String plainText) {
        return des3Encode(DEFAULT_KEY, DEFAULT_INIT_VECTOR, plainText);
    }

    public static String des3Encode(String key, String initVector, String plainText) {
        Key deskey = null;
        byte[] encryptData = null;
        try {
            DESedeKeySpec spec = new DESedeKeySpec(Base64.decodeBase64(key));
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
            deskey = keyfactory.generateSecret(spec);

            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(Base64.decodeBase64(initVector));
            cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
            encryptData = cipher.doFinal(plainText.getBytes(ENCODING));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String(encryptData);
    }

    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        String passwordEncode = des3Encode("{\"username\":\"SD00000002\",\"timestamp\":"+ timestamp+"}");
        String passwordDecode = des3Decode(passwordEncode);
        System.out.println(passwordEncode);
        System.out.println(passwordDecode);
    }

    public static String createPass(String sn){
        long timestamp = System.currentTimeMillis();
        String passwordEncode = des3Encode("{\"username\":\""+sn+"\",\"timestamp\":"+ timestamp+"}");
        return passwordEncode;
    }

    /**
     * 3DES解密
     *
     * @param encryptText 加密文本
     * @return
     * @throws Exception
     */
    public static String des3Decode(String encryptText) {
        return des3Decode(DEFAULT_KEY, DEFAULT_INIT_VECTOR, encryptText);
    }

    public static String des3Decode(String key, String initVector, String encryptText) {
        Key deskey = null;
        byte[] decryptData = null;
        String result = "";
        if (null != encryptText && !"".equals(encryptText)) {
            try {
                DESedeKeySpec spec = new DESedeKeySpec(Base64.decodeBase64(key));
                SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
                deskey = keyfactory.generateSecret(spec);
                Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
                IvParameterSpec ips = new IvParameterSpec(Base64.decodeBase64(initVector));
                cipher.init(Cipher.DECRYPT_MODE, deskey, ips);

                decryptData = cipher.doFinal(Base64.decodeBase64(encryptText));
                result = new String(decryptData, ENCODING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


}
