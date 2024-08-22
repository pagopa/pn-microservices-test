package it.pagopa.pn.cucumber.utils;

public class RequestEndpoint {
    protected static final String SMS_ENDPOINT =
            "/external-channels/v1/digital-deliveries/courtesy-simple-message-requests/{requestIdx}";
    protected static final String EMAIL_ENDPOINT =
            "/external-channels/v1/digital-deliveries/courtesy-full-message-requests/{requestIdx}";
    protected static final String PEC_ENDPOINT =
            "/external-channels/v1/digital-deliveries/legal-full-message-requests/{requestIdx}";
    protected static final String CARTACEO_ENDPOINT =
            "/external-channels/v1/paper-deliveries-engagements/{requestIdx}";
    protected static final String CONSOLIDATORE_ENDPOINT =
            "/consolidatore-ingress/v1/push-progress-events";
    protected static final String GET_CONFIGURATIONS_ENDPOINT =
            "/external-channels/v1/configurations";
    protected static final String GET_CLIENT_ENDPOINT =
            "/external-channel/gestoreRepository/clients/{x-pagopa-extch-cx-id}";
    protected static final String GET_REQUEST_ENDPOINT =
            "/external-channel/gestoreRepository/requests/{requestIdx}";
    protected static final String GET_REQUEST_MESSAGE_ID_ENDPOINT =
            "/external-channel/gestoreRepository/requests/messageId/{messageId}";
    protected static final String GET_ATTACHMENT =
            "/consolidatore-ingress/v1/get-attachment/{fileKey}";
}
