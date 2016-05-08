package xyz.eneroth.memo;

import com.heroku.sdk.jdbc.DatabaseUrl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@RestController
@RequestMapping(value = "/memo")
public class MemoController {

    @RequestMapping("/")
    public String index() {
        return "Memo running on Spring Boot!";
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/add")
    public void addMemo(@RequestParam(value="memo", defaultValue="EMPTY") String memo,
                        @RequestParam(value="userId", defaultValue="EMPTY") String userId) {
        Connection connection = null;
        MemoResponse res = new MemoResponse();
        try {
            connection = DatabaseUrl.extract().getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS memo_table (username text, memo text, tick timestamp)");
            stmt.executeUpdate("INSERT INTO memo_table VALUES ('" + userId + "', '" + memo + "', now())");
        } catch (Exception e) {
            List list = res.getMemos();
            list.add("ERROR");
            res.setMemos(list);
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/deleteall")
    public @ResponseBody void deleteAllMemos(@RequestParam(value="userId", defaultValue="EMPTY") String userId) {
        Connection connection = null;
        MemoResponse res = new MemoResponse();
        try {
            connection = DatabaseUrl.extract().getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM memo_table WHERE username = '" + userId + "'");
        } catch (Exception e) {
            List list = res.getMemos();
            list.add("ERROR:" + e.getMessage() + e.getStackTrace());
            res.setMemos(list);
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
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
                rs = stmt.executeQuery("SELECT * FROM memo_table order by tick DESC");
            } else {
                rs = stmt.executeQuery("SELECT * FROM memo_table WHERE username = '" + userId + "' order by tick DESC");
            }
            while (rs.next()) {
                MemoRecord record = new MemoRecord();
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
            }
        }
    }
}