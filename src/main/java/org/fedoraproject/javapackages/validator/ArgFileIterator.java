package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import io.kojan.javadeptools.rpm.RpmPackage;

/// An iterator that processes argument file paths and yields `RpmPackage`
/// objects. It resolves files and directories, handling symbolic links and
/// nested directories.
class ArgFileIterator implements Iterator<RpmPackage> {

    /// Iterator over the provided argument paths.
    private Iterator<Path> argIterator;
    /// Iterator over the paths of RPM files within the current directory or file
    /// being processed.
    private Iterator<Path> pathIterator = null;

    /// Creates an `ArgFileIterator` from a given collection of paths.
    ///
    /// @param args an iterable collection of paths
    /// @return an instance of `ArgFileIterator`
    public static ArgFileIterator create(Iterable<Path> args) {
        var result = new ArgFileIterator();
        result.argIterator = args.iterator();
        result.pathIterator = result.advance();

        if (result.pathIterator == null) {
            result.pathIterator = Collections.<Path>emptyList().iterator();
        }

        return result;
    }

    /// Advances the iterator to the next available path, resolving symbolic links
    /// and handling directories to locate RPM files.
    ///
    /// @return an iterator over resolved paths, or `null` if no more paths
    ///         exist
    private Iterator<Path> advance() {
        while (argIterator.hasNext()) {
            Path argPath = argIterator.next().resolve(".").toAbsolutePath().normalize();

            try {
                if (Files.isSymbolicLink(argPath)) {
                    argPath = argPath.toRealPath();
                }

                if (Files.notExists(argPath)) {
                    throw new RuntimeException("File " + argPath + " does not exist");
                } else if (Files.isRegularFile(argPath)) {
                    return Arrays.asList(argPath).iterator();
                } else if (Files.isDirectory(argPath)) {
                    return Files.find(argPath, Integer.MAX_VALUE, (path, attributes) ->
                            !attributes.isDirectory() && path.toString().endsWith(".rpm"),
                            FileVisitOption.FOLLOW_LINKS).iterator();
                } else {
                    throw new IllegalStateException("File " + argPath + " of unknown type");
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return null;
    }

    /// Checks if there are more RPM packages to iterate over.
    ///
    /// @return `true` if more RPM packages are available, `false`
    ///         otherwise
    @Override
    public boolean hasNext() {
        try {
            if (pathIterator.hasNext()) {
                return true;
            }
        } catch (Exception ex) {
            // Ignore loops
            if (ex.getCause() instanceof FileSystemLoopException) {
                return hasNext();
            } else {
                throw ex;
            }
        }

        pathIterator = advance();
        return pathIterator != null;
    }

    /// Returns the next `RpmPackage` in the iteration.
    ///
    /// @return the next `RpmPackage`
    /// @throws RuntimeException if an error occurs while retrieving the package
    @Override
    public RpmPackage next() {
        try {
            return new RpmPackage(pathIterator.next());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
