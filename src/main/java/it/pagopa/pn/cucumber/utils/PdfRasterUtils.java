package it.pagopa.pn.cucumber.utils;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_PDFRASTER;


import java.io.File;

import static it.pagopa.pn.cucumber.utils.RequestEndpoint.PDF_RASTER_CONVERT_ENDPOINT;


@Slf4j
public class PdfRasterUtils {
	private PdfRasterUtils() {
		throw new IllegalStateException("PdfRasterUtils is a utility class");
	}

    public static Response getCurrentClientConfig(String endpoint, File file, String partName, String contentType) {
		return CommonUtils.sendMultipartRequest(endpoint, file, partName, contentType, PN_PDFRASTER);
    }
}
