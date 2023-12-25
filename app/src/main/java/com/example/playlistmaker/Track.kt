import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Int,
    val artworkUrl100: String?,
    val trackId: Int,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    companion object : Parceler<Track> {

        override fun Track.write(parcel: Parcel, flags: Int) {
            parcel.writeString(trackName)
            parcel.writeString(artistName)
            parcel.writeInt(trackTimeMillis)
            parcel.writeString(artworkUrl100)
            parcel.writeInt(trackId)
            parcel.writeString(collectionName)
            parcel.writeString(releaseDate)
            parcel.writeString(primaryGenreName)
            parcel.writeString(country)
        }

        override fun create(parcel: Parcel): Track {
            return Track(parcel)
        }
    }

}