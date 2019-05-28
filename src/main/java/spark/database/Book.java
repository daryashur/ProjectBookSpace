package spark.database;

public class Book {
    private long id;
    private String nameOfBook;
    private String text;
    private long importance;
    private String form;
    private long userId;

    public Book() {
    }

    public Book(long id, String nameOfBook, String text, long importance, String form, long userId) {
        this.id = id;
        this.nameOfBook = nameOfBook;
        this.text = text;
        this.importance = importance;
        this.form = form;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public String getNameOfBook() {
        return nameOfBook;
    }

    public String getText() {
        return text;
    }

    public long getImportance() {
        return importance;
    }

    public String getForm() {
        return form;
    }

    public long getUserId() {
        return userId;
    }

    public void setNameOfBook(String nameOfBook) {
        this.nameOfBook = nameOfBook;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImportance(long importance) {
        this.importance = importance;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return text;
    }
}