
package net.kritika.journalApp;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Document(collection = "journal_entries")
@CompoundIndex(
        name = "owner_title_index",
        def = "{'owner': 1, 'title': 1}"
)
public class JournalEntry {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content cannot exceed 10000 characters")
    private String content;

    private LocalDateTime createdAt;

    private String owner;

    // Category
    private String category;

    // Tags
    private List<String> tags;

    // Mood Tracker
    private String mood;

    // Favorites / Bookmarks
    private boolean favorite = false;

    public JournalEntry() {
    }

    public JournalEntry(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.favorite = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // Category getter and setter
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Tags getter and setter
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // Mood getter and setter
    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    // Favorite getter and setter
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
