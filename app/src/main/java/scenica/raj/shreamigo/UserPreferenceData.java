package scenica.raj.shreamigo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DELL on 12/14/2016.
 */

public class UserPreferenceData implements Parcelable {

    public static final Creator<UserPreferenceData> CREATOR = new Creator<UserPreferenceData>() {
        @Override
        public UserPreferenceData createFromParcel(Parcel in) {
            return new UserPreferenceData(in);
        }

        @Override
        public UserPreferenceData[] newArray(int size) {
            return new UserPreferenceData[size];
        }
    };
    private String selectedCuisineName;
    private String selectedCuisineID;

    public UserPreferenceData() {
    }

    protected UserPreferenceData(Parcel in) {
        selectedCuisineName = in.readString();
        selectedCuisineID = in.readString();
    }

    public UserPreferenceData(String selectedCuisineName, String selectedCuisineID) {

        this.selectedCuisineName = selectedCuisineName;
        this.selectedCuisineID = selectedCuisineID;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selectedCuisineName);
        dest.writeString(selectedCuisineID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getSelectedCuisineName() {
        return selectedCuisineName;
    }

    public UserPreferenceData setSelectedCuisineName(String selectedCuisineName) {
        this.selectedCuisineName = selectedCuisineName;
        return this;
    }

    public String getSelectedCuisineID() {
        return selectedCuisineID;
    }

    public UserPreferenceData setSelectedCuisineID(String selectedCuisineID) {
        this.selectedCuisineID = selectedCuisineID;
        return this;
    }
}
