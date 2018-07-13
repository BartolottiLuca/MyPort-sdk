package tesi.barto.myport.model.security;

/**
 * Created by Valentina on 24/08/2017.
 */

import java.security.PublicKey;

public interface ISecurityManager {
    byte[] sign(byte[] toSign);

    boolean verify(PublicKey pubKey, byte[] toUpdate, byte[] toVerify);

    PublicKey getPublicKey();
}
