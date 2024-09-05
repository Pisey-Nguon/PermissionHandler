package com.digitaltalent.permissionhandler

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MediaHandler(private val fragment: Fragment) {

    private val context: Context by lazy { fragment.requireContext() }
    private var onMediaResult: ((String) -> Unit)? = null
    private lateinit var currentTakePhotoFile: File

    private val mediaRequestLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data
                val imagePath: String = if (imageUri != null) {
                    // Image picked from gallery
                    copyImageToAppDir(context, imageUri).path

                } else {
                    // Image captured from camera
                    fixRotate(currentTakePhotoFile).path
                }
                onMediaResult?.invoke(imagePath)
            }
        }

    private val takePhotoRequestLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                onMediaResult?.invoke(currentTakePhotoFile.path)
            }
        }

    private val pickupImageGalleryRequestLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let {
                    val path = copyImageToAppDir(context, it).path
                    onMediaResult?.invoke(path)
                }
            }
        }

    @Suppress("DEPRECATION")
    @SuppressLint("SimpleDateFormat")
    fun copyImageToAppDir(context: Context, uri: Uri): File {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )

            else -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))

        }

        // Compress the image
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        val byteArray = outStream.toByteArray()

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val file = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )

        // Write the compressed image to the file
        FileOutputStream(file).use { output ->
            output.write(byteArray)
        }

        return file
    }

    private fun fixRotate(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            }
        }

        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val outStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)

        FileOutputStream(file).use { fos ->
            fos.write(outStream.toByteArray())
        }

        return file
    }


    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(timeStamp: String, storageDir: File): File {
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentTakePhotoFile = this
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun requestMedia(onResult: (String) -> Unit) {
        this.onMediaResult = onResult
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val photoFile = try {
                createImageFile(
                    SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()),
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                )
            } catch (ex: IOException) {
                null
            }
            photoFile?.let { file ->
                val currentTakePhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                putExtra(MediaStore.EXTRA_OUTPUT, currentTakePhotoUri)
            }

        }

        val chooserIntent = Intent.createChooser(pickIntent, "Select Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent))
        }

        mediaRequestLauncher.launch(chooserIntent)
    }

    @SuppressLint("SimpleDateFormat")
    fun requestTakePhoto(onResult: (String) -> Unit) {
        this.onMediaResult = onResult
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {

            val photoFile = try {
                createImageFile(
                    SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()),
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                )
            } catch (ex: IOException) {
                null
            }
            photoFile?.let { file ->
                val currentTakePhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                putExtra(MediaStore.EXTRA_OUTPUT, currentTakePhotoUri)
                takePhotoRequestLauncher.launch(this)
            }

        }
    }

    fun requestPickupImageGallery(onResult: (String) -> Unit) {
        this.onMediaResult = onResult
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickupImageGalleryRequestLauncher.launch(intent)
    }
}
