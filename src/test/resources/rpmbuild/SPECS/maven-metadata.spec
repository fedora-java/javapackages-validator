Name:           maven-metadata
Version:        1
Release:        1
Summary:        RPM that contains a JAR with Maven metadata
License:        CC0
BuildArch:      noarch
BuildRequires:  javapackages-local

%description
%{summary}.

%prep
%setup -cT

%install
touch mf
jar cmf mf bar.jar
%mvn_artifact foo:bar:1 bar.jar
%mvn_install

%files -f .mfiles

%changelog
