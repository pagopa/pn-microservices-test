package it.pagopa.pn.cucumber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.pn.ec.rest.v1.api.SenderPhysicalAddressDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Setter
@Data
@NoArgsConstructor
public class ClientConfigurationInternalDto {

    private String xPagopaExtchCxId;

    @JsonProperty("sqsArn")
    private String sqsArn;

    @JsonProperty("sqsName")
    private String sqsName;

    @JsonProperty("pecReplyTo")
    private String pecReplyTo;

    @JsonProperty("mailReplyTo")
    private String mailReplyTo;

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("senderPhysicalAddress")
    private SenderPhysicalAddressDto senderPhysicalAddress;

    public ClientConfigurationInternalDto xPagopaExtchCxId(String xPagopaExtchCxId) {
        this.xPagopaExtchCxId = xPagopaExtchCxId;
        return this;
    }

    @NotNull
    @Pattern(regexp = "^(?!\\s*$).+") @Size(min = 10, max = 50)
    public String getxPagopaExtchCxId() {
        return xPagopaExtchCxId;
    }

}
