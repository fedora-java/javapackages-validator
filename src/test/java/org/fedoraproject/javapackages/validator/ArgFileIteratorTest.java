package org.fedoraproject.javapackages.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class ArgFileIteratorTest {
    static List<Path> toPaths(String... strings) {
        return Arrays.asList(strings).stream().map(Paths::get).toList();
    }

    @Test
    void testExactFile() {
        var it = ArgFileIterator.create(toPaths("src/test/resources/arg_file_iterator/dangling-symlink-1-1.noarch.rpm"));
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test
    void testExactFiles() {
        var it = ArgFileIterator.create(toPaths(
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
        var it = ArgFileIterator.create(toPaths(
                "src/test/resources/arg_file_iterator/dir/empty_dir"));
        assertFalse(it.hasNext());
    }

    @Test
    void testFileAndDir() {
        var it = ArgFileIterator.create(toPaths(
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
    void testDirSymlink() {
        var it = ArgFileIterator.create(toPaths(
                "src/test/resources/arg_file_iterator/dir_symlink"));
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test
    void testDirDanglingSymlink() {
        var it = ArgFileIterator.create(toPaths(
                "src/test/resources/arg_file_iterator/dir_dangling_symlink"));
        var ex = assertThrows(Exception.class, it::next);
        assertFalse(ex instanceof NoSuchElementException);
    }
}
