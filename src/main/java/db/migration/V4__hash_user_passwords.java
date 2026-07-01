package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class V4__hash_user_passwords extends BaseJavaMigration {

    private static final int BCRYPT_STRENGTH = 12;
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    @Override
    public void migrate(Context context) throws Exception {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

        try (PreparedStatement select = context.getConnection()
                .prepareStatement("SELECT id, password FROM app_user");
                ResultSet users = select.executeQuery();
                PreparedStatement update = context.getConnection()
                        .prepareStatement("UPDATE app_user SET password = ? WHERE id = ?")) {
            while (users.next()) {
                String password = users.getString("password");
                if (password.matches(BCRYPT_PATTERN)) {
                    continue;
                }

                update.setString(1, passwordEncoder.encode(password));
                update.setObject(2, users.getObject("id"));
                update.addBatch();
            }
            update.executeBatch();
        }
    }
}
