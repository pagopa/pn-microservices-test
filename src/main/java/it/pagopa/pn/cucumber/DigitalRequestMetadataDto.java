package it.pagopa.pn.cucumber;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;



@Setter
@Data
@NoArgsConstructor
public class DigitalRequestMetadataDto {

  @JsonProperty("correlationId")
  private String correlationId;

  @JsonProperty("eventType")
  private String eventType;

  @JsonProperty("tags")
  @Valid
  private Map<String, String> tags = null;


  public enum ChannelEnum {
    SMS("SMS"),
    
    EMAIL("EMAIL"),
    
    PEC("PEC");

    private String value;

    ChannelEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ChannelEnum fromValue(String value) {
      for (ChannelEnum b : ChannelEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("channel")
  private ChannelEnum channel;


  public enum MessageContentTypeEnum {
    PLAIN("text/plain"),
    
    HTML("text/html");

    private String value;

    MessageContentTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static MessageContentTypeEnum fromValue(String value) {
      for (MessageContentTypeEnum b : MessageContentTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("messageContentType")
  private MessageContentTypeEnum messageContentType;

  public DigitalRequestMetadataDto correlationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public DigitalRequestMetadataDto eventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  @NotNull 
  public String getEventType() {
    return eventType;
  }

  public DigitalRequestMetadataDto tags(Map<String, String> tags) {
    this.tags = tags;
    return this;
  }

  public DigitalRequestMetadataDto putTagsItem(String key, String tagsItem) {
    if (this.tags == null) {
      this.tags = new HashMap<>();
    }
    this.tags.put(key, tagsItem);
    return this;
  }

  
  public Map<String, String> getTags() {
    return tags;
  }

  public DigitalRequestMetadataDto channel(ChannelEnum channel) {
    this.channel = channel;
    return this;
  }


  @NotNull 
  public ChannelEnum getChannel() {
    return channel;
  }

  public DigitalRequestMetadataDto messageContentType(MessageContentTypeEnum messageContentType) {
    this.messageContentType = messageContentType;
    return this;
  }


  @NotNull 
  public MessageContentTypeEnum getMessageContentType() {
    return messageContentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DigitalRequestMetadataDto digitalRequestMetadataDto = (DigitalRequestMetadataDto) o;
    return Objects.equals(this.correlationId, digitalRequestMetadataDto.correlationId) &&
        Objects.equals(this.eventType, digitalRequestMetadataDto.eventType) &&
        Objects.equals(this.tags, digitalRequestMetadataDto.tags) &&
        Objects.equals(this.channel, digitalRequestMetadataDto.channel) &&
        Objects.equals(this.messageContentType, digitalRequestMetadataDto.messageContentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(correlationId, eventType, tags, channel, messageContentType);
  }
}
