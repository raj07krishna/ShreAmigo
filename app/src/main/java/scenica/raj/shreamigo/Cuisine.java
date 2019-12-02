package scenica.raj.shreamigo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DELL on 12/13/2016.
 */

public class Cuisine implements Parcelable {

    public static final Creator<Cuisine> CREATOR = new Creator<Cuisine>() {
        @Override
        public Cuisine createFromParcel(Parcel in) {
            return new Cuisine(in);
        }

        @Override
        public Cuisine[] newArray(int size) {
            return new Cuisine[size];
        }
    };
    String cuisineName;
    String cuisineID;
    Boolean selected;
    int result;

    public Cuisine(String cuisineName, String cuisineID, int result) {
        this.cuisineName = cuisineName;
        this.cuisineID = cuisineID;
        this.result = result;
    }

    public Cuisine(String cuisineName, String cuisineID) {
        this.cuisineName = cuisineName;
        this.cuisineID = cuisineID;
        this.selected = false;
    }

    protected Cuisine(Parcel in) {
        cuisineName = in.readString();
        cuisineID = in.readString();
        result = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cuisineName);
        dest.writeString(cuisineID);
        dest.writeInt(result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getCuisineName() {
        return cuisineName;
    }

    public String getCuisineID() {
        return cuisineID;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getResult() {
        return result;
    }

    public Cuisine setResult(int result) {
        this.result = result;
        return this;
    }
}
