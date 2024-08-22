package it.pagopa.pn.cucumber.dto.pojo;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PnAttachment {
    String id;
    String documentId;
    String documentType;
    String uri;
    String sha256;
    OffsetDateTime date;
}
