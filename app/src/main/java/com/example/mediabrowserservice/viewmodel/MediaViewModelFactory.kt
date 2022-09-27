package com.example.mediabrowserservice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mediabrowserservice.connection.MediaServiceConnection
import java.lang.IllegalArgumentException


class MediaViewModelFactory(
    private val mediaServiceConnection: MediaServiceConnection
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            return MediaViewModel(mediaServiceConnection) as T
        }
        throw IllegalArgumentException("viewModel class not found.")
    }
}