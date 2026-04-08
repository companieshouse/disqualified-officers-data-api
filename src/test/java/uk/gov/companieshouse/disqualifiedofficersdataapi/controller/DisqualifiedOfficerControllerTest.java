package uk.gov.companieshouse.disqualifiedofficersdataapi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.ExceptionHandlerConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.WebSecurityConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DeleteRequestParameters;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DeleteDisqualifiedOfficerService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DisqualifiedOfficerService;

@WebMvcTest(controllers = DisqualifiedOfficerController.class)
@ContextConfiguration(classes = {DisqualifiedOfficerController.class, ExceptionHandlerConfig.class})
@Import({WebSecurityConfig.class})
class DisqualifiedOfficerControllerTest {

    private static final String OFFICER_ID = "02588581";
    private static final String NATURAL = "natural";
    private static final String CORPORATE = "corporate";
    private static final String NATURAL_URL = String.format("/disqualified-officers/%s/%s/internal", NATURAL, OFFICER_ID);
    private static final String NATURAL_GET_URL = String.format("/disqualified-officers/%s/%s", NATURAL, OFFICER_ID);
    private static final String DELETE_NATURAL_URL = String.format("/disqualified-officers/%s/%s/internal", NATURAL, OFFICER_ID);
    private static final String DELETE_CORPORATE_URL = String.format("/disqualified-officers/%s/%s/internal", CORPORATE, OFFICER_ID);
    private static final String DELTA_AT = "20240925171003950844";
    private static final String STALE_DELTA_AT = "20220925171003950844";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisqualifiedOfficerService disqualifiedOfficerService;

    @MockitoBean
    private DeleteDisqualifiedOfficerService deleteService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Test
    @DisplayName("Disqualified Officer PUT request")
    void callDisqualifiedOfficerPutRequest() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with missing ERIC-Authorised-Key-Privileges")
    void callDisqualifiedOfficerPutRequestMissingAuthorisation() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with wrong privileges")
    void callDisqualifiedOfficerPutRequestWrongPrivileges() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .header("ERIC-Authorised-Key-Privileges", "privilege")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with oauth2 authorisation")
    void callDisqualifiedOfficerPutRequestWrongAuthorisation() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "oauth2")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with oauth2 authorisation and internal app privileges")
    void callDisqualifiedOfficerPutRequestWrongAuthorisationWithPrivileges() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "oauth2")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - NotFoundException 404 not found")
    void callDisqualifiedOfficerPutRequestNotFound() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(NotFoundException.class)
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - BadRequestException status code 400")
    void callDisqualifiedOfficerPutRequestBadRequest() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new BadRequestException("Bad request - data in wrong format"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - MethodNotAllowed status code 405")
    void callDisqualifiedOfficerPutRequestMethodNotAllowed() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new MethodNotAllowedException(
                String.format("Method Not Allowed - unsuccessful call to %s endpoint", NATURAL_URL)))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - InternalServerError status code 500")
    void callDisqualifiedOfficerPutRequestInternalServerError() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - ServiceUnavailable status code 503")
    void callDisqualifiedOfficerPutRequestServiceUnavailable() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - Forbidden status code 403")
    void callDisqualifiedOfficerPutRequestForbidden() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Disqualified Officer DELETE natural request")
    void callDisqualifiedOfficerDeleteNaturalRequest() throws Exception {
        DeleteRequestParameters expectedParams = DeleteRequestParameters.builder()
                .officerType("natural")
                .requestDeltaAt(DELTA_AT)
                .contextId("5342342")
                .officerId(OFFICER_ID)
                .build();

        mockMvc.perform(delete(DELETE_NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .header("x-delta-at", DELTA_AT))
                .andExpect(status().isOk());

        verify(deleteService).deleteDisqualification(expectedParams);
    }

    @Test
    @DisplayName("Disqualified Officer DELETE corporate request")
    void callDisqualifiedOfficerDeleteCorporateRequest() throws Exception {
        DeleteRequestParameters expectedParams = DeleteRequestParameters.builder()
                .officerType("corporate")
                .requestDeltaAt(DELTA_AT)
                .contextId("5342342")
                .officerId(OFFICER_ID)
                .build();

        mockMvc.perform(delete(DELETE_CORPORATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .header("x-delta-at", DELTA_AT))
                .andExpect(status().isOk());

        verify(deleteService).deleteDisqualification(expectedParams);
    }

    @Test
    @DisplayName("Disqualified Officer DELETE request - ServiceUnavailable status code 503")
    void callDisqualifiedOfficerDeleteRequestServiceUnavailable() throws Exception {
        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(deleteService).deleteDisqualification(any());

        mockMvc.perform(delete(DELETE_NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .header("x-delta-at", DELTA_AT))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Disqualified Officer DELETE request - Stale Delta At - Conflict status code 409")
    void callDisqualifiedOfficerDeleteRequestConflict() throws Exception {
        doThrow(new ConflictException("Stale delta at"))
                .when(deleteService).deleteDisqualification(any());

        mockMvc.perform(delete(DELETE_NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .header("x-delta-at", STALE_DELTA_AT))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Disqualified Officer DELETE request - No Delta At on Header - Bad request status code 400")
    void callDisqualifiedOfficerDeleteRequestBadRequest() throws Exception {
        doNothing()
                .when(deleteService).deleteDisqualification(any());

        mockMvc.perform(delete(DELETE_NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Disqualified Officer GET request")
    void callDisqualifiedOfficerGetRequest() throws Exception {
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        NaturalDisqualificationApi data = new NaturalDisqualificationApi();
        naturalDisqualification.setData(data);

        doReturn(naturalDisqualification)
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        MvcResult result = mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(data,
                objectMapper.readValue(result.getResponse().getContentAsString(), NaturalDisqualificationApi.class));
    }

    @Test
    @DisplayName("Disqualified Officer GET request with oauth2 success")
    void callDisqualifiedOfficerGetRequestOauth2() throws Exception {
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        NaturalDisqualificationApi data = new NaturalDisqualificationApi();
        naturalDisqualification.setData(data);

        doReturn(naturalDisqualification)
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "oauth2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DisqualifiedOfficer GET request - NotFoundException status code 404 resource not found")
    void callDisqualifiedOfficerGetRequestWhenDocumentNotFound() throws Exception {
        doThrow(new NotFoundException("Document not found"))
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Disqualified Officer OPTIONS request - CORS")
    void callDisqualifiedOfficerOptionsRequestCORS() throws Exception {
        mockMvc.perform(options(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("Origin", ""))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_MAX_AGE))
                .andReturn();
    }

    @Test
    @DisplayName("Disqualified Officer GET request - CORS")
    void callDisqualifiedOfficerGetRequestCORS() throws Exception {
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        NaturalDisqualificationApi data = new NaturalDisqualificationApi();
        naturalDisqualification.setData(data);

        doReturn(naturalDisqualification)
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        MvcResult result = mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andReturn();

        assertEquals(data,
                objectMapper.readValue(result.getResponse().getContentAsString(), NaturalDisqualificationApi.class));
    }

    @Test
    @DisplayName("Forbidden Disqualified Officer GET request - CORS")
    void getCompanyExemptionsForbiddenCORS() throws Exception {
        mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "")
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "oauth2"))
                .andExpect(status().isForbidden())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(content().string(""))
                .andReturn();
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - CORS")
    void callDisqualifiedOfficerPutRequestCORS() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(content().string(""))
                .andReturn();
    }
}