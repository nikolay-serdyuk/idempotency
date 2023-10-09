package org.example.idempotency.hash;

import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;
import org.example.idempotency.IdempotencyException;
import org.springframework.security.crypto.codec.Hex;

import javax.annotation.ParametersAreNonnullByDefault;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
public class Md5KeyGenerator implements KeyGenerator {

  @Override
  public String generate(String data) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IdempotencyException(e);
    }
    messageDigest.update(data.getBytes());
    byte[] digest = messageDigest.digest();
    char[] encodedDigest = Hex.encode(digest);
    return new String(encodedDigest);
  }
}
