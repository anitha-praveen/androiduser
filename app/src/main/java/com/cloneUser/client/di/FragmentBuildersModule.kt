package com.cloneUser.client.di

import com.cloneUser.client.SecondFragment
import com.cloneUser.client.dialogs.addressEdit.AddressEditDialog
import com.cloneUser.client.dialogs.cancelReasons.CancelReasonsDialog
import com.cloneUser.client.dialogs.countrylist.CountryListDialog
import com.cloneUser.client.dialogs.cropImageDialog.CropImageDialog
import com.cloneUser.client.dialogs.defaultDialog.DefaultDialog
import com.cloneUser.client.dialogs.disclosure.DisclosureDialog
import com.cloneUser.client.dialogs.tripCancelled.TripCancelledDialog
import com.cloneUser.client.dialogs.waitingProgress.WaitingProgressDialog
import com.cloneUser.client.drawer.aboutUs.AboutUsFrag
import com.cloneUser.client.drawer.applyPromo.ApplyPromoDialog
import com.cloneUser.client.drawer.complaint.addComplaints.ComplaintFragment
import com.cloneUser.client.drawer.complaint.history.common.ComplaintsListHistory
import com.cloneUser.client.drawer.complaint.history.complaintsHistory.ComplaintsHistory
import com.cloneUser.client.drawer.complaint.history.suggestionHistory.SuggestionHistory
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationFragment
import com.cloneUser.client.drawer.faq.FaqFragment
import com.cloneUser.client.drawer.favorites.addFavorite.AddFavoriteFragment
import com.cloneUser.client.drawer.favorites.listFavorite.FavoriteFragment
import com.cloneUser.client.drawer.favorites.pickFromMap.PickFromMapFragment
import com.cloneUser.client.drawer.invoice.InvoiceFragment
import com.cloneUser.client.drawer.language.LanguageFrag
import com.cloneUser.client.drawer.mapfragment.MapFragment
import com.cloneUser.client.drawer.myRides.HistoryListFragment
import com.cloneUser.client.drawer.myRides.cancelled.CancelledHistoryFragment
import com.cloneUser.client.drawer.myRides.completed.CompletedHistoryList
import com.cloneUser.client.drawer.myRides.scheduled.ScheduledHistoryFragment
import com.cloneUser.client.drawer.notificationList.NotificationFragment
import com.cloneUser.client.drawer.outStation.OutStationFrag
import com.cloneUser.client.drawer.outStation.listOutStation.OutStationListFragment
import com.cloneUser.client.drawer.outStation.outstationInvoice.OutstationInvoice
import com.cloneUser.client.drawer.outStation.outstationSearchPlace.OutStationSearchFragment
import com.cloneUser.client.drawer.profile.ProfileFragment
import com.cloneUser.client.drawer.profile.proAddFvourites.ProAddFavourites
import com.cloneUser.client.drawer.rating.RatingFragment
import com.cloneUser.client.drawer.referral.ReferralFrag
import com.cloneUser.client.drawer.rental.RentalFrag
import com.cloneUser.client.drawer.rental.rentalInvoice.RentalInvoiceFrag
import com.cloneUser.client.drawer.rideConfirm.RideConfirmFragment
import com.cloneUser.client.drawer.rideConfirm.changeAddress.RideConfirmChangeAddress
import com.cloneUser.client.drawer.sos.SosFragment
import com.cloneUser.client.drawer.suggestion.SuggestionFragment
import com.cloneUser.client.drawer.support.SupportFragment
import com.cloneUser.client.drawer.trip.TripFragment
import com.cloneUser.client.drawer.tripAddressChange.TripAddressChangeFrag
import com.cloneUser.client.drawer.wallet.WalletFragment
import com.cloneUser.client.loginOrSignup.getStarteedScrn.GetStartedScreen
import com.cloneUser.client.loginOrSignup.login.LoginFragment
import com.cloneUser.client.loginOrSignup.otp.OtpFragment
import com.cloneUser.client.loginOrSignup.register.RegisterFragment
import com.cloneUser.client.loginOrSignup.tour.TourGuideFrag
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributesMapFragment(): MapFragment

    @ContributesAndroidInjector
    abstract fun contributesSecondFragment(): SecondFragment

    @ContributesAndroidInjector
    abstract fun contributeGetStartedFrag(): GetStartedScreen

    @ContributesAndroidInjector
    abstract fun contributeTourGuideFrag(): TourGuideFrag

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeCountryListDialog(): CountryListDialog

    @ContributesAndroidInjector
    abstract fun contributeOtpFrag(): OtpFragment

    @ContributesAndroidInjector
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment


    @ContributesAndroidInjector
    abstract fun contributeHistoryListFragment(): HistoryListFragment

    @ContributesAndroidInjector
    abstract fun contributeComplaintFragment(): ComplaintFragment

    @ContributesAndroidInjector
    abstract fun contributeDisclosureDialog(): DisclosureDialog

    @ContributesAndroidInjector
    abstract fun contributeFavoriteFragment(): FavoriteFragment

    @ContributesAndroidInjector
    abstract fun contributeAddFavoriteFragment(): AddFavoriteFragment

    @ContributesAndroidInjector
    abstract fun contributePickFromMapFragment(): PickFromMapFragment

    @ContributesAndroidInjector
    abstract fun contributeConfirmDestinationFragment(): ConfirmDestinationFragment

    @ContributesAndroidInjector
    abstract fun contributeRideConfirmFragment(): RideConfirmFragment


    @ContributesAndroidInjector
    abstract fun contributeApplyPromoDialog(): ApplyPromoDialog


    @ContributesAndroidInjector
    abstract fun contributeSupportFragment(): SupportFragment

    @ContributesAndroidInjector
    abstract fun contributeSosFragment(): SosFragment

    @ContributesAndroidInjector
    abstract fun contributeWaitingProgressDialog(): WaitingProgressDialog

    @ContributesAndroidInjector
    abstract fun contributeWaitingDefaultDialog(): DefaultDialog

    @ContributesAndroidInjector
    abstract fun contributeTripFragment(): TripFragment

    @ContributesAndroidInjector
    abstract fun contributeCancelReasonsDialog(): CancelReasonsDialog

    @ContributesAndroidInjector
    abstract fun contributeTripCancelledDialog(): TripCancelledDialog

    @ContributesAndroidInjector
    abstract fun contributeInvoice(): InvoiceFragment

    @ContributesAndroidInjector
    abstract fun contributeRatingFragment(): RatingFragment

    @ContributesAndroidInjector
    abstract fun contributeChildHistoryList(): CompletedHistoryList

    @ContributesAndroidInjector
    abstract fun contributeCancelledHistoryFragment(): CancelledHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeScheduledHistoryFragment(): ScheduledHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeSuggestionFragment(): SuggestionFragment

    @ContributesAndroidInjector
    abstract fun contributeFaqFragment(): FaqFragment

    @ContributesAndroidInjector
    abstract fun contributeWallet(): WalletFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationFragment(): NotificationFragment

    @ContributesAndroidInjector
    abstract fun contributeTripAddressChangeFrag(): TripAddressChangeFrag

    @ContributesAndroidInjector
    abstract fun contributeProAddressChange(): ProAddFavourites

    @ContributesAndroidInjector
    abstract fun contributeReferralFrag(): ReferralFrag


    @ContributesAndroidInjector
    abstract fun contributeAddressEditDialog(): AddressEditDialog

    @ContributesAndroidInjector
    abstract fun contributesAboutUsFrag(): AboutUsFrag

    @ContributesAndroidInjector
    abstract fun contributeOutstationFrag(): OutStationFrag

    @ContributesAndroidInjector
    abstract fun contributeOutstationSearchFrag(): OutStationSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeOutstationListFrag(): OutStationListFragment

    @ContributesAndroidInjector
    abstract fun contributeOutstationInvoice(): OutstationInvoice

    @ContributesAndroidInjector
    abstract fun contributesRideConfirmChangeAddress(): RideConfirmChangeAddress

    @ContributesAndroidInjector
    abstract fun contributesCropImageDialog(): CropImageDialog

    @ContributesAndroidInjector
    abstract fun contributeComplaintsListHistory(): ComplaintsListHistory

    @ContributesAndroidInjector
    abstract fun contributeComplaintsHistory(): ComplaintsHistory

    @ContributesAndroidInjector
    abstract fun contributeSuggestionHistory(): SuggestionHistory

    @ContributesAndroidInjector
    abstract fun contributeRentalFrag(): RentalFrag

    @ContributesAndroidInjector
    abstract fun contributeRentalInvoice(): RentalInvoiceFrag

    @ContributesAndroidInjector
    abstract fun contributeLanguage(): LanguageFrag


}