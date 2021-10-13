/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.base.media

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView

class LocalMediaPlayer : TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {

    private var textureView: TextureView? = null
    private var surface: Surface? = null
    private var mediaPlayer: MediaPlayer? = null
    private var repeat: Boolean = false
    private var delay: Long = 0L
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val repeatRunnable: Runnable = Runnable {
        mediaPlayer?.start()
    }

    fun create(texture: TextureView) {
        releaseTexture()
        texture.surfaceTextureListener = this
        textureView = texture
    }

    fun start(uri: Uri, repeat: Boolean = false, delay: Long = 0L) {
        releasePlayer()
        this.repeat = repeat
        this.delay = delay
        val textureView = textureView
        if (textureView != null) {
            mediaPlayer = MediaPlayer().also {
                it.setOnPreparedListener(this)
                it.setOnCompletionListener(this)
                it.setSurface(this.surface)
                it.setDataSource(textureView.context, uri)
                it.prepareAsync()
            }
        } else {
            throw IllegalStateException("create() should be called before start()")
        }
    }

    fun stop() {
        releasePlayer()
    }

    fun destroy() {
        releaseTexture()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        this.surface = Surface(surface)
        mediaPlayer?.setSurface(this.surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        this.surface = null
        mediaPlayer?.setSurface(this.surface)
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer?.start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (repeat) {
            handler.postDelayed(repeatRunnable, delay)
        }
    }

    private fun releaseTexture() {
        textureView?.let {
            it.surfaceTextureListener = null
            textureView = null
        }
    }

    private fun releasePlayer() {
        handler.removeCallbacks(repeatRunnable)
        mediaPlayer?.let {
            it.setOnPreparedListener(null)
            it.setOnCompletionListener(null)
            it.release()
            mediaPlayer = null
        }
    }
}