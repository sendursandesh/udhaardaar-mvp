package com.udhaardaar.mvp

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * App-private encrypted document storage. The original bytes are AES-GCM encrypted
 * with a non-exportable Android Keystore key; the saved SHA-256 identifies the original.
 * Plaintext is materialized only to the app cache when a user explicitly opens a document.
 */
object ArthSaathiDocumentStorage {
    private const val KEY_ALIAS = "arthsaathi_document_v1"
    private const val MAX_BYTES = 25L * 1024L * 1024L
    private val fileNamePattern = Regex("[a-f0-9-]{36}\\.asdoc")

    data class StoredDocument(
        val fileName: String,
        val sha256: String,
        val sizeBytes: Long,
        val extension: String
    )

    fun store(context: Context, input: InputStream, originalName: String): StoredDocument {
        val bytes = input.use { it.readBytes() }
        require(bytes.isNotEmpty()) { "The selected document is empty." }
        require(bytes.size.toLong() <= MAX_BYTES) { "Documents must be 25 MB or smaller." }
        val extension = originalName.substringAfterLast('.', "bin")
            .lowercase().filter { it.isLetterOrDigit() }.take(8).ifBlank { "bin" }
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(bytes)
        val targetDir = File(context.filesDir, "arthsaathi_documents").apply { mkdirs() }
        val name = UUID.randomUUID().toString() + ".asdoc"
        val target = File(targetDir, name)
        FileOutputStream(target).use { output ->
            output.write(cipher.iv.size)
            output.write(cipher.iv)
            output.write(encrypted)
            output.fd.sync()
        }
        return StoredDocument(name, digest, bytes.size.toLong(), extension)
    }

    fun decryptToCache(context: Context, fileName: String, extension: String): File {
        require(fileNamePattern.matches(fileName)) { "Invalid document reference." }
        val dir = File(context.filesDir, "arthsaathi_documents").canonicalFile
        val encryptedFile = File(dir, fileName).canonicalFile
        require(encryptedFile.parentFile == dir && encryptedFile.isFile) { "Stored document is unavailable." }
        val bytes = FileInputStream(encryptedFile).use { it.readBytes() }
        require(bytes.isNotEmpty()) { "Stored document is empty." }
        val ivSize = bytes[0].toInt() and 0xff
        require(ivSize in 12..16 && bytes.size > 1 + ivSize) { "Stored document format is invalid." }
        val iv = bytes.copyOfRange(1, 1 + ivSize)
        val encrypted = bytes.copyOfRange(1 + ivSize, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        val plain = cipher.doFinal(encrypted)
        val safeExtension = extension.lowercase().filter { it.isLetterOrDigit() }.take(8).ifBlank { "bin" }
        context.cacheDir.listFiles()?.filter { it.name.startsWith("arthsaathi_preview_") }?.forEach { it.delete() }
        val preview = File(context.cacheDir, "arthsaathi_preview_" + UUID.randomUUID() + "." + safeExtension)
        FileOutputStream(preview).use { output ->
            output.write(plain)
            output.fd.sync()
        }
        return preview
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
