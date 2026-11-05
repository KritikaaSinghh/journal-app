
package net.kritika.journalApp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository
        extends MongoRepository<JournalEntry, String> {

    // Get all entries of a user
    List<JournalEntry> findAllByOwner(String owner);

    // Pagination
    Page<JournalEntry> findAllByOwner(String owner, Pageable pageable);

    // Find entry by ID and owner
    Optional<JournalEntry> findByIdAndOwner(String id, String owner);

    // Check entry ownership
    boolean existsByIdAndOwner(String id, String owner);

    // Search journal entries by title
    List<JournalEntry> findByOwnerAndTitleContainingIgnoreCase(
            String owner, String title
    );

    // Filter entries by date range
    List<JournalEntry> findAllByOwnerAndCreatedAtBetween(
            String owner,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Mood Analytics
    long countByOwnerAndMood(String owner, String mood);

    // Favorites / Bookmarks
    List<JournalEntry> findAllByOwnerAndFavoriteTrue(String owner);
}
