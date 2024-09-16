/*
 * Copyright (c) 2015-2020, Virgil Security, Inc.
 *
 * Lead Maintainer: Virgil Security Inc. <support@virgilsecurity.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     (1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *     (2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     (3) Neither the name of virgil nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.virgilsecurity.pythia.crypto;

import com.virgilsecurity.crypto.pythia.Pythia;
import com.virgilsecurity.crypto.pythia.PythiaComputeTransformationKeyPairResult;
import com.virgilsecurity.crypto.pythia.PythiaTransformResult;
import com.virgilsecurity.pythia.SampleDataHolder;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException;

import org.apache.commons.lang.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for {@link VirgilPythiaCrypto}.
 * 
 * @author Andrii Iakovenko
 *
 */
public class VirgilPythiaCryptoTest {

  private PythiaCrypto pythiaCrypto;
  private SampleDataHolder sample;

  @BeforeEach
  public void setup() {
    this.pythiaCrypto = new VirgilPythiaCrypto();

    this.sample = new SampleDataHolder("com/virgilsecurity/pythia/crypto/pythia-crypto.json");
  }

  @Test
  public void blind() {
    String password = this.sample.get("kPassword");

    Set<BlindResult> blindResults = new HashSet<>();
    for (int i = 0; i < 10; i++) {
      BlindResult blindResult = this.pythiaCrypto.blind(password);

      // blindResult should be different on each iteration
      for (BlindResult res : blindResults) {
        if (ArrayUtils.isEquals(res.getBlindedPassword(), blindResult.getBlindedPassword())
            && ArrayUtils.isEquals(res.getBlindingSecret(), blindResult.getBlindingSecret())) {
          fail();
        }
      }
      blindResults.add(blindResult);
    }
  }

  @Test
  public void deblind() {
    String password = this.sample.get("kPassword");
    byte[] transformationKeyId = this.sample.getBytes("kTransformationKeyID");
    byte[] pythiaSecret = this.sample.getBytes("kPythiaSecret");
    byte[] pythiaScopeSecret = this.sample.getBytes("kPythiaScopeSecret");
    byte[] tweek = this.sample.getBytes("kTweek");
    byte[] deblindedPassword = this.sample.getHexBytes("kDeblindedPassword");

    BlindResult blindResult = this.pythiaCrypto.blind(password);

    PythiaComputeTransformationKeyPairResult transformationKeyPair = Pythia
        .computeTransformationKeyPair(transformationKeyId, pythiaSecret, pythiaScopeSecret);
    PythiaTransformResult transformResult = Pythia.transform(blindResult.getBlindedPassword(),
        tweek, transformationKeyPair.getTransformationPrivateKey());
    byte[] deblindResult = this.pythiaCrypto.deblind(transformResult.getTransformedPassword(),
        blindResult.getBlindingSecret());
    assertArrayEquals(deblindedPassword, deblindResult);
  }

  @Test
  public void verify() {
    // TODO
  }

  @Test
  public void updateDeblinded() {
    // TODO
  }

  @Test
  public void generateSalt() {
    byte[] salt1 = this.pythiaCrypto.generateSalt();
    assertNotNull(salt1);
    assertEquals(32, salt1.length);

    byte[] salt2 = this.pythiaCrypto.generateSalt();
    assertNotNull(salt2);
    assertEquals(32, salt2.length);
    assertFalse(Arrays.equals(salt1, salt2));
  }

  @Test
  public void generateKeyPair() throws CryptoException {
    byte[] seed = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);

    VirgilKeyPair keyPair = this.pythiaCrypto.generateKeyPair(seed);
    assertNotNull(keyPair);

    VirgilPrivateKey privateKey = keyPair.getPrivateKey();
    assertNotNull(privateKey);
    assertNotNull(privateKey.getIdentifier());
    assertTrue(privateKey.getPrivateKey().isValid());

    VirgilPublicKey publicKey = keyPair.getPublicKey();
    assertNotNull(publicKey);
    assertNotNull(publicKey.getIdentifier());
    assertTrue(publicKey.getPublicKey().isValid());

    assertArrayEquals(privateKey.getIdentifier(), publicKey.getIdentifier());
  }

  @Test
  public void blind_concurrent() throws InterruptedException {
    final String password = this.sample.get("kPassword");

    ExecutorService es = Executors.newCachedThreadPool();
    for (int i = 0; i < 100; i++) {
      es.execute(new Runnable() {

        @Override
        public void run() {
          VirgilPythiaCrypto crypto = new VirgilPythiaCrypto();
          crypto.blind(password);
          System.gc();
        }
      });
    }
    es.shutdown();
    es.awaitTermination(1, TimeUnit.MINUTES);
  }

}
