package com.savdev.io.filepermission;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Set;

public class PosixPermissionsBuilder {

    final Path filePath;
    final Set<PosixFilePermission> filePermissions;

    public PosixPermissionsBuilder(final String filePath) {
        this.filePath = Paths.get(filePath);
        if (isPosixSupported()) {
            try {
                this.filePermissions =
                        Sets.newHashSet(Files.getPosixFilePermissions(
                                Paths.get(filePath)));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            this.filePermissions = Collections.emptySet();
        }
    }

    public boolean isPosixSupported() {
        return FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains("posix");
    }

    public PosixPermissionsBuilder userGroupRead() {
        filePermissions.add(PosixFilePermission.GROUP_READ);
        return this;
    }

    public PosixPermissionsBuilder userGroupWrite() {
        filePermissions.add(PosixFilePermission.GROUP_WRITE);
        return this;
    }

    public PosixPermissionsBuilder otherRead() {
        filePermissions.add(PosixFilePermission.OTHERS_READ);
        return this;
    }

    public PosixPermissionsBuilder otherWrite() {
        filePermissions.add(PosixFilePermission.OTHERS_WRITE);
        return this;
    }

    public Set<PosixFilePermission> build() {
        if (isPosixSupported()) {
            try {
                Files.setPosixFilePermissions(
                        this.filePath, this.filePermissions);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.filePermissions;
    }
}
