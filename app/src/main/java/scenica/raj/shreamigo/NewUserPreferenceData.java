package scenica.raj.shreamigo;

import java.util.ArrayList;

/**
 * Created by DELL on 1/12/2017.
 */

public class NewUserPreferenceData {

    ArrayList<String> selectedCuisineName;

    public NewUserPreferenceData(ArrayList<String> selectedCuisineName) {
        this.selectedCuisineName = selectedCuisineName;
    }

    public NewUserPreferenceData() {
    }

    public ArrayList<String> getSelectedCuisineName() {
        return selectedCuisineName;
    }

    public NewUserPreferenceData setSelectedCuisineName(ArrayList<String> selectedCuisineName) {
        this.selectedCuisineName = selectedCuisineName;
        return this;
    }
}
