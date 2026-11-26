package com.shadow.controlacceso.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Operaciones criptograficas sobre los UIDs de las tarjetas.
 *
 * <p>Guardamos {@code SHA-256(uid)} en lugar del UID. Para la demo es suficiente;
 * en produccion convendria HMAC-SHA256 con una clave secreta (ver README).</p>
 */
public class CriptoService {

    /** "04:a3:1f:7b" / "04 A3 1F 7B" / "04a31f7b" -> "04A31F7B". */
    public String normalizarUid(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("UID nulo");
        }
        String limpio = uid.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("UID invalido: '" + uid + "'");
        }
        return limpio;
    }

    /** Hash SHA-256 en hexadecimal (64 caracteres) del UID normalizado. */
    public String hashUid(String uid) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest(normalizarUid(uid).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 esta garantizado en toda JVM; esto no deberia pasar nunca
            throw new IllegalStateException(e);
        }
    }

    /** "04:A3:1F:7B" -> "04:A3:**:**" para mostrar en pantalla o en logs. */
    public String enmascararUid(String uid) {
        String n = normalizarUid(uid);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n.length(); i += 2) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(i < 4 ? n.substring(i, Math.min(i + 2, n.length())) : "**");
        }
        return sb.toString();
    }
}
