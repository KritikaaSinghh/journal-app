
package net.kritika.journalApp;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JournalAppApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Create an authenticated session for testing
    private MockHttpSession createSession(String username) {

        MockHttpSession session = new MockHttpSession();

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                )
        );

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return session;
    }

    @Test
    void contextLoads() {
    }

    // Unauthenticated users should not access journal APIs
    @Test
    void journalApiShouldRejectUnauthenticatedRequests() throws Exception {

        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isForbidden());
    }

    // An authenticated session should access journal APIs
    @Test
    void authenticatedSessionShouldAccessJournalApi() throws Exception {

        MockHttpSession session = createSession("testLoginUser");

        mockMvc.perform(
                get("/api/journal")
                        .session(session)
        ).andExpect(status().isOk());
    }

    // Logout should invalidate the session
    @Test
    void logoutShouldInvalidateSession() throws Exception {

        MockHttpSession session = createSession("testLogoutUser");

        mockMvc.perform(
                post("/api/auth/logout")
                        .session(session)
        ).andExpect(status().isOk());

        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isForbidden());
    }

    // Another user must not access a private journal entry
    @Test
    void anotherUserShouldNotAccessJournalEntry() throws Exception {

        String userA = "testUserA_" + UUID.randomUUID();
        String userB = "testUserB_" + UUID.randomUUID();

        MockHttpSession userASession = createSession(userA);
        MockHttpSession userBSession = createSession(userB);

        String entryId = null;

        try {

            String entryJson = """
                    {
                        "title": "Private Test Entry",
                        "content": "This entry belongs to User A"
                    }
                    """;

            // User A creates an entry
            MvcResult createResult = mockMvc.perform(
                            post("/api/journal")
                                    .session(userASession)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(entryJson)
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Map<String, Object> responseMap =
                    objectMapper.readValue(
                            createResult.getResponse().getContentAsString(),
                            Map.class
                    );

            entryId = (String) responseMap.get("id");

            assertNotNull(entryId, "Created entry must have an ID");

            String updatedJson = """
                    {
                        "title": "Hacked Entry",
                        "content": "User B is trying to update this entry"
                    }
                    """;

            // User B cannot read User A's entry
            mockMvc.perform(
                    get("/api/journal/" + entryId)
                            .session(userBSession)
            ).andExpect(status().isNotFound());

            // User B cannot update User A's entry
            mockMvc.perform(
                    put("/api/journal/" + entryId)
                            .session(userBSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedJson)
            ).andExpect(status().isNotFound());

            // User B cannot delete User A's entry
            mockMvc.perform(
                    delete("/api/journal/" + entryId)
                            .session(userBSession)
            ).andExpect(status().isNotFound());

            // User A should still be able to read the entry
            mockMvc.perform(
                    get("/api/journal/" + entryId)
                            .session(userASession)
            ).andExpect(status().isOk());

        } finally {

            // Clean up only the entry created by this test
            if (entryId != null) {
                mongoTemplate.remove(
                        Query.query(Criteria.where("_id").is(entryId)),
                        JournalEntry.class
                );
            }
        }
    }
}
