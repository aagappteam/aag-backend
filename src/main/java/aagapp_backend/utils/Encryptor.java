package aagapp_backend.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Encryptor {

    public Encryptor() {
    }

    public static String encrypt(String key, String initVector, String value) throws IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchPaddingException, UnsupportedEncodingException {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(1, skeySpec, iv);
        byte encrypted[] = cipher.doFinal(value.getBytes());
        System.out.println(
                (new StringBuilder("encrypted string: ")).append(Base64.encodeBase64String(encrypted)).toString());
        return Base64.encodeBase64String(encrypted);
    }

    public static String decrypt(String key, String initVector, String encrypted)
            throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(2, skeySpec, iv);
        byte decodedValue[] = (new Base64()).decode(encrypted.getBytes());
        byte original[] = cipher.doFinal(decodedValue);
        return new String(original);
    }

    public static Map splitQuery(String query) throws UnsupportedEncodingException {
        Map query_pairs = new LinkedHashMap();
        Map querypair = new LinkedHashMap();
        String pairs[] = query.split("&");
        String as[];
        int l = (as = pairs).length;
        for (int j = 0; j < l; j++) {
            String pair = as[j];
            int idx = pair.indexOf("=");
            String key = idx <= 0 ? pair : URLDecoder.decode(pair.substring(0, idx), "UTF-8");
            if (!query_pairs.containsKey(key))
                query_pairs.put(key, new LinkedList());
            String value = idx <= 0 || pair.length() <= idx + 1 ? null
                    : URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
            ((List) query_pairs.get(key)).add(value);
        }

        List keys = new ArrayList(query_pairs.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String k = (String) keys.get(i);
            List values = (List) query_pairs.get(k);
            if (values.size() == 0)
                querypair.put(k, "");
            else
                querypair.put(k, (String) values.get(0));
        }

        return querypair;
    }


}
