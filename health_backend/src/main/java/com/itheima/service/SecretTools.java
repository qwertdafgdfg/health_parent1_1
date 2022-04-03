package com.itheima.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version V1.0
 * @Author liyuan
 * @Description //TODO $
 * @Date $ $
 * @Param $
 * @return $
 **/


/**
 *
 * 密码工具
 *
 *
 * @author wangjie
 *
 */
public class SecretTools {

    public static final Logger LOGGER = LoggerFactory.getLogger(SecretTools.class);

    public static final String ALG_AES = "AES";
    protected static final String ALG_HMAC_MD5 = "HmacMD5";

    /**
     * 生成HMAC签名
     *
     * @paramalgorithm  算法
     * @paramplaintext  明文
     * @paramappKey 秘钥
     */
    public static String hmacDigest(String plaintext, String secretKey) {
        try {
            Mac mac = Mac.getInstance(ALG_HMAC_MD5);
            byte[] secretByte = secretKey.getBytes();
            byte[] dataBytes = plaintext.getBytes();
            SecretKey secret = new SecretKeySpec(secretByte, ALG_HMAC_MD5);
            mac.init(secret);
            return byte2HexStr(mac.doFinal(dataBytes));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void run(){
        System.out.println("job execute...");
    }

    public static String hmacDigest1(String plaintext, String secretKey) {
        try {
            Mac e = Mac.getInstance("HmacMD5");
            byte[] secretByte = secretKey.getBytes();
            byte[] dataBytes = plaintext.getBytes();
            SecretKeySpec secret = new SecretKeySpec(secretByte, "HmacMD5");
            e.init(secret);
            return byte2HexStr(e.doFinal(dataBytes));
        } catch (Exception var6) {
            throw new RuntimeException(var6.getMessage(), var6);
        }

    }

    //sign= HmacMD5 (”abc”,”app_id=abc &timestamp= 1611630110 & version=1.0”)
    private String HmacMD5(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacMD5");
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(secretKey);
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; bytes != null && i < bytes.length; i ++) {
            String s = Integer.toHexString(bytes[i] & 0XFF);
            if (s.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(s);
        }
        return stringBuilder.toString().toLowerCase();
    }

    @Test
    public void Test(){
        try{
            //先通过hmacMD5生成 sign    ，再通过base64加密参数里面value值。 组成URL       每个参数的值都需要base64加密传输。
           //String s1 = HmacMD5("bc219fa9-ffb5-2032-e053-1e78a8c02761", "app_id=bc219fa9-ffb4-2032-e053-1e78a8c02761&timestamp=1611630110&version=1.0");
            //bc219fa9-ffb4-2032-e053-1e78a8c02761
            String s1 = HmacMD5("bc219fa9-ffb5-2032-e053-1e78a8c02761", "app_id=bc219fa9-ffb4-2032-e053-1e78a8c02761&timestamp=1617065128&version=1.0");

            //new ClassPathXmlApplicationContext("SpringJobs.xml");
           //1主要是生成sign=bb817f818698a7bac4c8130428a2018a
            //2将sign base64编码生成 = YmI4MTdmODE4Njk4YTdiYWM0YzgxMzA0MjhhMjAxOGE=
            //组成URL。。

           System.out.println(s1.toString());
        }catch (Exception ex){
            System.out.println(ex.toString());
        }

    }


    /**
     * AES加密
     */
    public static String aesEncrypt(String content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(ALG_AES);
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ALG_AES);
            Cipher cipher = Cipher.getInstance(ALG_AES);
            //byte[] byteContent = content.getBytes(StringTools.UTF8);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //return byte2HexStr(cipher.doFinal(byteContent));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * AES解密
     */
    public static String aesDecrypt(String content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(hexStr2Bytes(content));
            return byte2HexStr(result); // 加密
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static String byte2HexStr(byte[] bytes) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; bytes != null && n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    private static byte[] hexStr2Bytes(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }



}