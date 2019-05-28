package spark;

import spark.database.DAO;
import spark.database.Database;
import spark.database.Book;
import spark.database.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static spark.Spark.*;

public class Demo {

    private static DAO dao;

    private static Long getUserId(Request request) {
        String userId = request.cookie("userId");
        if (userId == null) {
            return null;
        }
        return Long.valueOf(userId);
    }

    private static String getUserSearch(Request request) {
        String userSearch = request.cookie("userSearch");
        return userSearch;
    }

    private static String getUserTypes(Request request) {
        String userTypes = request.cookie("userTypes");
        return userTypes;
    }

    private static void setUserId(Response response, long id) {
        response.cookie("userId", id + "");
    }

    private static void setUserSearch(Response response, String search) {
        response.cookie("userSearch", search);
    }

    private static void setUserTypes(Response response, String types) {
        response.cookie("userTypes", types);
    }

    private static String getFileAsString(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/resources/" + filename)));
        } catch (IOException e) {
            return null;
        }
    }

    private static String addScript(String body, String content) {
        return body.replace("</body>", "<script>" + content + "</script></body>");
    }

    private static String setNotes(String body, String content) {
        return body.replace("<!--notes go here-->", content);
    }

    private static String createDivBook(String nameOfBook, String text, long importance, String form, long index) {
        String content = "";
        content += "<div class=\"note\">\n" +
                "                <div class=\"note_head\">\n" +
                "                    <p class=\"nameOfBook\">";
        if (nameOfBook != null)
            content += nameOfBook;
        content += "</p>\n" +
                "                    <a href=\"/mark/" + index + "\" id=\"m" + index + "\"><img src=\"img/";
        if (importance == 1)
            content += "notimportant.jpg";
        else
            content += "important.jpg";
        content += "\" alt=\"importance\" title=\"Like\"></a>\n" +
                "                    <a href=\"/delete/" + index + "\" id=\"d" + index + "\"><img src=\"img/delete.png\" alt=\"delete\" title=\"Delete\"></a>\n" +
                "                </div>\n" +
                "                <div class=\"note_text\">\n" +
                "                    <p>";
        if (form.equals("list")) {
            content += "• ";
            content += text.replace("<br>", "<br>• ");
        } else
            content += text;
        content += "</p>";
        content += "                </div>\n" +
                "            </div>";
        return content;
    }

    private static void run() {
        port(8081);
        staticFiles.location("/public");

        Filter onlyForUsers = (request, response) -> {
            Long id = getUserId(request);
            if (id == null) {
                response.redirect("/login");
            }
        };

        Filter onlyForAnons = (request, response) -> {
            Long id = getUserId(request);
            if (id != null) {
                response.redirect("/books");
            }
        };

        before("/books", onlyForUsers);
        before("/books_new", onlyForUsers);
        before("/add", onlyForUsers);
        before("/delete/:index", onlyForUsers);
        before("/mark/:index", onlyForUsers);
        before("/logout", onlyForUsers);
        before("/login", onlyForAnons);
        before("/register", onlyForAnons);

        get("/", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("index.html");
            return null;
        });

        get("/index", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("index.html");
            return null;
        });

        get("/logout", (request, response) -> {
            response.removeCookie("userId");
            response.removeCookie("userSearch");
            response.removeCookie("userTypes");
            response.redirect("index.html");
            return null;
        });

        get("/login", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("index.html");
            return null;
        });

        post("/login", (request, response) -> {
            String name = request.queryParams("name");
            String pass = request.queryParams("password");
            User user = dao.getUser(name);
            if (user == null) {
                response.type("text/html");
                return addScript(getFileAsString("public/index.html"), "alert('Not such user!')");
            }

            if (!pass.equals(user.getPassword())) {
                response.type("text/html");
                return addScript(getFileAsString("public/index.html"), "alert('Wrong password!')");
            } else {
                setUserId(response, user.getId());
                response.redirect("/books");
                return null;
            }
        });

        get("/registration", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("registration.html");
            return null;
        });

        post("/registration", (request, response) -> {
            String name = request.queryParams("reg_login");
            String email = request.queryParams("reg_email");
            String pass = request.queryParams("reg_password");
            if (dao.getUser(name) != null) {
                response.type("text/html");
                return addScript(getFileAsString("public/registration.html"), "alert('User with the same name already exists!')");
            }
            if (name.length() == 0 || pass.length() == 0) {
                response.type("text/html");
                return addScript(getFileAsString("public/registration.html"), "alert('Cannot sign up!')");
            } else {
                dao.insertUser(name, email, pass);
                User user = dao.getUser(name);
                setUserId(response, user.getId());
                response.redirect("/books");
                return null;
            }
        });

        get("/books_new", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("/books");
            return null;
        });

        get("/books", (request, response) -> {
            String returnValue = getFileAsString("public/books.html");
            String content = "";
            List<Book> notes = dao.getUserNotes(getUserId(request));
            if (notes.size() == 0) {
                content = "No books :(";
            } else {
                if (notes.size() == 0) {
                    content = "No books :(";
                } else {
                    content += "<div class=\"notes_block\">\n";
                    for (int i = notes.size() - 1; i >= 0; --i) {
                        if (notes.get(i).getImportance() == 1) {

                            content += createDivBook(notes.get(i).getNameOfBook(), notes.get(i).getText(), notes.get(i).getImportance(), notes.get(i).getForm(), notes.get(i).getId());
                        }
                    }
                    for (int i = notes.size() - 1; i >= 0; --i) {
                        if (notes.get(i).getImportance() != 1) {
                            content += createDivBook(notes.get(i).getNameOfBook(), notes.get(i).getText(), notes.get(i).getImportance(), notes.get(i).getForm(), notes.get(i).getId());
                        }
                    }
                    content += "</div>";
                }
            }
            response.type("text/html");
            return setNotes(returnValue, content);
        });

        get("/add", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("add.html");
            return null;
        });

        post("/add", (request, response) -> {
            String nameOfBook = request.queryParams("nameOfBook");
            String note = request.queryParams("note");
            note = note.replace("\r\n", "<br>");
            String form = request.queryParams("form");
            dao.insertNote(nameOfBook, note, form, getUserId(request));
            response.redirect("/books");
            return null;
        });


        get("/mark/:index", (request, response) -> {
            String noteIndex = request.params("index");
            Book currentNote = dao.getNote(Long.parseLong(noteIndex));
            if (currentNote.getImportance() == 1)
                dao.updateNoteImportance(noteIndex, "0");
            else
                dao.updateNoteImportance(noteIndex, "1");
            response.redirect("/books");
            return null;
        });

        get("/delete/:index", (request, response) -> {
            String noteIndex = request.params("index");
            dao.deleteNote(noteIndex);
            response.redirect("/books");
            return null;
        });

    }

    public static void main(String[] args) {
        dao = new DAO(new Database());
        dao.createTables();
        run();
    }
}
