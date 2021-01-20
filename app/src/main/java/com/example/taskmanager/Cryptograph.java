package com.example.taskmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class Cryptograph {
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String RSA_MODE =  "RSA/ECB/PKCS1Padding";
    private static final String AES_MODE = "AES/ECB/PKCS7Padding";
    private static final String KEY_ALIAS = "—";
    private KeyStore keyStore;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Cryptograph(Context context) throws Exception {
        keyStore = KeyStore.getInstance(AndroidKeyStore);
        keyStore.load(null);

        // Viene generata una coppia di chiavi RSA
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            // Generate a key pair for encryption
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        }

        SharedPreferences pref = context.getSharedPreferences("CRYPTOGRAPHY", Context.MODE_PRIVATE);
        String encryptedKeyB64 = pref.getString("ENCRYPTED_KEY", null);
        if (encryptedKeyB64 == null) {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            byte[] encryptedKey = rsaEncrypt(key);
            encryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("ENCRYPTED_KEY", encryptedKeyB64);
            edit.apply();
        }
    }

    private byte[] rsaEncrypt(byte[] secret) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        // Cifratura del testo
        Cipher inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        // Decifratura del testo
        Cipher output = Cipher.getInstance(RSA_MODE);

        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte)nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for(int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }
        return bytes;
    }

    private Key getSecretKey(Context context) throws Exception{
        SharedPreferences pref = context.getSharedPreferences("CRYPTOGRAPHY", Context.MODE_PRIVATE);
        String encryptedKeyB64 = pref.getString("ENCRYPTED_KEY", null);
        // need to check null, omitted here

        byte[] encryptedKey = Base64.decode(encryptedKeyB64, Base64.DEFAULT);

        byte[] key = rsaDecrypt(encryptedKey);

        return new SecretKeySpec(key, "AES");
    }

    public String encrypt(Context context, byte[] input) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(context));
        byte[] encodedBytes = c.doFinal(input);
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public byte[] decrypt(Context context, byte[] encrypted) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        c.init(Cipher.DECRYPT_MODE, getSecretKey(context));
        return c.doFinal(encrypted);
    }
}
