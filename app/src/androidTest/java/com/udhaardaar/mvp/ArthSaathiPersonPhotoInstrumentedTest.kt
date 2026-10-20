package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

class ArthSaathiPersonPhotoInstrumentedTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before fun setUp() {
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.logout(context)
        context.getSharedPreferences("arthsaathi_master_data", Context.MODE_PRIVATE).edit().clear().commit()
        ArthSaathiSession.login(context, "9876501199")
    }

    @After fun tearDown() {
        ArthSaathiSession.logout(context)
        context.getSharedPreferences("arthsaathi_master_data", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun personPhotoIsEncryptedAndLinkedToThePersonRecord() {
        val person = ArthSaathiCoreEngine.createPerson("Photo QA Person", "9876501188")
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        val bytes = output.toByteArray()
        val stored = ArthSaathiDocumentStorage.store(context, ByteArrayInputStream(bytes), "person_photo.jpg")
        val encryptedFile = File(File(context.filesDir, "arthsaathi_documents"), stored.fileName)
        try {
            assertTrue(encryptedFile.isFile)
            assertFalse(encryptedFile.readBytes().contentEquals(bytes))
            val document = ArthSaathiCoreEngine.saveModule("DOCUMENT", mapOf(
                "f0" to "Photo — Photo QA Person",
                "f1" to "Profile photo",
                "f2" to person.id!!,
                "f3" to stored.fileName,
                "f4" to stored.sha256,
                "sizeBytes" to stored.sizeBytes,
                "extension" to stored.extension,
                "mimeType" to "image/jpeg",
                "encrypted" to true,
                "storage" to "ANDROID_KEYSTORE_AES_GCM"
            ), mapOf("relatedId" to person.id!!, "personId" to person.id!!))
            val update = ArthSaathiCoreEngine.update(person.id!!, mapOf(
                "photoDocumentId" to document.id!!,
                "photoFileName" to stored.fileName,
                "photoExtension" to stored.extension,
                "photoSha256" to stored.sha256
            ), "user")
            assertTrue(document.ok && update.ok)
            assertEquals(document.id, ArthSaathiCoreEngine.find(person.id!!)?.optString("photoDocumentId"))
            assertTrue(ArthSaathiCoreEngine.linked(person.id!!).any { it.optString("id") == document.id })
            val preview = ArthSaathiDocumentStorage.decryptToCache(context, stored.fileName, stored.extension)
            val digest = MessageDigest.getInstance("SHA-256").digest(preview.readBytes())
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            assertEquals(stored.sha256, digest)
            preview.delete()
        } finally {
            encryptedFile.delete()
        }
    }
}
