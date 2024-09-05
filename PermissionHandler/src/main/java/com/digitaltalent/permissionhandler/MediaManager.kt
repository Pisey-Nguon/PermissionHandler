package com.digitaltalent.permissionhandler

import android.Manifest
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.requestTakePhotoOrGallery(callback: (path: String) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES) {
            runWithMediaHandler(this, MediaCheckerFragment.State.ALL, callback)
        }
    } else {
        runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE) {
            runWithMediaHandler(this, MediaCheckerFragment.State.ALL, callback)
        }
    }
}

fun Fragment.requestTakePhotoOrGallery(callback: (path: String) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        runWithPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            forceGranted = false
        ) {
            runWithMediaHandler(this, MediaCheckerFragment.State.ALL, callback)
        }
    } else {
        runWithPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            forceGranted = false
        ) {
            runWithMediaHandler(this, MediaCheckerFragment.State.ALL, callback)
        }
    }

}

fun FragmentActivity.requestTakePhoto(callback: (path: String) -> Unit) {
    runWithPermissions(Manifest.permission.CAMERA) {
        runWithMediaHandler(this, MediaCheckerFragment.State.CAMERA, callback)
    }
}

fun Fragment.requestTakePhoto(callback: (path: String) -> Unit) {
    runWithPermissions(Manifest.permission.CAMERA, forceGranted = false) {
        runWithMediaHandler(this, MediaCheckerFragment.State.CAMERA, callback)
    }
}

fun FragmentActivity.requestPickupImageGallery(callback: (path: String) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        runWithPermissions(Manifest.permission.READ_MEDIA_IMAGES) {
            runWithMediaHandler(this, MediaCheckerFragment.State.GALLERY, callback)
        }
    } else {
        runWithPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            runWithMediaHandler(this, MediaCheckerFragment.State.GALLERY, callback)
        }
    }
}

fun Fragment.requestPickupImageGallery(callback: (path: String) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        runWithPermissions(Manifest.permission.READ_MEDIA_IMAGES, forceGranted = false) {
            runWithMediaHandler(this, MediaCheckerFragment.State.GALLERY, callback)
        }
    } else {
        runWithPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, forceGranted = false) {
            runWithMediaHandler(this, MediaCheckerFragment.State.GALLERY, callback)
        }
    }
}

private fun runWithMediaHandler(
    target: Any?,
    state: MediaCheckerFragment.State,
    callback: (String) -> Unit
) {
    // check if permission check fragment is added or not
    // if not, add that fragment
    val mediaCheckerFragment = MediaCheckerFragment.newInstance(state = state)
    when (target) {
        is FragmentActivity -> {
            // this does not work at the moment
            target.supportFragmentManager.beginTransaction().apply {
                add(mediaCheckerFragment, MediaCheckerFragment::class.java.canonicalName)
                    .runOnCommit {
                        // start requesting permissions for the first time
                        mediaCheckerFragment.requestMedia(callback)
                    }
                commit()
            }
            // make sure fragment is added before we do any context based operations
            target.supportFragmentManager.executePendingTransactions()
        }

        is Fragment -> {
            // this does not work at the moment
            target.childFragmentManager.beginTransaction().apply {
                add(mediaCheckerFragment, MediaCheckerFragment::class.java.canonicalName)
                    .runOnCommit {
                        // start requesting permissions for the first time
                        mediaCheckerFragment.requestMedia(callback)
                    }
                commit()
            }
            // make sure fragment is added before we do any context based operations
            target.childFragmentManager.executePendingTransactions()
        }
    }

}