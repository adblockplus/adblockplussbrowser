package org.adblockplus.adblockplussbrowser.app.ui.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.postDelayed
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.app.databinding.StatusSnackbarBinding
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.UpdateStatus
import javax.inject.Inject

@AndroidEntryPoint
class StatusSnackbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @Inject
    lateinit var subscriptionsManager: SubscriptionsManager

    private val delayHandler = Handler(Looper.getMainLooper())

    private val binding: StatusSnackbarBinding =
        StatusSnackbarBinding.inflate(LayoutInflater.from(context), this, true)

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.mainScreenProgress.isIndeterminate = false
        binding.mainScreenProgress.max = 100
        binding.mainScreenErrorAction.setOnClickListener {
            subscriptionsManager.scheduleImmediate(force = true)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        hideSnackBar()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        delayHandler.removeCallbacksAndMessages(null)
    }

    fun setStatus(status: UpdateStatus) {
        when (status) {
            is UpdateStatus.Progress -> showProgress(status.progress)
            UpdateStatus.Error -> showError()
            UpdateStatus.None, UpdateStatus.Completed -> hideSnackBar()
        }
    }

    private fun showProgress(progress: Int) {
        delayHandler.removeCallbacksAndMessages(null)
        setProgressViewsVisible(visible = true)
        setErrorViewsVisible(visible = false)
        binding.mainScreenProgress.setProgressCompat(progress, true)
    }

    private fun showError() {
        delayHandler.removeCallbacksAndMessages(null)
        setProgressViewsVisible(visible = false)
        setErrorViewsVisible(visible = true)
        hideSnackBar(5000)
        binding.mainScreenProgress.progress = 0
    }

    private fun hideSnackBar(delay: Long = 2000) {
        if (isVisible) {
            delayHandler.removeCallbacksAndMessages(null)
            delayHandler.postDelayed(delayInMillis = delay) {
                animate().alpha(0f).withEndAction {
                    isGone = true
                }
                binding.mainScreenProgress.progress = 0
            }
        } else {
            binding.mainScreenProgress.progress = 0
        }
    }

    private fun setProgressViewsVisible(visible: Boolean) {
        showSnackBar()
        binding.mainScreenProgress.isVisible = visible
        binding.mainScreenProgressLabel.isVisible = visible
    }

    private fun setErrorViewsVisible(visible: Boolean) {
        showSnackBar()
        binding.mainScreenErrorLabel.isVisible = visible
        binding.mainScreenErrorAction.isVisible = visible
    }

    private fun showSnackBar() {
        if (isGone) {
            isVisible = true
            alpha = 0f
        }
        animate().alpha(1f).start()
    }
}

@BindingAdapter("currentStatus")
internal fun setCurrentStatus(snackbar: StatusSnackbar, status: UpdateStatus) {
    snackbar.setStatus(status)
}