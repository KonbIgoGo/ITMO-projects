package info.kgeorgiy.vpn.proto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class VpnClient extends VpnProto {
    private static final Logger log = LoggerFactory.getLogger(VpnClient.class);
    private final SocketFactory socketFactory;
    private final Config config;

    public VpnClient(
            int threads,
            int port,
            SocketFactory socketFactory,
            Config config
    ) throws IOException {
        super(threads, ServerSocketFactory.getDefault(), port);
        this.socketFactory = socketFactory;
        this.config = config;
    }

    public record Config(InetSocketAddress vpn, InetSocketAddress to, String username, String password) {}

    @Override
    protected void serve(Socket client) {
//        log.info("Client starting {}@{} to {}:{}", username, vpn, localPort, target);

        try (final Socket server = socketFactory.createSocket()) {
            server.connect(config.vpn());
            log.info("Client connected");
            final DataOutputStream os = new DataOutputStream(new BufferedOutputStream(server.getOutputStream()));
            os.writeUTF(config.username());
            os.writeUTF(config.password());
            os.writeUTF(config.to().getHostName());
            os.writeShort(config.to().getPort());
            log.info("Before flush");
            os.flush();
            log.info("Client header");

            copy(client, server);
        } catch (IOException e) {
            log.error("Client error", e);
        }
    }
}
