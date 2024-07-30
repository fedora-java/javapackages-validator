Name:           nvr-metadata-non-javadir
Version:        1
Release:        1
Summary:        RPM that contains a JAR outsides of javadir
License:        CC0
BuildArch:      noarch
%description
%{summary}.
%prep
%build
rm -rf %{name}
mkdir -p %{name}
touch %{name}/MANIFEST.MF
jar -c -m %{name}/MANIFEST.MF -f %{name}/%{name}.jar

%install
install -m 644 -D %{name}/%{name}.jar %{buildroot}/usr/share/%{name}/%{name}.jar

%files
/usr/share/%{name}/%{name}.jar

%changelog
