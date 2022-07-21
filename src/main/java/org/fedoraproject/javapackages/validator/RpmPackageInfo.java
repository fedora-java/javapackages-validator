package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.config.RpmPackage;

public class RpmPackageInfo implements RpmPathInfo, RpmPackage {
    private final Path path;
    private final RpmInfo info;

    public RpmPackageInfo(Path path) {
        this.path = path;
        try {
            info = new RpmInfo(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public RpmInfo getInfo() {
        return info;
    }

    @Override
    public RpmPackage getRpmPackage() {
        return this;
    }

    @Override
    public String getArch() {
        return info.getArch();
    }

    @Override
    public int getEpoch() {
        return info.getEpoch();
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public String getRelease() {
        return info.getRelease();
    }

    @Override
    public String getVersion() {
        return info.getVersion();
    }

    @Override
    public String getPackageName() {
        if (isSourceRpm()) {
            return getName();
        } else {
            return Common.getPackageName(info.getSourceRPM());
        }
    }

    @Override
    public boolean isSourceRpm() {
        return info.isSourcePackage();
    }

    @Override
    public List<String> getBuildArchs() {
        return info.getBuildArchs();
    }
}
