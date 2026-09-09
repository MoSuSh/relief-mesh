import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationRequest {
    @JsonProperty("resource_id")
    public int resource_id;

    @JsonProperty("user_id")
    public int user_id;

    @JsonProperty("quantity")
    public int quantity;

    @JsonProperty("version")
    public int version;

    // Default constructor required by Jackson for deserialization
    public ReservationRequest() {}
}