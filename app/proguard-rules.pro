# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-injars      bin/classes
#-injars      bin/resources.ap_
#-injars      libs
#-outjars     bin/application.apk
#-libraryjars /usr/local/android-sdk/platforms/android-28/android.jar

-dontpreverify
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class * extends androidx.fragment.app.Fragment{}
-keep class com.firebase.** { *; }

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.cloneUser.client.connection.BaseResponse { *; }

-keep class com.cloneUser.client.connection.BaseResponse$DataObjectsAllApi { *; }
-keep class com.cloneUser.client.connection.BaseResponse$InvoiceQuestionsList { *; }
-keep class com.cloneUser.client.connection.BaseResponse$NewUser { *; }
-keep class com.cloneUser.client.connection.responseModels.AdminContact { *; }
-keep class com.cloneUser.client.connection.responseModels.AdminContact$Data { *; }
-keep class com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang { *; }
-keep class com.cloneUser.client.connection.responseModels.CancelReason { *; }
-keep class com.cloneUser.client.connection.responseModels.CancelReason$Reason { *; }
-keep class com.cloneUser.client.connection.responseModels.ComplaintsModel { *; }
-keep class com.cloneUser.client.connection.responseModels.OutstationModel { *; }
-keep class com.cloneUser.client.connection.responseModels.OutStationTypes { *; }
-keep class com.cloneUser.client.connection.responseModels.Country { *; }
-keep class com.cloneUser.client.connection.responseModels.FaqModel { *; }
-keep class com.cloneUser.client.connection.responseModels.HistoryModel { *; }
-keep class com.cloneUser.client.connection.responseModels.HistoryModel$History { *; }
-keep class com.cloneUser.client.connection.responseModels.Languages { *; }
-keep class com.cloneUser.client.connection.responseModels.NotificationsModel { *; }
-keep class com.cloneUser.client.connection.responseModels.NotificationData { *; }
-keep class com.cloneUser.client.connection.responseModels.PackageModel { *; }
-keep class com.cloneUser.client.connection.responseModels.PackageModel$Package { *; }
-keep class com.cloneUser.client.connection.responseModels.PromoCodeModel { *; }
-keep class com.cloneUser.client.connection.responseModels.PromoCodeModel$Promocode { *; }
-keep class com.cloneUser.client.connection.responseModels.ReferralModel { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestBill { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestBill$Bill { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestData { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestData$Data { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestData$Data$CarDetails { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestData$Data$Driver { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestInProgress { *; }
-keep class com.cloneUser.client.connection.responseModels.RequestInProgress$Trips { *; }
-keep class com.cloneUser.client.connection.responseModels.Route { *; }
-keep class com.cloneUser.client.connection.responseModels.S3Model { *; }
-keep class com.cloneUser.client.connection.responseModels.SosModel { *; }
-keep class com.cloneUser.client.connection.responseModels.SosModel$Sos { *; }
-keep class com.cloneUser.client.connection.responseModels.Step { *; }
-keep class com.cloneUser.client.connection.responseModels.StopModel { *; }
-keep class com.cloneUser.client.connection.responseModels.SuggestionModel { *; }
-keep class com.cloneUser.client.connection.responseModels.Translation { *; }
-keep class com.cloneUser.client.connection.responseModels.TypesModel { *; }
-keep class com.cloneUser.client.connection.responseModels.TypesModel$ZoneTypePrice { *; }
-keep class com.cloneUser.client.connection.responseModels.UserModel { *; }
-keep class com.cloneUser.client.connection.responseModels.UserModel$User { *; }
-keep class com.cloneUser.client.connection.responseModels.WalletResponsModel { *; }
-keep class com.cloneUser.client.ut.Utilz$ValidationError { *; }
-keep class com.cloneUser.client.ut.Utilz$CustomError { *; }
-keep class com.cloneUser.client.connection.TranslationModel { *; }
-keep class com.cloneUser.client.connection.FavPlace { *; }
-keep class com.cloneUser.client.connection.FavPlace$Favourite { *; }
-keep class com.cloneUser.client.connection.FavPlace$LastTripHistory { *; }
-keep class com.cloneUser.client.connection.responseModels.SuggestionHistoryModel { *; }
-keep class com.cloneUser.client.connection.responseModels.SuggestionHistoryModel$Suggestion { *; }
-keep class com.cloneUser.client.connection.responseModels.ComplaintHistoryModel { *; }
-keep class com.cloneUser.client.connection.responseModels.ComplaintHistoryModel$Complaints { *; }
-keep class com.cloneUser.client.connection.responseModels.RentalPackageTypes { *; }
-keep class com.cloneUser.client.connection.responseModels.RentalPackageTypes$Type { *; }
-keep class com.cloneUser.client.connection.responseModels.RentalPackageTypes$Type$Vehicle { *; }
#AWS
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class com.amazonaws.internal.**                                   { *; }
-keep class org.codehaus.**                             { *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class com.amazonaws.** { *; }

-keep class com.google.android.gms.** { *; }

-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.apache.http.annotation.**
-dontwarn org.ietf.jgss.**
-dontwarn org.joda.convert.**
-dontwarn org.w3c.dom.bootstrap.**

#SDK split into multiple jars so certain classes may be referenced but not used
-dontwarn com.amazonaws.services.s3.**
-dontwarn com.amazonaws.services.sqs.**

-dontnote com.amazonaws.services.sqs.QueueUrlHandler

