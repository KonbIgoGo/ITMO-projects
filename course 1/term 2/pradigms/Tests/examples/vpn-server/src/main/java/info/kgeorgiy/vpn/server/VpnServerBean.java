package info.kgeorgiy.vpn.server;

import info.kgeorgiy.vpn.proto.VpnServer;
import io.github.stevenjdh.simple.ssl.SimpleSSLContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

@Component
public class VpnServerBean {
    private static final Logger log = LoggerFactory.getLogger(VpnServerBean.class);

    @Value("${info.kgeorgiy.vpn.server.threads:10}")
    private int threads;

    @Value("${info.kgeorgiy.vpn.server.port:8887}")
    private int port;

    @Value("${info.kgeorgiy.vpn.key.path:server.jks}")
    private Path keyPath;

    @Value("${info.kgeorgiy.vpn.key.password:password}")
    private char[] keyPassword;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private VpnServer server;

    @PostConstruct
    public void start() throws IOException {
        log.info("Starting VPN server at {}", port);
        final SSLContext context = SimpleSSLContext.newBuilder()
                .withKeyStore(keyPath, keyPassword)
                .build();

        server = new VpnServer(threads, context.getServerSocketFactory(), port) {
            @Override
            protected void authorize(String username, String password, String host, int port) throws IOException {
                log.info("Got client {}:{}@{}:{}", username, password, host, port);
                final User user = users.findByUsername(username);
                if (user == null) {
                    throw new IOException("Unknown user %s".formatted(username));
                }
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new IOException("Invalid password for %s".formatted(username));
                }
                log.info("Valid user {}", username);
            }
        };

        server.start();
    }

    @PreDestroy
    public void stop() throws IOException {
        if (server != null) {
            server.close();
        }
    }
}
