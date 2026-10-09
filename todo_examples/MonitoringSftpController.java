package com.savdev.services.rest;

import com.sftp.ConnectionFactory;
import com.sftp.ConnectionProperties;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for live SFTP connectivity checks and client algorithm inspection.
 *
 * Uses the same Spring beans and configuration as the real importer,
 * so results reflect exactly what the application would experience.
 *
 * Endpoints:
 *   GET /sftp-check             — test all configured SFTP connections
 *   GET /sftp-check?index=N     — test only SFTP.N
 *   GET /sftp-algorithms        — show all SSH algorithms enabled in the JSch client
 */
@RestController
public class MonitoringSftpController {

  private static final Log LOGGER = LogFactory.getLog(MonitoringSftpController.class);

  /**
   * Path to the known_hosts file used by the WildFly process user.
   * JSch reads this by default when StrictHostKeyChecking is enabled.
   */
  private static final String KNOWN_HOSTS_PATH = "/opt/brandmaker/wildfly/.ssh/known_hosts";

  /** JSch config keys that hold comma-separated algorithm lists. */
  private static final String[][] ALGORITHM_KEYS = {
    { "kex",                      "KEX (Key Exchange)" },
    { "server_host_key",          "Host Key algorithms" },
    { "cipher.c2s",               "Ciphers client->server" },
    { "cipher.s2c",               "Ciphers server->client" },
    { "mac.c2s",                  "MACs client->server" },
    { "mac.s2c",                  "MACs server->client" },
    { "compression.c2s",          "Compression client->server" },
    { "compression.s2c",          "Compression server->client" },
    { "PubkeyAcceptedAlgorithms", "Pubkey accepted algorithms" },
  };

  private final ConnectionFactory connectionFactory;

  @Autowired
  public MonitoringSftpController(final ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  // -------------------------------------------------------------------------
  // GET /sftp-algorithms
  // -------------------------------------------------------------------------

  @RequestMapping(value = "/sftp-algorithms", method = RequestMethod.GET,
    produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<SftpAlgorithmReport> sftpAlgorithms() {

    String javaVersion = System.getProperty("java.version");
    String javaVendor  = System.getProperty("java.vendor");
    String jschVersion = getJschVersion();

    Map<String, List<String>> algorithms = new LinkedHashMap<>();
    for (String[] entry : ALGORITHM_KEYS) {
      String raw = JSch.getConfig(entry[0]);
      List<String> list = new ArrayList<>();
      if (raw != null && !raw.trim().isEmpty()) {
        for (String algo : raw.split(",")) {
          list.add(algo.trim());
        }
      }
      algorithms.put(entry[1], list);
    }

    return ResponseEntity.ok(new SftpAlgorithmReport(javaVersion, javaVendor, jschVersion, algorithms));
  }

  // -------------------------------------------------------------------------
  // GET /sftp-check
  // -------------------------------------------------------------------------

  @RequestMapping(value = "/sftp-check", method = RequestMethod.GET,
    produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<SftpCheckReport> sftpCheck(
    @RequestParam(value = "index", required = false) Integer index) {

    List<ConnectionProperties> allProps = connectionFactory.getConnectionPropertiesList();
    List<SftpConnectionResult> results  = new ArrayList<>();

    int from = 0;
    int to   = allProps.size();

    if (index != null) {
      if (index < 0 || index >= allProps.size()) {
        return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(new SftpCheckReport(
            "Invalid index: " + index + ". Available range: 0-" + (allProps.size() - 1),
            results));
      }
      from = index;
      to   = index + 1;
    }

    for (int i = from; i < to; i++) {
      ConnectionProperties props = allProps.get(i);
      String authType = (props.getIdentityFile() != null && !props.getIdentityFile().isEmpty())
        ? "publickey" : "password";

      SftpConnectionResult result = new SftpConnectionResult(
        i,
        props.getHost(),
        resolveIp(props.getHost()),
        props.getPort(),
        props.getUsername(),
        authType,
        props.getIdentityFile()
      );

      // Check host key status independently — before attempting connection
      result.setSftpHostKey(checkHostKey(props));

      LOGGER.info("[sftp-check] Testing SFTP." + i
        + " -> " + props.getHost() + " (" + result.getResolvedIp() + ")"
        + ":" + props.getPort()
        + " user=" + props.getUsername()
        + " authType=" + authType
        + " hostKey=" + result.getSftpHostKey());

      try {
        List<String> folders = connectionFactory.getConnection(i).listRelevantFolders();
        result.setSuccess(true);
        result.setFolderCount(folders.size());
        result.setFolders(folders);
        result.setMessage("OK - listed " + folders.size() + " folder(s) in home directory");
        LOGGER.info("[sftp-check] SFTP." + i + " OK. Folders: " + folders);
      } catch (Exception e) {
        result.setSuccess(false);
        result.setMessage("FAILED - " + e.getMessage());
        LOGGER.warn("[sftp-check] SFTP." + i + " FAILED: " + e.getMessage(), e);
      }

      results.add(result);
    }

    boolean allOk = results.stream().allMatch(SftpConnectionResult::isSuccess);
    SftpCheckReport report = new SftpCheckReport(allOk ? "ALL_OK" : "SOME_FAILED", results);
    return ResponseEntity.status(allOk ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(report);
  }

  // -------------------------------------------------------------------------
  // Host key check
  // -------------------------------------------------------------------------

  /**
   * Connects to the server with StrictHostKeyChecking=yes and immediately
   * disconnects. This is the only reliable way to detect a changed or missing
   * host key without relying on the production session (which has
   * StrictHostKeyChecking=no and would silently accept anything).
   *
   * Possible return values:
   *   "OK"                        — host key is present in known_hosts and matches
   *   "NOT_IN_KNOWN_HOSTS: run 'ssh-keyscan -p PORT HOST >> /opt/brandmaker/wildfly/.ssh/known_hosts' to add it"
   *   "HOST_KEY_CHANGED: the server's key no longer matches known_hosts — update it with 'ssh-keyscan -p PORT HOST' and replace the old entry in /opt/brandmaker/wildfly/.ssh/known_hosts"
   *   "KNOWN_HOSTS_NOT_FOUND: file /opt/brandmaker/wildfly/.ssh/known_hosts does not exist"
   *   "ERROR: <message>"
   */
  private String checkHostKey(ConnectionProperties props) {
    try {
      JSch jsch = new JSch();

      // Load known_hosts — if file doesn't exist, flag it immediately
      java.io.File knownHostsFile = new java.io.File(KNOWN_HOSTS_PATH);
      if (!knownHostsFile.exists()) {
        return "KNOWN_HOSTS_NOT_FOUND: file " + KNOWN_HOSTS_PATH + " does not exist — "
          + "create it and run: ssh-keyscan -p " + props.getPort()
          + " " + props.getHost() + " >> " + KNOWN_HOSTS_PATH;
      }
      jsch.setKnownHosts(KNOWN_HOSTS_PATH);

      // Check what status JSch gives for this host before even connecting
      HostKeyRepository hkr = jsch.getHostKeyRepository();
      String hostWithPort = "[" + props.getHost() + "]:" + props.getPort();
      // JSch stores non-22 ports as [host]:port, port 22 as just host
      String lookupKey = props.getPort() == 22 ? props.getHost() : hostWithPort;

      HostKey[] knownKeys = hkr.getHostKey(lookupKey, null);
      if (knownKeys == null || knownKeys.length == 0) {
        return "NOT_IN_KNOWN_HOSTS: run this on the server to add it: "
          + "ssh-keyscan -p " + props.getPort() + " " + props.getHost()
          + " >> " + KNOWN_HOSTS_PATH;
      }

      // Actually connect with strict checking to detect a changed key
      Session session = null;
      try {
        session = jsch.getSession(props.getUsername(), props.getHost(), props.getPort());
        session.setConfig("StrictHostKeyChecking", "yes");
        session.setTimeout(10000);
        session.connect();
        // If we get here the key matched — disconnect immediately
        return "OK";
      } catch (JSchException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains("reject") || msg.contains("CHANGED") || msg.contains("differs")) {
          return "HOST_KEY_CHANGED: the server key no longer matches " + KNOWN_HOSTS_PATH
            + " — to fix: ssh-keyscan -p " + props.getPort() + " " + props.getHost()
            + " (copy the new key and replace the old entry in " + KNOWN_HOSTS_PATH + ")";
        }
        // Auth failure is fine here — it means the host key WAS accepted
        // and only auth failed, which is expected since we use strict mode
        // with no credentials configured for this probe
        if (msg.contains("Auth") || msg.contains("auth") || msg.contains("password")
          || msg.contains("publickey")) {
          return "OK";
        }
        // Connection refused, timeout, etc. — not a host key problem
        return "ERROR: " + msg;
      } finally {
        if (session != null && session.isConnected()) {
          session.disconnect();
        }
      }

    } catch (Exception e) {
      LOGGER.warn("[sftp-check] Host key check failed for " + props.getHost() + ": " + e.getMessage());
      return "ERROR: " + e.getMessage();
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private String resolveIp(String host) {
    try {
      return InetAddress.getByName(host).getHostAddress();
    } catch (Exception e) {
      LOGGER.warn("[sftp-check] Could not resolve IP for host: " + host + " - " + e.getMessage());
      return "unresolved";
    }
  }

  private String getJschVersion() {
    try {
      Package pkg = JSch.class.getPackage();
      if (pkg != null && pkg.getImplementationVersion() != null) {
        return pkg.getImplementationVersion();
      }
    } catch (Exception ignored) { }
    try {
      java.io.InputStream is = JSch.class.getResourceAsStream(
        "/META-INF/maven/com.github.mwiede/jsch/pom.properties");
      if (is != null) {
        java.util.Properties props = new java.util.Properties();
        props.load(is);
        return props.getProperty("version", "unknown");
      }
    } catch (Exception ignored) { }
    return "unknown";
  }

  // -------------------------------------------------------------------------
  // Response DTOs
  // -------------------------------------------------------------------------

  public static class SftpAlgorithmReport {
    private final String javaVersion;
    private final String javaVendor;
    private final String jschVersion;
    private final Map<String, List<String>> algorithms;

    public SftpAlgorithmReport(String javaVersion, String javaVendor,
                               String jschVersion, Map<String, List<String>> algorithms) {
      this.javaVersion = javaVersion;
      this.javaVendor  = javaVendor;
      this.jschVersion = jschVersion;
      this.algorithms  = algorithms;
    }

    public String getJavaVersion()                    { return javaVersion; }
    public String getJavaVendor()                     { return javaVendor; }
    public String getJschVersion()                    { return jschVersion; }
    public Map<String, List<String>> getAlgorithms()  { return algorithms; }
  }

  public static class SftpCheckReport {
    private final String overallStatus;
    private final List<SftpConnectionResult> connections;

    public SftpCheckReport(String overallStatus, List<SftpConnectionResult> connections) {
      this.overallStatus = overallStatus;
      this.connections   = connections;
    }

    public String getOverallStatus()                    { return overallStatus; }
    public List<SftpConnectionResult> getConnections()  { return connections; }
  }

  public static class SftpConnectionResult {
    private final int index;
    private final String host;
    private final String resolvedIp;
    private final int port;
    private final String username;
    private final String authType;
    private final String identityFile;
    private String sftpHostKey;
    private boolean success;
    private String message;
    private int folderCount;
    private List<String> folders;

    public SftpConnectionResult(int index, String host, String resolvedIp,
                                int port, String username, String authType,
                                String identityFile) {
      this.index        = index;
      this.host         = host;
      this.resolvedIp   = resolvedIp;
      this.port         = port;
      this.username     = username;
      this.authType     = authType;
      this.identityFile = identityFile;
    }

    public int getIndex()              { return index; }
    public String getHost()            { return host; }
    public String getResolvedIp()      { return resolvedIp; }
    public int getPort()               { return port; }
    public String getUsername()        { return username; }
    public String getAuthType()        { return authType; }
    public String getIdentityFile()    { return identityFile; }
    public String getSftpHostKey()     { return sftpHostKey; }
    public boolean isSuccess()         { return success; }
    public String getMessage()         { return message; }
    public int getFolderCount()        { return folderCount; }
    public List<String> getFolders()   { return folders; }

    public void setSftpHostKey(String sftpHostKey) { this.sftpHostKey = sftpHostKey; }
    public void setSuccess(boolean success)        { this.success = success; }
    public void setMessage(String message)         { this.message = message; }
    public void setFolderCount(int folderCount)    { this.folderCount = folderCount; }
    public void setFolders(List<String> folders)   { this.folders = folders; }
  }
}