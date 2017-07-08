package com.revof11.javacircleci;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple test that checks the system security setup.
 */
@Test (
  description = "Simple test that checks the system security setup."
)
public class SecurityVerifierTest {

  /**
   * The provider name to use for testing Bouncy Castle.
   */
  public static final String BOUNCY_CASTLE = "BC";

  /**
   * The {@code Logger} to use in cooperation with this test instance.
   */
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  /**
   * Tests the basics of system security.
   * @throws Exception if anything goes horribly wrong
   */
  @Test (
    description = "Tests the basics of system security."
  )
  public void testBasics() throws Exception {
    new SecurityVerifier().assertSecureSystem();
    Security.addProvider((Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance());
  }

  /**
   * Tests the automated generation of secret keys.
   * @param algorithm the algorithm to use for key generation
   * @param provider the provider to use for the algorithm ({@code null} for auto-detect)
   * @param keySize the size of the key to actually generate
   * @throws GeneralSecurityException if our algorithm or provider of choice is not provided
   */
  @Test (
    description = "Tests the automated generation of secret keys.",
    dataProvider = "getSecretKeyCombinations",
    dependsOnMethods = {"testBasics"}
  )
  public void testSecretKeyGeneration(String algorithm, String provider, int keySize) throws GeneralSecurityException {
    try {
      KeyGenerator generator = StringUtils.isNotBlank(provider) ? KeyGenerator.getInstance(algorithm, provider) : KeyGenerator.getInstance(algorithm);
      generator.init(keySize);
      SecretKey secretKey = generator.generateKey();
      byte[] encoded = secretKey.getEncoded();
      String result = new String(Hex.encodeHex(encoded));

      Assert.assertTrue(StringUtils.isNotBlank(result), "Somehow got a blank secret key.");
      if (LOG.isInfoEnabled()) {
        LOG.info(String.format("Secret Key Generation : A[%s], P[%s], S[%d] : %s", algorithm, provider, keySize, result));
      }
    } catch (NoSuchAlgorithmException e) {
      LOG.error(String.format("Algorithm Not Found: %s", algorithm), e);
      throw e;
    }
  }

  /**
   * Retrieves a list of known, valid secret key algorithms and key lengths.
   * @return a list of known, valid secret key algorithms and key lengths
   */
  @DataProvider (name = "getSecretKeyCombinations")
  public static Iterator<Object[]> getSecretKeyCombinations() {
    List<Object[]> all = new ArrayList<>();

    // Java basics
    all.addAll(getFor("AES", null, 128, 256));
    all.addAll(getFor("ARCFOUR", null, 40, 128, 256, 512, 1024));
    all.addAll(getFor("Blowfish", null, 128, 256, 448));
    all.add(new Object[]{"DES", null, 56});
    all.addAll(getFor("DESede", null, 112, 168));
    all.addAll(getFor("RC2", null, 40, 128, 256, 512, 1024));
    all.addAll(getFor("HmacMD5", null, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA1", null, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA256", null, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA384", null, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA512", null, 128, 256, 512, 1024, 2048, 4096));

    // Bouncy Castle
    all.addAll(getFor("AES", BOUNCY_CASTLE, 8, 48, 128, 256));
    all.addAll(getFor("AESWrap", BOUNCY_CASTLE, 8, 48, 128, 256, 448));
    all.addAll(getFor("AESWrap", BOUNCY_CASTLE, 8, 48, 128, 256, 448));
    all.addAll(getFor("Camellia", BOUNCY_CASTLE, 128, 256));
    all.addAll(getFor("Cast5", BOUNCY_CASTLE, 8, 64, 128));
    all.addAll(getFor("Cast6", BOUNCY_CASTLE, 8, 64, 128, 256));
    all.addAll(getFor("DES", BOUNCY_CASTLE, 64));
    all.addAll(getFor("DESede", BOUNCY_CASTLE, 128, 192));
    all.addAll(getFor("DESedeWrap", BOUNCY_CASTLE, 128, 192));
    all.addAll(getFor("GOST28147", BOUNCY_CASTLE, 256));
    all.addAll(getFor("Grainv1", BOUNCY_CASTLE, 80));
    all.addAll(getFor("Grain128", BOUNCY_CASTLE, 128));
    all.addAll(getFor("HC128", BOUNCY_CASTLE, 128));
    all.addAll(getFor("HC256", BOUNCY_CASTLE, 256));
    all.addAll(getFor("Noekeon", BOUNCY_CASTLE, 128));
    all.addAll(getFor("RC2", BOUNCY_CASTLE, 8, 16, 64, 128, 256, 512, 1024));
    all.addAll(getFor("RC4", BOUNCY_CASTLE, 8, 16, 64, 128, 256, 512, 1024, 2048));
    all.addAll(getFor("RC5", BOUNCY_CASTLE, 8, 16, 64, 128));
    all.addAll(getFor("RC5-64", BOUNCY_CASTLE, 8, 16, 64, 128, 256));
    all.addAll(getFor("RC6", BOUNCY_CASTLE, 8, 16, 64, 128, 256));
    all.addAll(getFor("Rijndael", BOUNCY_CASTLE, 8, 16, 64, 128, 256));
    all.addAll(getFor("Salsa20", BOUNCY_CASTLE, 128, 256));
    all.addAll(getFor("SEED", BOUNCY_CASTLE, 128));
    all.addAll(getFor("Serpent", BOUNCY_CASTLE, 128, 256));
    all.addAll(getFor("Skipjack", BOUNCY_CASTLE, 128, 256));
    all.addAll(getFor("TEA", BOUNCY_CASTLE, 128));
    all.addAll(getFor("Twofish", BOUNCY_CASTLE, 128, 256));
    all.addAll(getFor("VMPC", BOUNCY_CASTLE, 128, 6144));
    all.addAll(getFor("VMPC-KSA3", BOUNCY_CASTLE, 128, 6144));
    all.addAll(getFor("XTEA", BOUNCY_CASTLE, 128));
    all.addAll(getFor("HmacMD2", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacMD4", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacRIPEMD128", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacRIPEMD160", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA1", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA224", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA256", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA384", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacSHA512", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));
    all.addAll(getFor("HmacTIGER", BOUNCY_CASTLE, 128, 256, 512, 1024, 2048, 4096));

    // exit
    return all.iterator();
  }


  /**
   * Retreves a {@code List} for all the specified algorithm sizes.
   * @param algorithm the algorithm to get the size list for
   * @param provider the provider the algorithm belongs to
   * @param sizes the list of sizes that we want to generate a reference for
   * @return a {@code List} for all the specified algorithm sizes
   */
  private static List<Object[]> getFor(String algorithm, String provider, int... sizes) {
    return Arrays.stream(sizes).mapToObj(next -> new Object[]{algorithm, provider, next}).collect(Collectors.toList());
  }
}
