import java.io.BufferedWriter;
import java.io.IOException;

public class ClientConnection {
    private final BufferedWriter out;
    private final Object writeLock = new Object(); // lock for synchronizing writes

    public ClientConnection(BufferedWriter out) {
        this.out = out;
    }

    public void sendLine(String line) throws IOException {
        synchronized (writeLock) {
            out.write(line);
            out.write("\n");
            out.flush();
        }
    }

    public void sendLines(String[] lines) throws IOException {
        synchronized (writeLock) {
            for (String line : lines) {
                out.write(line);
                out.write("\n");
            }
            out.flush();
        }



    }
}

    



