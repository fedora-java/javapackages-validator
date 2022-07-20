package org.fedoraproject.javapackages.validator;

import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.config.RpmPackage;

public class RpmPackageImpl implements RpmPackage {
    private RpmInfo rpmInfo;

    public RpmPackageImpl(RpmInfo rpmInfo) {
        super();
        this.rpmInfo = rpmInfo;
    }

    @Override
    public String getArch() {
        return rpmInfo.getArch();
    }

    @Override
    public int getEpoch() {
        return rpmInfo.getEpoch();
    }

    @Override
    public String getName() {
        return rpmInfo.getName();
    }

    @Override
    public String getRelease() {
        return rpmInfo.getRelease();
    }

    @Override
    public String getVersion() {
        return rpmInfo.getVersion();
    }

    @Override
    public String getPackageName() {
        if (isSourceRpm()) {
            return getName();
        } else {
            return Common.getPackageName(rpmInfo.getSourceRPM());
        }
    }

    @Override
    public boolean isSourceRpm() {
        return rpmInfo.isSourcePackage();
    }

    @Override
    public List<String> getBuildArchs() {
        return rpmInfo.getBuildArchs();
    }
}
