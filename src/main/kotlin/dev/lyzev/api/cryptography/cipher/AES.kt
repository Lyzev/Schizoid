/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.cryptography.cipher

import dev.lyzev.api.cryptography.hash.Sha256
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES(password: String) {

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

        private fun deriveKeyFromPassword(password: String) = SecretKeySpec(Sha256.hash(password.encodeToByteArray()).copyOf(KEY_SIZE / 8), "AES")
    }

    private val key = deriveKeyFromPassword(password)

    fun encrypt(plainText: String): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec)
        val cipherText = cipher.doFinal(plainText.encodeToByteArray())
        return iv + cipherText
    }

    fun decrypt(cipherText: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = cipherText.copyOfRange(0, GCM_IV_LENGTH)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)
        val plainText = cipher.doFinal(cipherText.copyOfRange(GCM_IV_LENGTH, cipherText.size))
        return plainText.decodeToString()
    }
}
