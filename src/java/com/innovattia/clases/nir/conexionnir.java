/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases.nir;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author 52556
 */
public class conexionnir {
   
    String host = "jdbc:mysql://10.11.10.64:3306/";
   // String host = "jdbc:mysql://10.11.10.103:3306/";
    String base = "cambioNir";
    String usuario = "root";

    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    byte[] arrayBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    private String psd;
    SecretKey key;

    public conexionnir() throws Exception {
        myEncryptionKey = "YwmNhpTvMWKgmWSTZSTQHdLO";
        myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
        arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        ks = new DESedeKeySpec(arrayBytes);
        skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = skf.generateSecret(ks);

    }
    

    public String encrypt() throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        String encryptedString = null;
        psd = "fT@Sa424gfdc";
        //psd="k755Cit>Jah+";
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] plainText = psd.getBytes(UNICODE_FORMAT);
        byte[] encryptedText = cipher.doFinal(plainText);
        encryptedString = new String(Base64.encodeBase64(encryptedText));

        return encryptedString;
    }

    public String decrypt(String encryptedString) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String decryptedText = null;
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] encryptedText = Base64.decodeBase64(encryptedString.getBytes());
        byte[] plainText = cipher.doFinal(encryptedText);
        decryptedText = new String(plainText);

        return decryptedText;
    }
}
