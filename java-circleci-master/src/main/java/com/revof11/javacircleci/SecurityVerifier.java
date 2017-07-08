package com.revof11.javacircleci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

/**
 * Simple class that checks the current security configuration of the system. We use
 * this primarily to be able to determine whether or not key security configuration
 * is in place allowing our service to run on various continuous integration systems
 * as well as verification that our production services are configured properly.
 * There is nothing really fancy that goes on, otherwise.
 */
public class SecurityVerifier {

  /**
   * The name of the class that we use to verify Bouncy Castle existence.
   */
  public static final String BOUNCY_CASTLE_CLASS = "org.bouncycastle.jce.provider.BouncyCastleProvider";

  /**
   * The algorithm to use to check for installation fo the Java Cryptography
   * Extension (JCE) Unlimited Strength Jurisdiction Policy Files.
   */
  public static final String UNLIMITED_STRENGTH_ALGORITHM = "AES";

  /**
   * The minimum key length to check for installation fo the Java Cryptography
   * Extension (JCE) Unlimited Strength Jurisdiction Policy Files.
   */
  public static final int UNLIMITED_STRENGTH_MINIMUM_KEY_LENGTH = 128;

  /**
   * The {@code Logger} to use in cooperation with this utility class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SecurityVerifier.class);

  /**
   * Looks for the BouncyCastle provider in the security configuration.
   * @return {@code true} if Bouncy Castle is available, else {@code false}
   * @see #BOUNCY_CASTLE_CLASS
   * @see <a href="https://www.bouncycastle.org/">Legion of the Bouncy Castle</a>
   */
  public static boolean isBouncyCastleInstalled() {
    boolean installed = true;
    try {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Class.forName(BOUNCY_CASTLE_CLASS, false, classLoader);
    } catch (ClassNotFoundException e) {
      installed = false;
      LOG.error(String.format("Unable to find class (%s) due to error: %s", BOUNCY_CASTLE_CLASS, e.getMessage()), e);
    }
    return installed;
  }

  /**
   * Determines whether or not the Java Cryptography Extension (JCE) Unlimited
   * Strength Jurisdiction Policy Files are installed.
   * @return whether or not the Java Cryptography Extension (JCE) Unlimited Strength
   * Jurisdiction Policy Files are installed
   * @see Cipher#getMaxAllowedKeyLength(String)
   * @see #UNLIMITED_STRENGTH_ALGORITHM
   * @see #UNLIMITED_STRENGTH_MINIMUM_KEY_LENGTH
   * @see <a href="http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html">Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files</a>
   */
  public static boolean isUnlimitedStrength() {
    boolean valid;
    try {
      final int maximumKeyLength = Cipher.getMaxAllowedKeyLength(UNLIMITED_STRENGTH_ALGORITHM);
      if (LOG.isDebugEnabled()) {
        LOG.debug(String.format("System maximum allowed key length (%s): %d", UNLIMITED_STRENGTH_ALGORITHM, maximumKeyLength));
      }
      valid = (maximumKeyLength > UNLIMITED_STRENGTH_MINIMUM_KEY_LENGTH);
    } catch (NoSuchAlgorithmException e) {
      valid = false;
      LOG.error(String.format("Unable to find JCE Unlimited Policy files: %s", e.getMessage()), e);
    }
    return valid;
  }

  /**
   * Runs our basic assertion(s) for system security checks.
   * @see #isBouncyCastleInstalled()
   * @see #isUnlimitedStrength()
   */
  public void assertSecureSystem() {
    if (LOG.isInfoEnabled()) {
      LOG.info("Security Verifier : Verifying");
    }

    assert isBouncyCastleInstalled() : "System not deemed secure enough : Bouncy Castle";
    assert isUnlimitedStrength() : "System not deemed secure enough : Java JCE Unlimited";
  }
}
