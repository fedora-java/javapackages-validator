package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ArgFileIterator implements Iterator<RpmPackageInfo> {
    private Iterator<String> argIterator;
    private Iterator<Path> pathIterator = null;

    public ArgFileIterator(List<String> argList) {
        this.argIterator = argList.iterator();
        pathIterator = advance();

        if (pathIterator == null) {
            pathIterator = Arrays.<Path>asList().iterator();
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
                            attributes.isRegularFile() && path.toString().endsWith(".rpm")).iterator();
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
        if (pathIterator.hasNext()) {
            return true;
        }

        pathIterator = advance();

        if (pathIterator != null) {
            return true;
        }

        return false;
    }

    @Override
    public RpmPackageInfo next() {
        return new RpmPackageInfo(pathIterator.next());
    }
}
