package scenica.raj.shreamigo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DELL on 1/14/2017.
 */

public class GeofenceDetails implements Parcelable {
    public static final Parcelable.Creator<GeofenceDetails> CREATOR = new Creator<GeofenceDetails>() {
        @Override
        public GeofenceDetails createFromParcel(Parcel source) {
            return new GeofenceDetails(source);
        }

        @Override
        public GeofenceDetails[] newArray(int size) {
            return new GeofenceDetails[size];
        }
    };
    String uniqueID;
    Double latitude;
    Double longitude;

    public GeofenceDetails(Double latitude, Double longitude, String uniqueID) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.uniqueID = uniqueID;
    }

    public GeofenceDetails() {

    }

    public GeofenceDetails(Parcel in) {

        uniqueID = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public Double getLatitude() {
        return latitude;
    }

    public GeofenceDetails setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public GeofenceDetails setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public GeofenceDetails setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(uniqueID);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
