package info.kgeorgiy.vpn.client;

import info.kgeorgiy.vpn.proto.VpnClient;
import io.github.stevenjdh.simple.ssl.SimpleSSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_PORT = 80;

    @CommandLine.Command
    private static class Command implements Callable<Void> {
        @CommandLine.Option(names={"--user", "-u"}, required = true, description = "Username")
        String user;

        @CommandLine.Option(names={"--password", "-p"}, required = true, description = "Password")
        String password;

        @CommandLine.Option(names={"--vpn", "-v"}, required = true, description = "VPN host:port")
        String vpn;

        @CommandLine.Option(names={"--target", "-t"}, required = true, description = "Target host:port")
        String target;

        @CommandLine.Option(names={"--local", "-l"}, defaultValue = "-1", description = "Local port")
        int localPort;

        @CommandLine.Option(names={"--cert", "-c"}, defaultValue = "client.cer", description = "VPN server sertificate")
        Path certPath;

        @CommandLine.Option(names={"--no-ssl", "-n"}, defaultValue = "false", description = "Disable ssl")
        boolean noSSl;

        @Override
        public Void call() throws IOException {
            InetSocketAddress vpn = parseSocketAddress("vpn", this.vpn);
            InetSocketAddress to = parseSocketAddress("to", this.target);
            if (localPort < 0) {
                localPort = to.getPort();
            }
            final SocketFactory socketFactory = noSSl
                    ? SocketFactory.getDefault()
                    : SimpleSSLContext.newPEMContextBuilder()
                            .withPublicKey(certPath)
                            .build()
                            .getSocketFactory();

            log.info("Connecting {}:{}@{} to {}:{}", user, password, vpn, localPort, to);
            final VpnClient.Config config = new VpnClient.Config(vpn, to, user, password);
            try (final VpnClient client = new VpnClient(10, localPort, socketFactory, config)) {
                log.info("Created");
                client.start();
                System.out.println("Press any key to exit");
                System.in.read();
                log.info("Done");
            }
            return null;
        }
    }


    private static InetSocketAddress parseSocketAddress(final String name, final String address) throws IllegalArgumentException {
        try {
            final int index = address.indexOf(":");
            return index < 0
                    ? new InetSocketAddress(address, DEFAULT_PORT)
                    : new InetSocketAddress(address.substring(0, index), Integer.parseInt(address.substring(index + 1)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid %s port: %s".formatted(name, e.getMessage()), e);
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(Command.class).execute(args));
    }
}
