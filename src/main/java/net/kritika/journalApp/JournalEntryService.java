
package net.kritika.journalApp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    private final JournalEntryRepository repository;

    public JournalEntryService(JournalEntryRepository repository) {
        this.repository = repository;
    }

    // Save journal entry
    public JournalEntry saveEntry(JournalEntry entry, String owner) {
        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(LocalDateTime.now());
        }

        entry.setOwner(owner);
        return repository.save(entry);
    }

    // Get all entries of a user
    public List<JournalEntry> getAllEntries(String owner) {
        return repository.findAllByOwner(owner);
    }

    // Get paginated entries of a user
    public Page<JournalEntry> getEntriesWithPagination(
            String owner, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repository.findAllByOwner(owner, pageable);
    }

    // Get entry by ID and owner
    public Optional<JournalEntry> getEntryById(String id, String owner) {
        return repository.findByIdAndOwner(id, owner);
    }

    // Delete entry
    public boolean deleteEntry(String id, String owner) {
        Optional<JournalEntry> entry =
                repository.findByIdAndOwner(id, owner);

        if (entry.isEmpty()) {
            return false;
        }

        repository.delete(entry.get());
        return true;
    }

    // Search entries by title
    public List<JournalEntry> searchByTitle(String owner, String title) {
        return repository.findByOwnerAndTitleContainingIgnoreCase(
                owner, title
        );
    }

    // Filter entries by date range
    public List<JournalEntry> getEntriesByDateRange(
            String owner,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return repository.findAllByOwnerAndCreatedAtBetween(
                owner, startDate, endDate
        );
    }

    // Mood Analytics: Count entries by mood
    public long getMoodCount(String owner, String mood) {
        return repository.countByOwnerAndMood(owner, mood);
    }

    // Get all favorite entries
    public List<JournalEntry> getFavoriteEntries(String owner) {
        return repository.findAllByOwnerAndFavoriteTrue(owner);
    }

    // Add or remove favorite
    public Optional<JournalEntry> toggleFavorite(String id, String owner) {

        Optional<JournalEntry> optionalEntry =
                repository.findByIdAndOwner(id, owner);

        if (optionalEntry.isEmpty()) {
            return Optional.empty();
        }

        JournalEntry entry = optionalEntry.get();

        entry.setFavorite(!entry.isFavorite());

        return Optional.of(repository.save(entry));
    }
}
