package com.neptune.crb.processing.service;

import com.neptune.crb.processing.model.CoreUser;
import com.neptune.crb.processing.model.ErrorData;
import com.neptune.crb.processing.model.CrbUser;
import com.neptune.crb.processing.model.User;
import com.neptune.crb.processing.util.AXWorker;
import com.neptune.crb.processing.util.CrbException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.neptune.crb.processing.util.Logger.logError;

@Service
public class UserService
{

    private final JdbcTemplate jdbcTemplate;
    private final AXWorker axWorker;


    public UserService(JdbcTemplate jdbcTemplate, AXWorker axWorker)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.axWorker = axWorker;

    }

//    public List<User> findLoginUsers(String userName, String password) {
//        String sql = "SELECT user_name, password, role,bu_nm,first_name,last_name  FROM users where userId = ? and password = ? ";
//        return jdbcTemplate.query(sql, (rs, rowNum) ->
//                new User(
//                        rs.getString("user_name"),
//                        rs.getString("password"),
//                        rs.getString("role"),
//                        rs.getString("bu_nm"),
//                        rs.getString("first_name")+rs.getString("last_name")
//                )
//        );
//    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public User findUserByEmail(String userName, String password)
    {
        String sql = "SELECT A.SYSUSER_ID, A.LOGIN_ID,B.PASSWD, A.FIRST_NM, A.LAST_NM AS NAME, A.EMP_NO, A.MAIN_BRANCH_ID, A.BU_NM, A.ROLE_ID, A.ROLE_NM \n" +
                "FROM V_SYSUSER A, SYSPWD_HIST B WHERE A.LOCKED_FG='N' " +
                "AND A.LOGIN_ID=:userId AND B.SYSUSER_ID=A.SYSUSER_ID AND A.REC_ST=B.REC_ST " +
                "AND B.PASSWD= :password AND B.REC_ST='A'";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userName);
        params.put("password", password);
        return namedParameterJdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                new User(
                        rs.getString("LOGIN_ID"),
                        rs.getString("PASSWD"),
                        rs.getString("ROLE_NM"),
                        rs.getString("BU_NM"),
                        rs.getString("BU_NM"),
                        rs.getString("FIRST_NM") + rs.getString("LAST_NM")
                )
        );
    }

    public boolean existsByReference(String loginId)
    {
        String sql = "SELECT COUNT(*) FROM " + axWorker.channelSchemaName + ".CRB_USER WHERE UPPER(LOGIN_ID) = ?";
        // Consider adding status condition if needed: AND STATUS = 'ACTIVE'

        try
        {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId);
            return count != null && count > 0;
        } catch (Exception e)
        {
            logError("Database error checking reference existence for loginId: {}", e);
            return false;
        }
    }

    //    public User findUserByUserName(String userName, String password) {
//        password = axWorker.getCypher().encrypt(password);
//        String sql = "SELECT A.SYSUSER_ID, A.LOGIN_ID, B.PASSWD,  " +
//                "A.FIRST_NM||' '||A.LAST_NM AS FULL_NAME, A.EMP_NO, A.BU_NM, C.ROLE,A.ROLE_NM   " +
//                "FROM " + axWorker.coreSchemaName + ".V_SYSUSER A " +
//                "JOIN " + axWorker.coreSchemaName + ".SYSPWD_HIST B " +
//                "ON B.SYSUSER_ID = A.SYSUSER_ID AND A.REC_ST = B.REC_ST " +
//                "JOIN " + axWorker.channelSchemaName + ".CRB_USER C " +
//                "ON ON A.LOGIN_ID = C.LOGIN_ID " +
//                "WHERE A.LOCKED_FG='N' " +
//                "AND A.LOGIN_ID = ? " +
//                "AND B.PASSWD = ? " +
//                "AND B.REC_ST = 'A'";
//        System.out.println(sql);
//        System.out.println(userName);
//        System.out.println(password);
//        try {
//            return jdbcTemplate.queryForObject(sql, new Object[]{userName, password}, (rs, rowNum) ->
//                    new User(
//                            rs.getString("LOGIN_ID"),
//                            "XXXXXXXXXX",
//                            rs.getString("ROLE"),
//                            rs.getString("BU_NM"),
//                            rs.getString("FULL_NAME"),
//                            rs.getString("ROLE_NM")
//
//                    )
//            );
//        } catch (IncorrectResultSizeDataAccessException e) {
//            logError("No user found or multiple users found: " + e.getMostSpecificCause().getMessage());
//             throw new CrbException(new ErrorData<>("CRB_0101","INVALID USER","INVALID USER"));
//        } catch (DataAccessException e) {
//            logError("Database error: " + e.getMostSpecificCause().getMessage());
//            throw e;
//        }
//    }
    public User findUserByUserName(String userName, String password)
    {
        // Encrypt/hash password as per your DB design
        String encryptedPassword = axWorker.getCypher().encrypt(password);

        String sql = "SELECT A.SYSUSER_ID, A.LOGIN_ID, A.FIRST_NM || ' ' || A.LAST_NM AS FULL_NAME, " +
                "A.EMP_NO, A.BU_NM, C.ROLE, A.ROLE_NM " +
                "FROM " + axWorker.coreSchemaName + ".V_SYSUSER A " +
                "JOIN " + axWorker.coreSchemaName + ".SYSPWD_HIST B " +
                "ON B.SYSUSER_ID = A.SYSUSER_ID AND A.REC_ST = B.REC_ST " +
                "JOIN " + axWorker.channelSchemaName + ".CRB_USER C " +
                "ON A.LOGIN_ID = C.LOGIN_ID " +
                "WHERE A.LOCKED_FG = 'N' AND A.LOGIN_ID = ? AND B.PASSWD = ? AND B.REC_ST = 'A'";
        System.out.println("sql "+sql);
        System.out.println("encryptedPassword "+encryptedPassword);
        try
        {
            return jdbcTemplate.queryForObject(sql, new Object[]{userName, encryptedPassword},
                    (rs, rowNum) -> new User(
                            rs.getString("LOGIN_ID"),
                            "XXXXXXXXXX", // mask password
                            rs.getString("ROLE"),
                            rs.getString("BU_NM"),
                            rs.getString("FULL_NAME"),
                            rs.getString("ROLE_NM")
                    )
            );
        } catch (IncorrectResultSizeDataAccessException e)
        {
            logError("No user found or multiple users found for LOGIN_ID: " + userName);
            throw new CrbException(new ErrorData<>("CRB_0101", "INVALID USER", "INVALID USER"));
        } catch (DataAccessException e)
        {
            logError("Database error: " + e.getMostSpecificCause().getMessage());
            throw e;
        }
    }

    public Map<String, Object> getAllUsers()
    {

        List<CrbUser> portalUsers = getCrbUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("portalUsers", portalUsers);
        result.put("portalUserCount", portalUsers.size());
        result.put("totalUsers", portalUsers.size());

        return result;
    }

    public Map<String, Object> getTheme()
    {

//{ "responseCode": "00", "data": { "theme": "ocean" } }
        Optional<String> portalTheme = getAppTheme();
        Map<String, Object> result = new HashMap<>();
        result.put("theme", portalTheme.orElse("Stone"));

        return result;
    }

    private Optional<String> getAppTheme(){
        String query = "SELECT PARAM_VALUE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_APP_SETTING A " +
                "WHERE PARAM_CD = ?";
        String theme = jdbcTemplate.queryForObject(query, String.class, "THEME");
        return Optional.ofNullable(theme);
    }
//    public int addUser(CrbUser user) {
//        String sql = "INSERT INTO " + axWorker.channelSchemaName + ".CRB_USER (LOGIN_ID, USER_NM, ROLE, CREATED_DATE, LAST_MODIFIED_DATE) VALUES (?, ?, ?, ?, ?)";
//        return jdbcTemplate.update(sql,
//                user.getLoginId(),
//                user.getUserName(),
//                user.getRole(),
//                user.getCreatedDate(),
//                user.getLastModifiedDate()
//        );
//    }

    public int addUser(CrbUser user)
    {
        String sql = "MERGE INTO " + axWorker.channelSchemaName + ".CRB_USER target " +
                "USING (SELECT ? as LOGIN_ID, ? as USER_NM, ? as ROLE, ? as CREATED_DATE, ? as LAST_MODIFIED_DATE FROM dual) source " +
                "ON (target.LOGIN_ID = source.LOGIN_ID) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET target.USER_NM = source.USER_NM, " +
                "             target.ROLE = source.ROLE, " +
                "             target.LAST_MODIFIED_DATE = source.LAST_MODIFIED_DATE " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (LOGIN_ID, USER_NM, ROLE, CREATED_DATE, LAST_MODIFIED_DATE) " +
                "  VALUES (source.LOGIN_ID, source.USER_NM, source.ROLE, source.CREATED_DATE, source.LAST_MODIFIED_DATE)";

        return jdbcTemplate.update(sql,
                user.getLoginId(),
                user.getUserName(),
                user.getRole(),
                user.getCreatedDate(),
                user.getLastModifiedDate()
        );
    }

    public Map<String, Object> getUserById(String loginId)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("user", getCrbUser(loginId));
        return result;
    }

    private List<CrbUser> getCrbUser(String loginId)
    {
        String sql = "SELECT LOGIN_ID, USER_NM, ROLE, CREATED_DATE, LAST_MODIFIED_DATE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_USER A " +
                "WHERE LOGIN_ID = ?";  // FIXED: Parameterized query

        return jdbcTemplate.query(sql, new Object[]{loginId}, (rs, rowNum) -> {
            // FIXED: Added null checks
            Timestamp createdDate = rs.getTimestamp("CREATED_DATE");
            Timestamp lastModifiedDate = rs.getTimestamp("LAST_MODIFIED_DATE");

            return new CrbUser(
                    rs.getString("LOGIN_ID"),
                    rs.getString("USER_NM"),
                    rs.getString("ROLE"),
                    "Y",
                    "A",
                    createdDate != null ? createdDate.toLocalDateTime() : null,
                    lastModifiedDate != null ? lastModifiedDate.toLocalDateTime() : null
            );
        });
    }

    private List<CoreUser> getCoreUsers()
    {
        String sql = "SELECT A.LOGIN_ID, A.FIRST_NM||' '||A.LAST_NM AS FULL_NAME " +
                "FROM " + axWorker.coreSchemaName + ".V_SYSUSER A " +
                "WHERE A.LOCKED_FG='N' AND A.REC_ST='A'";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CoreUser(
                        rs.getString("LOGIN_ID"),
                        rs.getString("FULL_NAME")
                )
        );
    }

    private List<CrbUser> getCrbUsers()
    {
        String sql = "SELECT " +
                "    A.LOGIN_ID,  " +
                "    A.FIRST_NM||' '||A.LAST_NM AS FULL_NAME, " +
                "    COALESCE(B.ROLE, 'N/A') AS ROLE, " +
                "    CASE  " +
                "        WHEN B.LOGIN_ID IS NOT NULL THEN 'Y'  " +
                "        ELSE 'N'  " +
                "    END AS CRB_USER, " +
                "    CASE  " +
                "        WHEN B.LOGIN_ID IS NOT NULL THEN 'A'  " +
                "        ELSE 'N'  " +
                "    END AS STATUS, " +
                "    B.CREATED_DATE, " +
                "    B.LAST_MODIFIED_DATE " +
                "FROM " + axWorker.coreSchemaName + ".V_SYSUSER A " +
                "LEFT JOIN " + axWorker.channelSchemaName + ".CRB_USER B ON A.LOGIN_ID = B.LOGIN_ID " +
                "WHERE A.LOCKED_FG = 'N'  " +
                "  AND A.REC_ST = 'A' " +
                "ORDER BY B.CREATED_DATE DESC NULLS LAST, A.LOGIN_ID ";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CrbUser(
                        rs.getString("LOGIN_ID"),
                        rs.getString("FULL_NAME"),
                        rs.getString("ROLE"),
                        rs.getString("CRB_USER"),
                        rs.getString("STATUS"),
                        convertToLocalDateTime(rs.getTimestamp("CREATED_DATE")),
                        convertToLocalDateTime(rs.getTimestamp("LAST_MODIFIED_DATE"))
                )
        );
    }

    private LocalDateTime convertToLocalDateTime(Timestamp timestamp)
    {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    public int updateUser(String loginId, String userName, String role)
    {
        String sql = "UPDATE CRB_USER SET USER_NM = ?, ROLE = ?, LAST_MODIFIED_DATE = CURRENT_TIMESTAMP WHERE LOGIN_ID = ?";
        return jdbcTemplate.update(sql, userName, role, loginId);
    }

    public int deleteUser(String loginId)
    {
        String sql = "DELETE FROM " + axWorker.channelSchemaName + ".CRB_USER WHERE LOGIN_ID = ?";
        return jdbcTemplate.update(sql, loginId);
    }

    public int updateUserEmail(Long id, String newEmail)
    {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newEmail, id);
    }

    public int deleteUser(Long id)
    {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

//    public int[] batchInsertUsers(List<User> users) {
//        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
//        return jdbcTemplate.batchUpdate(sql, users, users.size(),
//                (ps, user) -> {
//                    ps.setString(1, user.getUserName());
//                    ps.setString(2, user.getNames());
//                });
//    }

    @Transactional
    public void transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount)
    {
        String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        jdbcTemplate.update(withdrawSql, amount, fromAccountId);

        String depositSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        jdbcTemplate.update(depositSql, amount, toAccountId);
    }

    public int saveTheme(String theme)
    {
        String updateTheme = "UPDATE " + axWorker.channelSchemaName + ".CRB_APP_SETTING SET PARAM_VALUE = ?  WHERE UPPER(PARAM_CD) = 'THEME' ";
        return jdbcTemplate.update(updateTheme, theme);
    }
}

