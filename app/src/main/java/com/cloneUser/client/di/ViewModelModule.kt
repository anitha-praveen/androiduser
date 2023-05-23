package com.cloneUser.client.di

import androidx.lifecycle.ViewModel
import com.cloneUser.client.SecondFragmeVM
import com.cloneUser.client.dialogs.addressEdit.AddressEditVM
import com.cloneUser.client.dialogs.cancelReasons.CancelReasonsVM
import com.cloneUser.client.dialogs.countrylist.CountryListVM
import com.cloneUser.client.dialogs.cropImageDialog.CropImageVm
import com.cloneUser.client.dialogs.defaultDialog.DefaultDialogVM
import com.cloneUser.client.dialogs.disclosure.DisclosureVM
import com.cloneUser.client.dialogs.tripCancelled.TripCancelledVM
import com.cloneUser.client.dialogs.waitingProgress.WaitingProgressVM
import com.cloneUser.client.drawer.DrawerVM
import com.cloneUser.client.drawer.aboutUs.AboutUsVm
import com.cloneUser.client.drawer.applyPromo.ApplyPromoVM
import com.cloneUser.client.drawer.complaint.addComplaints.ComplaintVM
import com.cloneUser.client.drawer.complaint.history.common.ComplaintsListHistoryVM
import com.cloneUser.client.drawer.complaint.history.complaintsHistory.ComplaintsHistoryVM
import com.cloneUser.client.drawer.complaint.history.suggestionHistory.SuggestionHistoryVM
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationVM
import com.cloneUser.client.drawer.faq.FaqVm
import com.cloneUser.client.drawer.favorites.addFavorite.AddFavoriteVM
import com.cloneUser.client.drawer.favorites.listFavorite.FavoriteVM
import com.cloneUser.client.drawer.favorites.pickFromMap.PickFromMapVM
import com.cloneUser.client.drawer.invoice.InvoiceVM
import com.cloneUser.client.drawer.language.LanguageVM
import com.cloneUser.client.drawer.mapfragment.MapFragmentVM
import com.cloneUser.client.drawer.myRides.HistoryListVM
import com.cloneUser.client.drawer.myRides.cancelled.CancelledHistoryVM
import com.cloneUser.client.drawer.myRides.completed.CompletedHistoryListVM
import com.cloneUser.client.drawer.myRides.scheduled.ScheduledHistoryVM
import com.cloneUser.client.drawer.notificationList.NotificationVM
import com.cloneUser.client.drawer.outStation.OutStationVM
import com.cloneUser.client.drawer.outStation.listOutStation.OutStationListVm
import com.cloneUser.client.drawer.outStation.outstationInvoice.OutstationInvoiceVm
import com.cloneUser.client.drawer.outStation.outstationSearchPlace.OutStationSearchVm
import com.cloneUser.client.drawer.profile.ProfileVM
import com.cloneUser.client.drawer.profile.proAddFvourites.ProAddFavouritesVm
import com.cloneUser.client.drawer.rating.RatingVM
import com.cloneUser.client.drawer.referral.ReferralVM
import com.cloneUser.client.drawer.rental.RentalVM
import com.cloneUser.client.drawer.rental.rentalInvoice.RentalnvoiceVm
import com.cloneUser.client.drawer.rideConfirm.RideConfirmVM
import com.cloneUser.client.drawer.rideConfirm.changeAddress.RideConfirmChangeAddressVm
import com.cloneUser.client.drawer.sos.SosVM
import com.cloneUser.client.drawer.suggestion.SuggestionVm
import com.cloneUser.client.drawer.support.SupportVM
import com.cloneUser.client.drawer.trip.TripVM
import com.cloneUser.client.drawer.tripAddressChange.TripAddressChangeVM
import com.cloneUser.client.drawer.wallet.WalletVm
import com.cloneUser.client.loginOrSignup.LoginOrSignupVM
import com.cloneUser.client.loginOrSignup.getStarteedScrn.GetStartedScreenVM
import com.cloneUser.client.loginOrSignup.login.LoginVM
import com.cloneUser.client.loginOrSignup.otp.OtpVM
import com.cloneUser.client.loginOrSignup.register.RegisterVM
import com.cloneUser.client.loginOrSignup.tour.TourGuideVM
import com.cloneUser.client.splash.SplashVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SplashVM::class)
    internal abstract fun bindSplashVM(viewModel: SplashVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginOrSignupVM::class)
    internal abstract fun bindSSignupVM(viewModel: LoginOrSignupVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapFragmentVM::class)
    internal abstract fun bindMyViewModel(viewModel: MapFragmentVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SecondFragmeVM::class)
    internal abstract fun bindSecondFragmeVM(viewModel: SecondFragmeVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DrawerVM::class)
    internal abstract fun bindDrawerVM(viewModel: DrawerVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetStartedScreenVM::class)
    internal abstract fun bindGetStartedScreenVM(viewModel: GetStartedScreenVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TourGuideVM::class)
    internal abstract fun bindTourGuideVM(viewModel: TourGuideVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginVM::class)
    internal abstract fun bindLoginVM(viewModel: LoginVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CountryListVM::class)
    internal abstract fun bindCountryListVM(viewModel: CountryListVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OtpVM::class)
    internal abstract fun bindOtpVM(viewModel: OtpVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegisterVM::class)
    internal abstract fun bindRegisterVM(viewModel: RegisterVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileVM::class)
    internal abstract fun bindProfileVM(viewModel: ProfileVM): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(HistoryListVM::class)
    internal abstract fun bindHistoryListVM(viewModel: HistoryListVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ComplaintVM::class)
    internal abstract fun bindComplaintVM(viewModel: ComplaintVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DisclosureVM::class)
    internal abstract fun bindDisclosureVM(viewModel: DisclosureVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteVM::class)
    internal abstract fun bindFavoriteVM(viewModel: FavoriteVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddFavoriteVM::class)
    internal abstract fun bindAddFavoriteVM(viewModel: AddFavoriteVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PickFromMapVM::class)
    internal abstract fun bindPickFromMapVM(viewModel: PickFromMapVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmDestinationVM::class)
    internal abstract fun bindConfirmDestinationVM(viewModel: ConfirmDestinationVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RideConfirmVM::class)
    internal abstract fun bindRideConfirmVM(viewModel: RideConfirmVM): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ApplyPromoVM::class)
    internal abstract fun bindApplyPromoVM(viewModel: ApplyPromoVM): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(SupportVM::class)
    internal abstract fun bindSupportVM(viewModel: SupportVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SosVM::class)
    internal abstract fun bindSosVM(viewModel: SosVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WaitingProgressVM::class)
    internal abstract fun bindWaitingProgressVM(viewModel: WaitingProgressVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DefaultDialogVM::class)
    internal abstract fun bindDefaultDialogVM(viewModel: DefaultDialogVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TripVM::class)
    internal abstract fun bindTripVM(viewModel: TripVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancelReasonsVM::class)
    internal abstract fun bindCancelReasonsVM(viewModel: CancelReasonsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TripCancelledVM::class)
    internal abstract fun bindTripCancelledVM(viewModel: TripCancelledVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InvoiceVM::class)
    internal abstract fun bindInvoiceVM(viewModel: InvoiceVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RatingVM::class)
    internal abstract fun bindRatingVM(viewModel: RatingVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CompletedHistoryListVM::class)
    internal abstract fun bindChildHistoryListVM(viewModel: CompletedHistoryListVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancelledHistoryVM::class)
    internal abstract fun bindCancelledHistoryVM(viewModel: CancelledHistoryVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduledHistoryVM::class)
    internal abstract fun bindScheduledHistoryVM(viewModel: ScheduledHistoryVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SuggestionVm::class)
    internal abstract fun bindSuggestionVM(viewModel: SuggestionVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FaqVm::class)
    internal abstract fun bindFaqVM(viewModel: FaqVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WalletVm::class)
    internal abstract fun bindWallet(viewModel: WalletVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationVM::class)
    internal abstract fun bindNotificationVM(viewModel: NotificationVM): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(TripAddressChangeVM::class)
    internal abstract fun bindTripAddressChangeVM(viewModel: TripAddressChangeVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProAddFavouritesVm::class)
    internal abstract fun bindProAddFavouritesVm(viewModel: ProAddFavouritesVm): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ReferralVM::class)
    internal abstract fun bindReferralVM(viewModel: ReferralVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddressEditVM::class)
    internal abstract fun bindAddressEditVM(viewModel: AddressEditVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AboutUsVm::class)
    internal abstract fun bindAboutUsVm(viewModel: AboutUsVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RideConfirmChangeAddressVm::class)
    internal abstract fun bindRideConfirmChangeAddressVm(viewModel: RideConfirmChangeAddressVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CropImageVm::class)
    internal abstract fun bindCropImageVm(viewModel: CropImageVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ComplaintsListHistoryVM::class)
    internal abstract fun bindComplaintsListHistoryVM(viewModel: ComplaintsListHistoryVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ComplaintsHistoryVM::class)
    internal abstract fun bindComplaintsHistoryVM(viewModel: ComplaintsHistoryVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SuggestionHistoryVM::class)
    internal abstract fun bindSuggestionHistoryVM(viewModel: SuggestionHistoryVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RentalVM::class)
    internal abstract fun bindRentalVM(viewModel: RentalVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RentalnvoiceVm::class)
    internal abstract fun bindRentalnvoiceVm(viewModel: RentalnvoiceVm): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(OutStationVM::class)
    internal abstract fun bindOutStationVM(viewModel: OutStationVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OutStationSearchVm::class)
    internal abstract fun bindOutStationSearchVm(viewModel: OutStationSearchVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OutStationListVm::class)
    internal abstract fun bindOutStationListVm(viewModel: OutStationListVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OutstationInvoiceVm::class)
    internal abstract fun bindOutstationInvoiceVm(viewModel: OutstationInvoiceVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LanguageVM::class)
    internal abstract fun bindOutLanguageVm(viewModel: LanguageVM): ViewModel
}