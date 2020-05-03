package ie.wit.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class WeightMatesModel(
    var uid: String? = "",
    var calorietype: String = "N/A",
    var amount: Int = 0,
    var profilepic: String = "",
    var email: String? = "joe@bloggs.com")
    : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "calorietype" to calorietype,
            "amount" to amount,
            "profilepic" to profilepic,
            "email" to email
        )
    }
}



