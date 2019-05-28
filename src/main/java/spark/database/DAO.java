package spark.database;

import java.util.ArrayList;
import java.util.List;

public class DAO {
    private final Database database;

    public DAO(Database database) {
        this.database = database;
    }

    public void createTables() {
        database.executeUpdate("CREATE TABLE IF NOT EXISTS my_users(id IDENTITY(1,1) NOT NULL PRIMARY KEY, name VARCHAR(30) NOT NULL UNIQUE, email VARCHAR(30) UNIQUE, password VARCHAR(30) NOT NULL);");
        database.executeUpdate("CREATE TABLE IF NOT EXISTS books(id IDENTITY NOT NULL PRIMARY KEY, nameOfBook VARCHAR(25), text VARCHAR(1000) NOT NULL, importance BIGINT NOT NULL, form VARCHAR(10) NOT NULL, user_id BIGINT NOT NULL);");
        database.executeUpdate("ALTER TABLE IF EXISTS books ADD CONSTRAINT IF NOT EXISTS fk_user_id FOREIGN KEY (user_id) REFERENCES my_users(id) ON DELETE CASCADE;");//связывает таблицу ьфйюзер по шв с
       /* database.executeUpdate("DROP TABLE IF EXISTS my_users;");
        database.executeUpdate("DROP TABLE IF EXISTS books;");*/
    }

    public void insertUser(String name, String email, String password) {
        if (email.length() == 0)
            database.executeUpdate("INSERT INTO my_users (name, password) VALUES (" + database.escapeSQL(name) + ", " + database.escapeSQL(password) + ");");
        else
            database.executeUpdate("INSERT INTO my_users (name, email, password) VALUES (" + database.escapeSQL(name) + ", " + database.escapeSQL(email) + ", " + database.escapeSQL(password) + ");");
    }

    public void deleteUser(String id) {
        database.executeUpdate("DELETE FROM my_users WHERE id = " + database.escapeSQL(id) + ";");
        database.executeUpdate("DELETE FROM books WHERE user_id = " + database.escapeSQL(id) + ";");
    }

    public void insertNote(String nameOfBook, String text, String form, long userId) {
        database.executeUpdate("INSERT INTO books (nameOfBook, text, importance, form, user_id) VALUES (" + database.escapeSQL(nameOfBook) + ", " + database.escapeSQL(text) + ", 0, "  + database.escapeSQL(form)  + ", " + userId + ");");
    }

    public void deleteNote(String noteId) {
        database.executeUpdate("DELETE FROM books WHERE id = " + database.escapeSQL(noteId) + ";");
    }

    public void updateNoteImportance(String noteId, String value) {
        database.executeUpdate("UPDATE books SET importance = " + database.escapeSQL(value)+ " WHERE id = " + database.escapeSQL(noteId) + ";");
    }

    public void updateNoteText(String noteId, String value) {
        database.executeUpdate("UPDATE books SET text = " + database.escapeSQL(value)+ " WHERE id = " + database.escapeSQL(noteId) + ";");
    }

    public void updateNoteTopic(String noteId, String value) {
        database.executeUpdate("UPDATE books SET topic = " + database.escapeSQL(value)+ " WHERE id = " + database.escapeSQL(noteId) + ";");
    }

    public User getUser(String name) {
        String sql = "SELECT * FROM my_users WHERE name = " + database.escapeSQL(name) + ";";
        return database.executeQuery(sql, resultSet -> {
            if (resultSet.first()) {
                long id = resultSet.getLong("id");
                String currentUserName = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                return new User(id, currentUserName, email, password);
            }
            return null;
        });
    }

    public User getUser(long id) {
        String sql = "SELECT * FROM my_users WHERE id = " + database.escapeSQL(id + "") + ";";
        return database.executeQuery(sql, resultSet -> {
            if (resultSet.first()) {
                long currentId = resultSet.getLong("id");
                String currentUserName = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                return new User(currentId, currentUserName, email, password);
            }
            return null;
        });
    }

    public Book getNote(Long id) {
        String sql = "SELECT * FROM books WHERE id = " + database.escapeSQL(id + "") + ";";
        return database.executeQuery(sql, resultSet -> {
            if (resultSet.first()) {
                long noteId = resultSet.getLong("id");
                String topic = resultSet.getString("nameOfBook");
                String text = resultSet.getString("text");
                long importance = resultSet.getLong("importance");
                String form = resultSet.getString("form");
                long user_id = resultSet.getLong("user_id");
                return new Book(noteId, topic, text, importance,form, user_id);
            }
            return null;
        });
    }

    public List<Book> getNotesBySearch(Long id, String search) {
        List<Book> result = new ArrayList<>();
        String sql = "SELECT n.* FROM notes AS n INNER JOIN my_users AS u ON n.user_id = u.id WHERE u.id = " + database.escapeSQL(id + "") + " AND (topic LIKE " + database.escapeSQL('%' + search + '%') + " OR text LIKE " + database.escapeSQL('%' + search + '%') + ");";
        return database.executeQuery(sql, resultSet -> {
            while (resultSet.next()) {
                long noteId = resultSet.getLong("id");
                String topic = resultSet.getString("nameOfBook");
                String text = resultSet.getString("text");
                long importance = resultSet.getLong("importance");
                String form = resultSet.getString("form");
                long user_id = resultSet.getLong("user_id");
                result.add(new Book(noteId, topic, text, importance, form, user_id));
            }
            return result;
        });
    }

    public List<Book> getUserNotes(Long id) {
        List<Book> result = new ArrayList<>();
        System.out.println("getUserNotes");
        String sql = "SELECT n.* FROM books AS n INNER JOIN my_users AS u ON n.user_id = u.id WHERE u.id= " + database.escapeSQL(id + "") + ";";
        return database.executeQuery(sql, resultSet -> {
            while (resultSet.next()) {
                long noteId = resultSet.getLong("id");
                String topic = resultSet.getString("nameOfBook");
                String text = resultSet.getString("text");
                long importance = resultSet.getLong("importance");
                String form = resultSet.getString("form");
                long user_id = resultSet.getLong("user_id");
                result.add(new Book(noteId, topic, text, importance, form, user_id));
            }
            return result;
        });
    }
}
