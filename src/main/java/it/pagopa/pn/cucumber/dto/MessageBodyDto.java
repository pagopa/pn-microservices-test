package it.pagopa.pn.cucumber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageBodyDto {

    String source;
    String detailType;
    String detail;

    @Override
    public String toString() {
        return "MessageBodyDto{" +
                "source='" + source + '\'' +
                ", detailType='" + detailType + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
