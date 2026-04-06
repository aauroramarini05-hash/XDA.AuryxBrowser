package com.xdustatom.auryxbrowser.playservices

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.review.ReviewManagerFactory

class GoogleServicesManager(private val appContext: Context) {

    companion object {
        private const val IN_APP_UPDATE_REQUEST_CODE = 7406
    }

    private var appUpdateManager: AppUpdateManager? = null

    fun initialize() {
        if (!isPlayServicesAvailable()) return
        appUpdateManager = AppUpdateManagerFactory.create(appContext)
    }

    fun isPlayServicesAvailable(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext)
        return status == ConnectionResult.SUCCESS
    }

    fun showResolvableErrorDialog(activity: Activity): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (status == ConnectionResult.SUCCESS) return false
        if (apiAvailability.isUserResolvableError(status)) {
            apiAvailability.getErrorDialog(activity, status, 9910)?.show()
            return true
        }
        return false
    }

    fun startImmediateUpdateIfAvailable(activity: Activity, onFallback: (() -> Unit)? = null): Boolean {
        val manager = appUpdateManager ?: run {
            onFallback?.invoke()
            return false
        }

        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (!tryStartImmediateUpdate(activity, manager, info)) {
                onFallback?.invoke()
            }
        }.addOnFailureListener {
            onFallback?.invoke()
        }
        return true
    }

    fun resumeImmediateUpdateIfNeeded(activity: Activity) {
        val manager = appUpdateManager ?: return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                tryStartImmediateUpdate(activity, manager, info)
            }
        }
    }

    private fun tryStartImmediateUpdate(
        activity: Activity,
        manager: AppUpdateManager,
        info: AppUpdateInfo
    ): Boolean {
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            return false
        }
        if (!info.isImmediateUpdateAllowed) {
            return false
        }

        return runCatching {
            manager.startUpdateFlowForResult(
                info,
                AppUpdateType.IMMEDIATE,
                activity,
                IN_APP_UPDATE_REQUEST_CODE
            )
        }.isSuccess
    }

    fun requestInAppReview(activity: Activity, onComplete: (() -> Unit)? = null): Boolean {
        if (!isPlayServicesAvailable()) return false

        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                reviewManager.launchReviewFlow(activity, reviewInfo)
                    .addOnCompleteListener { onComplete?.invoke() }
            }
            .addOnFailureListener { onComplete?.invoke() }
        return true
    }
}

object GoogleServices {
    private var instance: GoogleServicesManager? = null

    fun init(application: Application) {
        if (instance == null) {
            instance = GoogleServicesManager(application.applicationContext)
        }
        instance?.initialize()
    }

    fun get(): GoogleServicesManager? = instance
}
