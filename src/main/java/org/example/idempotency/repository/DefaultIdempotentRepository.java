package org.example.idempotency.repository;

import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;
import lombok.RequiredArgsConstructor;
import org.example.idempotency.IdempotencyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.ResultSet;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DefaultIdempotentRepository implements IdempotentRepository {

  private static final String CONTAINS_QUERY =
      "select exists(select * from idempotency where id = ? and processed = true)";
  private static final String RESERVE_QUERY =
      "insert into idempotency(id, processed) values(?, false)";
  private static final String UPDATE_QUERY =
      "update idempotency set data = ? FORMAT JSON, processed = true where id = ?";
  private static final String GET_QUERY =
      "select data from idempotency where id = ?";
  private static final String REMOVE_QUERY = "delete from idempotency where id = ?";

  private final JdbcTemplate jdbcTemplate;

  @Override
  public boolean contains(String key) {
    Boolean result = jdbcTemplate.execute(
        CONTAINS_QUERY,
        (PreparedStatementCallback<Boolean>) ps -> {
          ps.setString(1, key);
          ResultSet rs = ps.executeQuery();
          if (rs.next()) {
            return rs.getBoolean(1);
          }
          return false;
        });

    return result != null && result;
  }

  @Override
  public void reserve(String key) {
    jdbcTemplate.update(RESERVE_QUERY, ps -> ps.setString(1, key));
  }

  @Override
  public void update(String key, String value) {
    jdbcTemplate.update(
        UPDATE_QUERY,
        ps -> {
          ps.setString(1, value);
          ps.setString(2, key);
        }
    );
  }

  @Override
  public String get(String key) {
    String result = jdbcTemplate.execute(
        GET_QUERY,
        (PreparedStatementCallback<String>) ps -> {
          ps.setString(1, key);
          ResultSet rs = ps.executeQuery();
          if (rs.next()) {
            return rs.getString(1);
          }
          return null;
        });

    if (result == null) {
      throw new IdempotencyException("Can't find value for key == " + key);
    }

    return result;
  }

  @Override
  public void remove(String key) {
    jdbcTemplate.update(REMOVE_QUERY, ps -> ps.setString(1, key));
  }
}
