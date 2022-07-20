package org.fedoraproject.javapackages.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ArgFileIteratorTest {
    @Test
    void testExactFile() {
        var it = new ArgFileIterator(Arrays.asList("src/test/resources/arg_file_iterator/dangling-symlink-1-1.noarch.rpm"));
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test
    void testExactFiles() {
        var it = new ArgFileIterator(Arrays.asList(
                "src/test/resources/arg_file_iterator/dangling-symlink-1-1.noarch.rpm",
                "src/test/resources/arg_file_iterator/dir/dangling-symlink-1-1.noarch.rpm",
                "src/test/resources/arg_file_iterator/dir/duplicate-file1-1-1.noarch.rpm"));
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test
    void testEmptyDir() {
        var it = new ArgFileIterator(Arrays.asList(
                "src/test/resources/arg_file_iterator/dir/empty_dir"));
        assertFalse(it.hasNext());
    }

    @Test
    void testFileAndDir() {
        var it = new ArgFileIterator(Arrays.asList(
                "src/test/resources/arg_file_iterator/dangling-symlink-1-1.noarch.rpm",
                "src/test/resources/arg_file_iterator/dir"));
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test
    void testDir() {
        var it = new ArgFileIterator(Arrays.asList(
                "src/test/resources/arg_file_iterator"));
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }
}
