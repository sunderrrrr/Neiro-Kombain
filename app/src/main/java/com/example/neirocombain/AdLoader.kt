package com.example.neirocombain

import android.content.Context
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.instream.MobileInstreamAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader


class AdLoader {
    var rewardedAd: RewardedAd? = null
    var rewardedAdLoader: RewardedAdLoader? = null
    private fun loadRewardedAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder("R-M-4088559-2").build()
        rewardedAdLoader?.loadAd(adRequestConfiguration)
    }
    fun showAd(context: Context, rewardedAd: RewardedAd?, rewardedAdLoader: RewardedAdLoader) {
        rewardedAd?.apply {
            setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {
                    // Called when ad is shown.
                }

                override fun onAdFailedToShow(adError: AdError) {
                    // Called when an RewardedAd failed to show
                }

                override fun onAdDismissed() {
                    // Called when ad is dismissed.
                    // Clean resources after Ad dismissed
                    var adEventListener = rewardedAd?.setAdEventListener(null)


                    // Now you can preload the next rewarded ad.
                    loadRewardedAd()
                }

                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    // Called when an impression is recorded for an ad.
                }

                override fun onRewarded(reward: Reward) {
                    // Called when the user can be rewarded.
                    //attemptsLeft = 5
                    //attempts_text.text = "$attemptsLeft/5"

                }
            })
           // show(requireActivity())
        }

    }

    fun load_ad(context: Context, banner: BannerAdView, rewardedAd: RewardedAd?){
        rewardedAdLoader = RewardedAdLoader(context).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                //    context.rewardedAd = rewardedAd
                    // The ad was loaded successfully. Now you can show loaded ad.
                }

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    // Ad failed to load with AdRequestError.
                    // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
                }
            })
        }
        loadRewardedAd()
        MobileAds.initialize(context){
            MobileInstreamAds.setAdGroupPreloading(true)
            MobileAds.enableLogging(true)
            banner.setAdUnitId("R-M-4088559-1")// BANER
            banner.setAdSize(BannerAdSize.fixedSize(context, 320, 70))
            val adRequest: AdRequest = AdRequest.Builder().build()
            println(adRequest)
            banner.run {
                println(adRequest)
                loadAd(adRequest) } }
    }

}