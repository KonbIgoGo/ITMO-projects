package info.kgeorgiy.vpn.proto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class VpnServer extends VpnProto {
    private static final Logger log = LoggerFactory.getLogger(VpnServer.class);

    public VpnServer(int threads, ServerSocketFactory factory, int port) throws IOException {
        super(threads, factory, port);
    }

    @Override
    protected void serve(Socket client) {
        MDC.put("client", client.getRemoteSocketAddress().toString());
        try (client) {
            final DataInputStream is = new DataInputStream(client.getInputStream());
            final String username = is.readUTF();
            final String password = is.readUTF();
            final String host = is.readUTF();
            final int port = is.readUnsignedShort();

            log.info("Query {}:{} {}:{}", username, password, host, port);
            authorize(username, password, host, port);
            try (final Socket server = new Socket(host, port)) {
                copy(client, server);
            }
        } catch (IOException e) {
            log.info("Server error: {}", e.getMessage(), e);
        }
    }

    protected abstract void authorize(String username, String password, String host, int port) throws IOException;
}
