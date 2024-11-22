package com.example.tripsetgo

import androidx.navigation.NavArgs
import android.os.Bundle

data class CapsuleContentFragmentArgs(
    val capsuleId: String,
    val capsuleName: String
) : NavArgs {
    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): CapsuleContentFragmentArgs {
            bundle.let {
                return CapsuleContentFragmentArgs(
                    capsuleId = it.getString("capsuleId") ?: "",
                    capsuleName = it.getString("capsuleName") ?: ""
                )
            }
        }
    }
}