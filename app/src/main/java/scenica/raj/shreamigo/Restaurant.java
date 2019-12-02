package scenica.raj.shreamigo;

/**
 * Created by DELL on 12/3/2016.
 */

public class Restaurant {

    private String results_found;
    private String id;
    private String name;
    private String resUrl;

    private String address;

    private String addressLocality;
    private String addressCity;
    private String averageCostForTwo;
    private String priceRange;
    private String currency;
    private String image_url;
    private String rating;
    private String rating_color;
    private String ratingText;
    private String cuisines;
    private String phoneNumber;
    private String latitude;
    private String longitude;
    private String photos_url;
    private String votes;


    public Restaurant(String results_found, String id, String name, String resUrl, String address, String addressLocality, String addressCity, String averageCostForTwo, String priceRange, String currency, String image_url, String rating, String rating_color, String ratingText, String cuisines, String phoneNumber, String latitude, String longitude, String photos_url, String votes) {
        final String HEX = "#";
        String BRACKET = "(";
        this.results_found = results_found;
        this.id = id;
        this.name = name;
        this.resUrl = resUrl;
        this.address = address;
        this.addressLocality = addressLocality;
        this.addressCity = addressCity;
        this.averageCostForTwo = averageCostForTwo.concat(" for two (approx.)");
        this.priceRange = priceRange;
        this.currency = currency;
        this.image_url = image_url;
        this.rating = rating;
        this.rating_color = HEX.concat(rating_color);
        this.ratingText = ratingText;
        this.cuisines = cuisines;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photos_url = photos_url;
        this.votes = BRACKET.concat(votes).concat(")");
    }

    public Restaurant() {
    }

    public String getVotes() {
        return votes;
    }

    public Restaurant setVotes(String votes) {
        this.votes = votes;
        return this;
    }

    public String getPhotos_url() {
        return photos_url;
    }

    public Restaurant setPhotos_url(String photos_url) {
        this.photos_url = photos_url;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getResUrl() {
        return resUrl;
    }

    public String getAddress() {
        return address;
    }

    public Restaurant setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getAddressLocality() {
        return addressLocality;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public String getAverageCostForTwo() {
        return averageCostForTwo;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public String getCurrency() {
        return currency;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getRating() {
        return rating;
    }

    public String getRating_color() {
        return rating_color;
    }

    public String getRatingText() {
        return ratingText;
    }

    public String getCuisines() {
        return cuisines;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getResults_found() {
        return results_found;
    }


}

