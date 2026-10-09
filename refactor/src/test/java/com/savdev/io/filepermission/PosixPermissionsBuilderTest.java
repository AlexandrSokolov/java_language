package com.savdev.io.filepermission;

import com.savdev.io.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class PosixPermissionsBuilderTest extends BaseTest {

    @Test
    public void testPosixPermissionsBuilder() throws IOException {

        String tempFilePath = filePathInTestTempFolder("perm.file.txt");
        File file = new File(tempFilePath);
        file.createNewFile();

        PosixPermissionsBuilder permissionsBuilder =
                new PosixPermissionsBuilder(tempFilePath);
        if (permissionsBuilder.isPosixSupported()){
            //set some rights to the file:
            Assert.assertFalse(permissionsBuilder.userGroupRead()
                .userGroupWrite()
                .otherRead().build()
                    .isEmpty());
        }
    }
}
