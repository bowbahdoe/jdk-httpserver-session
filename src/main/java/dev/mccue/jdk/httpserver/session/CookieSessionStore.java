package dev.mccue.jdk.httpserver.session;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

final class CookieSessionStore implements SessionStore {

    static final String HMAC_ALGORITHM = "HmacSHA256";
    static final String CRYPT_TYPE = "AES";
    static final String CRYPT_ALGO = "AES/CBC/PKCS5Padding";

    static {
        // Ensure cipher-algorithm classes are preloaded
        try {
            Cipher.getInstance(CRYPT_ALGO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static String hmac(byte[] key, byte[] data) {
        try {
            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            return Base64.getEncoder().encodeToString(mac.doFinal(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] randomBytes(int size) {
        var seed = new byte[size];
        new SecureRandom().nextBytes(seed);
        return seed;
    }

    static byte[] concat(byte[] a, byte[] b) {
        var c = new byte[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i];
        }
        for (int i = 0; i < b.length; i++) {
            c[a.length + i] = b[i];
        }
        return c;
    }

    static byte[] encrypt(byte[] key, byte[] data) {
        try {
            var cipher = Cipher.getInstance(CRYPT_ALGO);
            var secretKey = new SecretKeySpec(key, CRYPT_TYPE);
            var iv = randomBytes(cipher.getBlockSize());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return concat(iv, cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    record SplitArrays(byte[] a, byte[] b) {
        static SplitArrays splitAt(int i, byte[] data) {
            var a = new byte[i];
            var b = new byte[data.length - i];
            for (int j = 0; j < i; j++) {
                a[j] = data[j];
            }
            for (int j = 0; j < (data.length - i); j++) {
                b[j] = data[i + j];
            }
            return new SplitArrays(a, b);
        }
    }



    static String decrypt(byte[] key, byte[] data) {
        try {
            var cipher = Cipher.getInstance(CRYPT_ALGO);
            var secretKey = new SecretKeySpec(key, CRYPT_TYPE);

            var splitArrays = SplitArrays.splitAt(cipher.getBlockSize(), data);
            var iv = splitArrays.a;
            var encryptedData = splitArrays.b;

            var ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return new String(
                    cipher.doFinal(encryptedData),
                    StandardCharsets.UTF_8
            );
        } catch (InvalidAlgorithmParameterException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | NoSuchAlgorithmException
                 | BadPaddingException
                 | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    static String seal(byte[] key, SessionData sessionData) {
        var serialized = Json.writeString(serialize(sessionData))
                .getBytes(StandardCharsets.UTF_8);
        var data = encrypt(key, serialized);
        return Base64.getEncoder().encodeToString(data) + "--" + hmac(key, data);
    }

    // Timing resistant equality check
    static boolean checkEqual(String a, String b) {
        if (a != null && b != null && a.length() == b.length()) {
            int total = 0;
            for (int i = 0; i < a.length(); i++) {
                total |= a.charAt(i) ^ b.charAt(i);
            }
            return total == 0;
        }
        return false;
    }

    static SessionData unseal(byte[] key, String string) {
        var split = string.split("--", 2);
        if (split.length != 2) {
            return null;
        }

        var data = split[0];
        var mac = split[1];
        var decodedData = Base64.getDecoder().decode(data);

        if (checkEqual(mac, hmac(key, decodedData))) {
            var decrypted = decrypt(key, decodedData);
            return deserialize(Json.readString(decrypted));
        }

        return null;
    }

    static JsonObject serialize(SessionData sessionData) {
        return sessionData.value();
    }

    static SessionData deserialize(Json json) {
        return new SessionData(JsonDecoder.object(json));
    }

    final byte[] key;
    CookieSessionStore(byte[] key) {
        if (key.length != 16) {
            throw new IllegalArgumentException("Key must be 16 bytes: " + key.length);
        }

        this.key = new byte[key.length];
        System.arraycopy(key, 0, this.key, 0, key.length);
    }

    @Override
    public Optional<SessionData> read(SessionKey key) {
        return Optional.ofNullable(unseal(this.key, key.value()));
    }

    @Override
    public SessionKey write(SessionKey key, SessionData data) {
        return new SessionKey(seal(this.key, data));
    }

    @Override
    public Optional<SessionKey> delete(SessionKey key) {
        return Optional.of(new SessionKey(seal(this.key, new SessionData(Json.emptyObject()))));
    }
}
