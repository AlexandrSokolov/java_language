$ ssh -vvv -p 2022 -o StrictHostKeyChecking=no -o BatchMode=yes \
   ex@X.X.X.X 2>&1 | grep -i "kex\|cipher\|mac\|host key\|compress" | head -30

debug1: SSH2_MSG_KEXINIT sent
debug1: SSH2_MSG_KEXINIT received
debug2: kex_parse_kexinit: curve25519-sha256@libssh.org,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha256,diffie-hellman-group-exchange-sha1,diffie-hellman-group14-sha1,diffie-hellman-group1-sha1
debug2: kex_parse_kexinit: ecdsa-sha2-nistp256-cert-v01@openssh.com,ecdsa-sha2-nistp384-cert-v01@openssh.com,ecdsa-sha2-nistp521-cert-v01@openssh.com,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,ssh-ed25519-cert-v01@openssh.com,ssh-rsa-cert-v01@openssh.com,ssh-dss-cert-v01@openssh.com,ssh-rsa-cert-v00@openssh.com,ssh-dss-cert-v00@openssh.com,ssh-ed25519,ssh-rsa,ssh-dss
debug2: kex_parse_kexinit: aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-cbc,3des-cbc,blowfish-cbc,cast128-cbc,aes192-cbc,aes256-cbc,arcfour,rijndael-cbc@lysator.liu.se
debug2: kex_parse_kexinit: aes128-ctr,aes192-ctr,aes256-ctr,arcfour256,arcfour128,aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-cbc,3des-cbc,blowfish-cbc,cast128-cbc,aes192-cbc,aes256-cbc,arcfour,rijndael-cbc@lysator.liu.se
debug2: kex_parse_kexinit: hmac-md5-etm@openssh.com,hmac-sha1-etm@openssh.com,umac-64-etm@openssh.com,umac-128-etm@openssh.com,hmac-sha2-256-etm@openssh.com,hmac-sha2-512-etm@openssh.com,hmac-ripemd160-etm@openssh.com,hmac-sha1-96-etm@openssh.com,hmac-md5-96-etm@openssh.com,hmac-md5,hmac-sha1,umac-64@openssh.com,umac-128@openssh.com,hmac-sha2-256,hmac-sha2-512,hmac-ripemd160,hmac-ripemd160@openssh.com,hmac-sha1-96,hmac-md5-96
debug2: kex_parse_kexinit: hmac-md5-etm@openssh.com,hmac-sha1-etm@openssh.com,umac-64-etm@openssh.com,umac-128-etm@openssh.com,hmac-sha2-256-etm@openssh.com,hmac-sha2-512-etm@openssh.com,hmac-ripemd160-etm@openssh.com,hmac-sha1-96-etm@openssh.com,hmac-md5-96-etm@openssh.com,hmac-md5,hmac-sha1,umac-64@openssh.com,umac-128@openssh.com,hmac-sha2-256,hmac-sha2-512,hmac-ripemd160,hmac-ripemd160@openssh.com,hmac-sha1-96,hmac-md5-96
debug2: kex_parse_kexinit: none,zlib@openssh.com,zlib
debug2: kex_parse_kexinit: none,zlib@openssh.com,zlib
debug2: kex_parse_kexinit:
debug2: kex_parse_kexinit:
debug2: kex_parse_kexinit: first_kex_follows 0
debug2: kex_parse_kexinit: reserved 0
debug2: kex_parse_kexinit: curve25519-sha256,curve25519-sha256@libssh.org,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group14-sha256
debug2: kex_parse_kexinit: rsa-sha2-256,rsa-sha2-512,ecdsa-sha2-nistp256,ssh-ed25519
debug2: kex_parse_kexinit: aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-ctr,aes192-ctr,aes256-ctr
debug2: kex_parse_kexinit: aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-ctr,aes192-ctr,aes256-ctr
debug2: kex_parse_kexinit: hmac-sha2-256-etm@openssh.com,hmac-sha2-256
debug2: kex_parse_kexinit: hmac-sha2-256-etm@openssh.com,hmac-sha2-256
debug2: kex_parse_kexinit: none
debug2: kex_parse_kexinit: none
debug2: kex_parse_kexinit:
debug2: kex_parse_kexinit:
debug2: kex_parse_kexinit: first_kex_follows 0
debug2: kex_parse_kexinit: reserved 0
debug2: mac_setup: setup hmac-sha2-256-etm@openssh.com
debug1: kex: server->client aes128-ctr hmac-sha2-256-etm@openssh.com none
debug2: mac_setup: setup hmac-sha2-256-etm@openssh.com
debug1: kex: client->server aes128-ctr hmac-sha2-256-etm@openssh.com none

Supported algorithms:
The output contains two sets of kex_parse_kexinit blocks — the first is your client's offerings, the second is the server's. Here's what the server (SFTP.0 / FinRP) supports:

🔑 KEX (Key Exchange) algorithms
curve25519-sha256
curve25519-sha256@libssh.org
ecdh-sha2-nistp256
ecdh-sha2-nistp384
ecdh-sha2-nistp521
diffie-hellman-group14-sha256
🖥️ Host Key algorithms
rsa-sha2-256
rsa-sha2-512
ecdsa-sha2-nistp256
ssh-ed25519
🔒 Ciphers (client→server / server→client — identical)
aes128-gcm@openssh.com
aes256-gcm@openssh.com
chacha20-poly1305@openssh.com
aes128-ctr
aes192-ctr
aes256-ctr
✅ MACs (client→server / server→client — identical)
hmac-sha2-256-etm@openssh.com
hmac-sha2-256
🗜️ Compression
none

#scan without authenticating at all (nmap might be not installed or not allowed)
nmap -p 2022 --script ssh2-enum-algos X.X.X.X

### Enable ssh-rsa (SHA-1) for JSch

    static {
        // JSch 2.x disables ssh-rsa (SHA-1) by default.
        // Some servers (e.g. Apache MINA SSHD 2.4.0) still require it.
        // We append it to the existing list so nothing already enabled is removed.
        String existing = JSch.getConfig("PubkeyAcceptedAlgorithms");
        if (existing != null && !existing.contains("ssh-rsa")) {
            JSch.setConfig("PubkeyAcceptedAlgorithms", existing + ",ssh-rsa");
        }
    }

### JSch maven deps

```xml
    <!-- https://mvnrepository.com/artifact/com.github.mwiede/jsch -->
    <dependency>
      <groupId>com.github.mwiede</groupId>
      <artifactId>jsch</artifactId>
      <version>2.28.2</version>
    </dependency>
        <!--
        Required ONLY for Java 8.
        Bridges missing JVM crypto algorithms like ed25519 for JSch 2.x.
        Remove when upgrading to Java 11+:
        -->
    <dependency>
      <groupId>org.bouncycastle</groupId>
      <artifactId>bcprov-jdk18on</artifactId>
      <version>1.84</version>
    </dependency>
```