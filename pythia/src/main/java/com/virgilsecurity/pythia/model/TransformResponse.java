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

package com.virgilsecurity.pythia.model;

import com.google.gson.annotations.SerializedName;
import com.virgilsecurity.common.util.Validator;

/**
 * Plain model representing response from Pythia server.
 * 
 * @author Danylo Oliinyk
 *
 */
public final class TransformResponse {
  @SerializedName("transformed_password")
  private byte[] transformedPassword;

  @SerializedName("proof")
  private Proof proof;

  /**
   * Create a new instance of {@link TransformResponse}.
   */
  public TransformResponse() {
  }

  /**
   * Create a new instance of {@link TransformResponse}.
   *
   * @param transformedPassword
   *          blindedPassword, protected using server secret.
   */
  public TransformResponse(byte[] transformedPassword) {
    Validator.checkNullAgrument(transformedPassword,
                                "TransformResponse -> 'transformedPassword' should not be null");

    this.transformedPassword = transformedPassword;
  }

  /**
   * Create a new instance of {@link TransformResponse}.
   *
   * @param transformedPassword
   *          blindedPassword, protected using server secret.
   * @param proof
   *          the proof.
   */
  public TransformResponse(byte[] transformedPassword, Proof proof) {
    this(transformedPassword);
    Validator.checkNullAgrument(proof, "TransformResponse -> 'proof' should not be null");

    this.proof = proof;
  }

  /**
   * Get the transformed password.
   * 
   * @return the blindedPassword, protected using server secret.
   */
  public byte[] getTransformedPassword() {
    return transformedPassword;
  }

  /**
   * Get the proof.
   * 
   * @return the proof.
   */
  public Proof getProof() {
    return proof;
  }

}
