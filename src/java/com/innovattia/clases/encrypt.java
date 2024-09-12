/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 *
 * @author 52556
 */
public class encrypt {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    

    private void preparePublic(String publico) {
        try {
            String pubKey = publico.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\n", "");
            X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(pubKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpecPublic);

        } catch (Exception e) {
            System.out.println("e " + e);
        }
    }

    private void preparePrivate(String privado) {
        try {
            String privKey = privado
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\n", "");
            PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(decode(privKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpecPrivate);
        } catch (Exception e) {
            System.out.println("e " + e);
        }
    }

    public String encrypt(String message, String publico) throws Exception {
        this.preparePublic(publico);
        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        //return encode(encryptedBytes).bytes.encodeBase64().toString();
        return new String(encode(encryptedBytes).getBytes());
    }

    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);

    }

    private static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public String decrypt(String encryptedMessage, String privado) throws Exception {
        this.preparePrivate(privado);
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage, "UTF8");
    }
}
