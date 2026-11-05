
package net.kritika.journalApp;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    // Create a new journal entry
    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(
            @Valid @RequestBody JournalEntry entry,
            Authentication authentication) {

        JournalEntry savedEntry =
                journalEntryService.saveEntry(entry, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
    }

    // Get all journal entries of logged-in user
    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllEntries(
            Authentication authentication) {

        return ResponseEntity.ok(
                journalEntryService.getAllEntries(authentication.getName())
        );
    }

    // Get a specific journal entry
    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getEntryById(
            @PathVariable String id,
            Authentication authentication) {

        return journalEntryService.getEntryById(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get paginated journal entries
    @GetMapping("/page")
    public ResponseEntity<?> getEntriesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Authentication authentication) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body("Page must be >= 0 and size must be between 1 and 100");
        }

        return ResponseEntity.ok(
                journalEntryService.getEntriesWithPagination(
                        authentication.getName(),
                        page,
                        size
                )
        );
    }

    // Search journal entries by title
    @GetMapping("/search")
    public ResponseEntity<List<JournalEntry>> searchByTitle(
            @RequestParam String title,
            Authentication authentication) {

        List<JournalEntry> entries =
                journalEntryService.searchByTitle(
                        authentication.getName(),
                        title
                );

        return ResponseEntity.ok(entries);
    }

    // Filter journal entries by date range
    @GetMapping("/filter")
    public ResponseEntity<?> filterByDate(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {

        try {

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (end.isBefore(start)) {
                return ResponseEntity.badRequest()
                        .body("End date cannot be before start date.");
            }

            LocalDateTime startDateTime = start.atStartOfDay();

            LocalDateTime endDateTime = end.plusDays(1).atStartOfDay()
                    .minusNanos(1);

            List<JournalEntry> entries =
                    journalEntryService.getEntriesByDateRange(
                            authentication.getName(),
                            startDateTime,
                            endDateTime
                    );

            return ResponseEntity.ok(entries);

        } catch (java.time.format.DateTimeParseException exception) {

            return ResponseEntity.badRequest()
                    .body("Invalid date format. Use yyyy-MM-dd.");
        }
    }

    // MOOD ANALYTICS API
    @GetMapping("/mood-stats")
    public ResponseEntity<Map<String, Object>> getMoodStats(
            Authentication authentication) {

        String owner = authentication.getName();

        Map<String, Object> moodStats = new LinkedHashMap<>();

        moodStats.put("Happy",
                journalEntryService.getMoodCount(owner, "Happy"));

        moodStats.put("Sad",
                journalEntryService.getMoodCount(owner, "Sad"));

        moodStats.put("Excited",
                journalEntryService.getMoodCount(owner, "Excited"));

        moodStats.put("Stressed",
                journalEntryService.getMoodCount(owner, "Stressed"));

        moodStats.put("Calm",
                journalEntryService.getMoodCount(owner, "Calm"));

        moodStats.put("Grateful",
                journalEntryService.getMoodCount(owner, "Grateful"));

        return ResponseEntity.ok(moodStats);
    }
    // Get all favorite journal entries
    @GetMapping("/favorites")
    public ResponseEntity<List<JournalEntry>> getFavoriteEntries(
            Authentication authentication) {

        return ResponseEntity.ok(
                journalEntryService.getFavoriteEntries(authentication.getName())
        );
    }

    // Add or remove an entry from favorites
    @PutMapping("/{id}/favorite")
    public ResponseEntity<JournalEntry> toggleFavorite(
            @PathVariable String id,
            Authentication authentication) {

        return journalEntryService.toggleFavorite(
                        id,
                        authentication.getName()
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a journal entry
    @PutMapping("/{id}")
    public ResponseEntity<JournalEntry> updateEntry(
            @PathVariable String id,
            @Valid @RequestBody JournalEntry updatedEntry,
            Authentication authentication) {

        return journalEntryService.getEntryById(id, authentication.getName())
                .map(existingEntry -> {

                    existingEntry.setTitle(updatedEntry.getTitle());
                    existingEntry.setContent(updatedEntry.getContent());
                    existingEntry.setCategory(updatedEntry.getCategory());
                    existingEntry.setTags(updatedEntry.getTags());
                    existingEntry.setMood(updatedEntry.getMood());

                    JournalEntry savedEntry =
                            journalEntryService.saveEntry(
                                    existingEntry,
                                    authentication.getName()
                            );

                    return ResponseEntity.ok(savedEntry);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a journal entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable String id,
            Authentication authentication) {

        boolean deleted = journalEntryService.deleteEntry(
                id,
                authentication.getName()
        );

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
