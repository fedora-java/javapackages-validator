package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.fedoraproject.javadeptools.rpm.RpmFile;

public class ArgFileIterator implements Iterator<RpmFile> {
    private Iterator<String> argIterator;
    private Iterator<Path> pathIterator = null;

    public ArgFileIterator(Iterable<String> args) {
        this.argIterator = args.iterator();
        pathIterator = advance();

        if (pathIterator == null) {
            pathIterator = Collections.<Path>emptyList().iterator();
        }
    }

    private Iterator<Path> advance() {
        while (argIterator.hasNext()) {
            Path argPath = Paths.get(argIterator.next()).resolve(".").toAbsolutePath().normalize();

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

        if (pathIterator != null) {
            return true;
        }

        return false;
    }

    @Override
    public RpmFile next() {
        try {
            return RpmFile.from(pathIterator.next().toUri().toURL());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
