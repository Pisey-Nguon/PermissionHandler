package com.digitaltalent.permissionhandler

import android.os.Bundle
import androidx.fragment.app.Fragment

class MediaCheckerFragment : Fragment() {
    companion object {
        private const val ARG_STATE = "state"

        fun newInstance(state: State) = MediaCheckerFragment().apply {
            arguments = Bundle().apply { putSerializable(ARG_STATE, state) }
        }
    }

    private lateinit var mediaHandler: MediaHandler
    private var state: State? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = arguments?.getSerializable(ARG_STATE) as? State
        mediaHandler = MediaHandler(this)
    }

    fun requestMedia(execute: (String) -> Unit) {
        state?.let {
            when (it) {
                State.GALLERY -> mediaHandler.requestPickupImageGallery(execute)
                State.CAMERA -> mediaHandler.requestTakePhoto(execute)
                State.ALL -> mediaHandler.requestMedia(execute)
            }
        }
    }

    enum class State {
        GALLERY,
        CAMERA,
        ALL,
    }
}
