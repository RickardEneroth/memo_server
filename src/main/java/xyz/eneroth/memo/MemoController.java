package xyz.eneroth.memo;

import com.heroku.sdk.jdbc.DatabaseUrl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@RestController
@RequestMapping(value = "/memo")
public class MemoController {

    private static final String TABLE_NAME = "memo_table";
    private static final String SEQUENCE_NAME = "id_seq";

    @RequestMapping("/")
    public String index() {
        return "Memo running on Spring Boot!";
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, value = "/__dbinit__")
    public String dbInit(@RequestParam(value="key") String key) {
        if (!key.equalsIgnoreCase(System.getenv("ADMIN_KEY"))) {
            return "Fel key!";
        }
        Connection connection = null;
        try {
            connection = DatabaseUrl.extract().getConnection();
            Statement stmt = connection.createStatement();
            try {
                stmt.executeUpdate("DROP TABLE " + TABLE_NAME);
                System.out.println("Table dropped");
                stmt.executeUpdate("DROP SEQUENCE " + SEQUENCE_NAME);
                System.out.println("Sequence dropped");
            } catch (Exception e) {
                e.printStackTrace();
            }
            stmt.executeUpdate("CREATE TABLE " + TABLE_NAME + " (id text, username text, memo text, tick timestamp)");
            System.out.println("Table created");
            stmt.executeUpdate("CREATE SEQUENCE " + SEQUENCE_NAME + " START 100");
            System.out.println("Sequence created");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "OK";
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, value = "/add")
    public void addMemo(@RequestParam(value="memo", defaultValue="EMPTY") String memo,
                        @RequestParam(value="userId", defaultValue="EMPTY") String userId) {
        Connection connection = null;
        try {
            connection = DatabaseUrl.extract().getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO " + TABLE_NAME + " VALUES (nextval('" + SEQUENCE_NAME + "'), '" + userId + "', '" + memo + "', now())");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.DELETE, value = "/delete")
    public @ResponseBody void deleteMemo(@RequestParam(value="id") String id) {
        Connection connection = null;
        try {
            connection = DatabaseUrl.extract().getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE id = '" + id + "'");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/getall")
    public @ResponseBody MemoResponse getAllMemos(@RequestParam(value="userId", defaultValue="EMPTY") String userId) {
        Connection connection = null;
        MemoResponse response = new MemoResponse();
        try {
            connection = DatabaseUrl.extract().getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs;
            if (userId.equalsIgnoreCase("EMPTY")) {
                rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME + " order by tick DESC");
            } else {
                rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME + " WHERE username = '" + userId + "' order by tick DESC");
            }
            while (rs.next()) {
                MemoRecord record = new MemoRecord();
                record.setId(rs.getString("id"));
                record.setMemo(rs.getString("memo"));
                record.setUserId(rs.getString("username"));
                record.setTimestamp(rs.getTimestamp("tick").toString());
                List list = response.getMemos();
                list.add(record);
                response.setMemos(list);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            List list = response.getMemos();
            list.add("ERROR:" + e.getMessage() + e.getStackTrace());
            response.setMemos(list);
            return null;
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}