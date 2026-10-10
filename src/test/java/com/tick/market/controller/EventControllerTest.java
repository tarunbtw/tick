package com.tick.market.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick.market.dto.EventIngestRequest;
import com.tick.market.dto.EventResponse;
import com.tick.market.dto.IngestResponse;
import com.tick.market.dto.PagedResponse;
import com.tick.market.entity.EventType;
import com.tick.market.exception.DuplicateEventException;
import com.tick.market.exception.EventNotFoundException;
import com.tick.market.exception.GlobalExceptionHandler;
import com.tick.market.service.EventIngestionService;
import com.tick.market.service.EventQueryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventIngestionService eventIngestionService;

    @MockBean
    private EventQueryService eventQueryService;

    @Test
    @DisplayName("POST /api/v1/events - Should return 201 Created on valid request")
    void shouldReturn201CreatedOnValidRequest() throws Exception {
        EventIngestRequest request = new EventIngestRequest(
            "evt-web-01",
            "GOOGL",
            EventType.TRADE,
            new BigDecimal("175.25"),
            new BigDecimal("200"),
            Instant.parse("2026-10-10T12:00:00Z")
        );

        when(eventIngestionService.ingestEvent(any(EventIngestRequest.class)))
            .thenReturn(IngestResponse.accepted("evt-web-01", Instant.now()));

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACCEPTED"))
            .andExpect(jsonPath("$.eventId").value("evt-web-01"));
    }

    @Test
    @DisplayName("POST /api/v1/events - Should return 400 Bad Request on validation error")
    void shouldReturn400BadRequestOnValidationError() throws Exception {
        // Missing eventId, negative price
        EventIngestRequest invalidRequest = new EventIngestRequest(
            "",
            "GOOGL",
            EventType.TRADE,
            new BigDecimal("-5.00"),
            new BigDecimal("100"),
            Instant.now()
        );

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/events - Should return 409 Conflict on duplicate event")
    void shouldReturn409ConflictOnDuplicate() throws Exception {
        EventIngestRequest request = new EventIngestRequest(
            "evt-dup-web",
            "GOOGL",
            EventType.TRADE,
            new BigDecimal("175.25"),
            new BigDecimal("200"),
            Instant.parse("2026-10-10T12:00:00Z")
        );

        when(eventIngestionService.ingestEvent(any(EventIngestRequest.class)))
            .thenThrow(new DuplicateEventException("evt-dup-web"));

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("evt-dup-web")));
    }

    @Test
    @DisplayName("GET /api/v1/events - Should return 200 OK with PagedResponse")
    void shouldReturn200OkWithPagedResponse() throws Exception {
        EventResponse item = new EventResponse(
            1L,
            "evt-item",
            "GOOGL",
            EventType.TRADE,
            new BigDecimal("175.25"),
            new BigDecimal("200"),
            Instant.now(),
            Instant.now()
        );
        PagedResponse<EventResponse> pagedResponse = new PagedResponse<>(
            List.of(item),
            0,
            20,
            1,
            1,
            true,
            true
        );

        when(eventQueryService.queryEvents(eq("GOOGL"), eq(EventType.TRADE), any(), any(), eq(0), eq(20)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/events")
                .param("symbol", "GOOGL")
                .param("eventType", "TRADE")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].eventId").value("evt-item"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/events/{eventId} - Should return 200 OK when found")
    void shouldReturn200OkWhenFound() throws Exception {
        EventResponse item = new EventResponse(
            1L,
            "evt-single",
            "AAPL",
            EventType.TRADE,
            new BigDecimal("220.00"),
            new BigDecimal("50"),
            Instant.now(),
            Instant.now()
        );

        when(eventQueryService.getEventById("evt-single")).thenReturn(item);

        mockMvc.perform(get("/api/v1/events/evt-single"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventId").value("evt-single"))
            .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    @DisplayName("GET /api/v1/events/{eventId} - Should return 404 Not Found when missing")
    void shouldReturn404NotFoundWhenMissing() throws Exception {
        when(eventQueryService.getEventById("nonexistent-evt"))
            .thenThrow(new EventNotFoundException("nonexistent-evt"));

        mockMvc.perform(get("/api/v1/events/nonexistent-evt"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
