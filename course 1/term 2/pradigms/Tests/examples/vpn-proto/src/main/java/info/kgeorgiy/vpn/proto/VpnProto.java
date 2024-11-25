package info.kgeorgiy.vpn.proto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class VpnProto implements AutoCloseable {
    private static final int BUFFER_SIZE = 2048;

    private static final Logger log = LoggerFactory.getLogger(VpnProto.class);

    private ServerSocket serverSocket;
    protected ExecutorService executor;

    public VpnProto(final int threads, final ServerSocketFactory factory, final int port) throws IOException {
        serverSocket = factory.createServerSocket(port);

        executor = Executors.newScheduledThreadPool(threads);
    }

    public void start() {
        executor.submit(() -> {
            log.info("Started at {}", serverSocket.getLocalSocketAddress());
            MDC.clear();
            try {
                while (true) {
                    final Socket client = serverSocket.accept();
                    log.info("Got client %s".formatted(client.getRemoteSocketAddress()));
                    executor.submit(() -> serve(client));
                }
            } catch (IOException e) {
                log.error("Accept error", e);
            }
        });
    }

    protected abstract void serve(Socket client);


    private static void copy(final String direction, final InputStream is, final Socket to) {
        MDC.put("dir", direction);
        long total = 0;
        try {
            final OutputStream os = to.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            log.debug("Copy start");
            while ((read = is.read(buffer)) > 0) {
                total += read;
                os.write(buffer, 0, read);
                os.flush();
                log.debug("Copy {}", read);
            }
            to.shutdownOutput();
        } catch (final IOException e) {
            log.error("Copy error: {}", e.getMessage(), e);
        } finally {
            log.info("Copy done {} bytes transferred", total);
            MDC.remove("dir");
        }
    }

    protected void copy(Socket client, Socket server) throws IOException {
        try {
            MDC.put("server", server.getRemoteSocketAddress().toString());
            log.info("Connected to server");

            final InputStream serverIS = server.getInputStream();
            final Map<String, String> context = MDC.getCopyOfContextMap();
            final Future<?> future = executor.submit(() -> {
                MDC.setContextMap(context);
                copy("server->client", serverIS, client);
            });
            copy("client->server", client.getInputStream(), server);
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Copy error {}", e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
