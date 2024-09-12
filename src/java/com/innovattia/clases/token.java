/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases;

import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Innovattia
 */
public class token {

    private final SecureRandom secureRandom = new SecureRandom(); //threadsafe

    public String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        byte[] encodedBytes = Base64.encodeBase64(randomBytes);
        String  token =new String(encodedBytes);
        return token;
    }
}
