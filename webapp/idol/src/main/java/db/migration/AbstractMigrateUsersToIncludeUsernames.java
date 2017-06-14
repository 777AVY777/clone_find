package db.migration;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.savedsearches.OldUserEntity;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactoryImpl;
import com.hp.autonomy.types.idol.marshalling.marshallers.jaxb2.Jaxb2MarshallerFactory;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.requests.idol.actions.user.UserActions;
import com.hp.autonomy.types.requests.idol.actions.user.params.UserReadParams;
import lombok.AllArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


// Warning: this migration depends on access to system properties that are added in IdolFindConfigService
public abstract class AbstractMigrateUsersToIncludeUsernames implements SpringJdbcMigration {

    public static final String COMMUNITY_PROTOCOL = "find.community.protocol";
    public static final String COMMUNITY_HOST = "find.community.host";
    public static final String COMMUNITY_PORT = "find.community.port";

    private static final int HTTP_SOCKET_TIMEOUT = 9000;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 5;
    private static final int MAX_CONNECTIONS_TOTAL = 5;
    private static final String NO_USER_IN_COMMUNITY_ERROR_CODE = "UASERVERUSERREAD-2147438053";

    private final AciService aciService;
    private final AciServerDetails serverDetails;
    private final ProcessorFactory processorFactory;

    protected AbstractMigrateUsersToIncludeUsernames() {
        final String host = System.getProperty(COMMUNITY_HOST);
        final int port = Integer.parseInt(System.getProperty(COMMUNITY_PORT));
        final AciServerDetails.TransportProtocol transportProtocol = AciServerDetails.TransportProtocol.valueOf(System.getProperty(COMMUNITY_PROTOCOL));

        serverDetails = new AciServerDetails(transportProtocol, host, port);
        processorFactory = new ProcessorFactoryImpl(new Jaxb2MarshallerFactory());

        final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(HTTP_SOCKET_TIMEOUT).build();

        final HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                .setDefaultSocketConfig(socketConfig)
                .build();

        final AciHttpClient aciHttpClient = new AciHttpClientImpl(httpClient);
        aciService = new AciServiceImpl(aciHttpClient, serverDetails);
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        final List<OldUserEntity> userEntities = getAllUserEntities(jdbcTemplate);

        final Collection<OldUserEntity> hasUids = userEntities.stream()
                .filter(userEntity -> userEntity.getUid() != null)
                .collect(Collectors.toList());

        final List<OldUserEntity> userEntitiesToChange = hasUids.stream()
                .map(userEntity -> userEntity.toBuilder()
                        .username(getUsernameFromCommunity(userEntity))
                        .uid(null)
                        .build())
                .collect(Collectors.toList());

        final List<OldUserEntity> userEntitiesToDelete = userEntitiesToChange.stream()
                .filter(userEntity -> userEntity.getUsername() == null)
                .collect(Collectors.toList());

        updateUserEntities(jdbcTemplate, userEntitiesToChange);

        deleteUserEntities(jdbcTemplate, userEntitiesToDelete);
    }

    protected abstract String getUpdateUserSql();

    protected abstract void getBatchParameters(final PreparedStatement ps, final OldUserEntity userEntity) throws SQLException;

    private List<OldUserEntity> getAllUserEntities(final JdbcOperations jdbcTemplate) {
        return jdbcTemplate.query("SELECT * FROM users", new BeanPropertyRowMapper<>(OldUserEntity.class));
    }

    private String getUsernameFromCommunity(final OldUserEntity userEntity) throws AciErrorException {
        try {
            final AciParameters parameters = new AciParameters(UserActions.UserRead.name());
            parameters.add(UserReadParams.UID.name(), userEntity.getUid());

            final User user = aciService.executeAction(serverDetails, parameters, processorFactory.getResponseDataProcessor(User.class));
            return user.getUsername();
        } catch (final AciErrorException e) {
            if (NO_USER_IN_COMMUNITY_ERROR_CODE.equals(e.getErrorId())) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private void updateUserEntities(final JdbcOperations jdbcTemplate, final List<OldUserEntity> userEntities) {
        final String sql = getUpdateUserSql();

        jdbcTemplate.batchUpdate(sql, new UserEntitiesBatchPreparedStatementSetter(
                userEntities,
                (ps, i) -> getBatchParameters(ps, userEntities.get(i))
        ));
    }

    private void deleteUserEntities(final JdbcOperations jdbcTemplate, final List<OldUserEntity> userEntities) {
        final String sql = "DELETE FROM users WHERE user_id=?";

        jdbcTemplate.batchUpdate(sql, new UserEntitiesBatchPreparedStatementSetter(
                userEntities,
                (ps, i) -> ps.setLong(1, userEntities.get(i).getUserId()))
        );
    }

    @AllArgsConstructor
    private static class UserEntitiesBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

        private final List<OldUserEntity> userEntities;
        private final SQLConsumer consumer;

        @Override
        public void setValues(final PreparedStatement ps, final int i) throws SQLException {
            consumer.accept(ps, i);
        }

        @Override
        public int getBatchSize() {
            return userEntities.size();
        }
    }

    @FunctionalInterface
    private interface SQLConsumer {
        void accept(PreparedStatement ps, int i) throws SQLException;
    }

}