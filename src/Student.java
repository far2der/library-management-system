import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Student implements OrdinaryUser{
    
    private Connection connect() {
        String url = "jdbc:sqlite:library.db";
        Conn.conn = null;
        try {
            Conn.conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Conn.conn;
    }

    public String view() throws SQLException {
        return null;
    }

    public boolean readDocs(int id) throws SQLException {
        Conn.resSet = Conn.query.executeQuery("SELECT * FROM docs" + (id == 0 ? "" : " WHERE id = " + id));
        boolean result = false;

        while (Conn.resSet.next()) {
            int i = Conn.resSet.getInt("id");
            String name = Conn.resSet.getString("name");
            String author = Conn.resSet.getString("author");
            int type = Conn.resSet.getInt("type");
            int holder = Conn.resSet.getInt("taken_by");
            java.sql.Date due = Conn.resSet.getDate("due_when");
            System.out.print("ID" + i);
            System.out.print(" " + name);
            System.out.print(type == 1 ? ", book written" : type == 2 ? ", article written" : type == 3 ? ", AV material made" : "");
            System.out.print(" by " + author + ". ");
            if (Conn.resSet.getInt("reference") == 1) System.out.print("Reference material. ");
            if (Conn.resSet.getInt("bestseller") == 1) System.out.print("Current bestseller. ");
            if (Conn.resSet.getString("taken_by") != null && !Conn.resSet.getString("taken_by").isEmpty()) {
                System.out.print("Currently is held by ID" + holder);
                System.out.print(", due " + due + ".");
            }
            System.out.println();
            result = true;
        }
        if (!result) System.out.println("No such document exists in the database.");
        return result;
    }


    public void bookDocument(int user, int doc) throws SQLException {
        Conn.resSet = Conn.query.executeQuery("SELECT * FROM users WHERE id = " + user);
        boolean next = true;
        String[] holdi = new String[0];
        if (Conn.resSet.next())
            holdi = Conn.resSet.getString("holding").split(" ");
        else {
            next = false;
            System.out.println("No such user exists in the database.");
        }

        Conn.resSet = Conn.query.executeQuery("SELECT * FROM docs WHERE id = " + doc);
        int id = 0;
        if (next && Conn.resSet.next())
            if (Conn.resSet.getInt("reference") == 0)
                id = Conn.resSet.getInt("copy");
            else {
                System.out.println("This document is a reference which cannot be checked out.");
                next = false;
            }
        else {
            next = false;
            System.out.println("No such document exists in the database.");
        }
        if (next) {
            boolean docSet = Conn.resSet.getString("taken_by") == null;
            Conn.resSet = Conn.query.executeQuery("SELECT id FROM docs WHERE copy = " + id);

            boolean copy = false;

            while (Conn.resSet.next()) {
                for (String i : holdi) {
                    if (i.equals(Conn.resSet.getString("id"))) copy = true;
                }
            }

            if (copy) System.out.println("User already has one copy of the document.");
            else if (!docSet) System.out.println("The document has already been checked out.");

            if (docSet && !copy) {
                Conn.resSet = Conn.query.executeQuery("SELECT * FROM users WHERE id = " + user);
                String holding = Conn.resSet.getString("holding");
                int lvl = Conn.resSet.getInt("access");

                String sql = "UPDATE users SET holding = ? WHERE id = " + user;
                holding = holding + (holding.isEmpty() ? "" : " ") + doc;
                PreparedStatement ps = Conn.conn.prepareStatement(sql);
                ps.setString(1, holding);
                ps.executeUpdate();

                Conn.resSet = Conn.query.executeQuery("SELECT * FROM docs WHERE id = " + doc);
                int type = Conn.resSet.getInt("type");
                int bestseller = Conn.resSet.getInt("bestseller");
                Conn.query.executeUpdate("UPDATE docs SET taken_by = " + user + " WHERE id = " + doc);

                java.util.Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Conn.query.executeUpdate("UPDATE docs SET taken_when = " + sdf.format(now) + " WHERE id = " + doc);
                Calendar c = Calendar.getInstance();
                c.setTime(now);
                if (type == 1 && lvl <= 1) c.add(Calendar.DATE, 28);
                else if (type == 1 && bestseller == 1) c.add(Calendar.DATE, 14);
                else if (type == 1) c.add(Calendar.DATE, 21);
                else c.add(Calendar.DATE, 14);
                now = c.getTime();

                Conn.query.executeUpdate("UPDATE docs SET due_when = " + sdf.format(now) + " WHERE id = " + doc);

                System.out.println("Document has been successfully checked out!");
            }
        }
    }


    public String returnDoc(int id) throws SQLException {
        return null;
    }
}
