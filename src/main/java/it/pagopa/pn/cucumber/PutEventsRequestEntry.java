package it.pagopa.pn.cucumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class PutEventsRequestEntry {

    Instant time;

    String source;

    List<String> resources;

    @JsonProperty("detail-type")
    String detailType;

    NotificationMessage detail;

    String eventBusName;

    String traceHeader;

    String version;

    String id;

    String account;

    String region;


    @Override
    public String toString() {
        return "PutEventsRequestEntry{" +
                "time=" + time +
                ", source='" + source + '\'' +
                ", resources=" + resources +
                ", detailType='" + detailType + '\'' +
                ", detail='" + detail + '\'' +
                ", eventBusName='" + eventBusName + '\'' +
                ", traceHeader='" + traceHeader + '\'' +
                ", version='" + version + '\'' +
                ", id='" + id + '\'' +
                ", account='" + account + '\'' +
                ", region='" + region + '\'' +
                '}';
    }

    public NotificationMessage detail() {
        return detail;
    }

    public String traceHeader() {
        return traceHeader;
    }

    public Object detailType() {
        return detailType;
    }

    public String eventBusName() {
        return eventBusName;
    }

    public Object source() {
        return source;
    }

    public String version(){
        return version;
    }

    public String id(){
        return id;
    }
    public String region() {
        return region;
    }

    public String account(){
        return account;
    }
}
