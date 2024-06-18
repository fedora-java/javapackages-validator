Name:           nvr-metadata-epoch-0
Epoch:          0
Version:        1.2.abc~m2
Release:        1.el9
Summary:        RPM that contains a JAR, Epoch 0
License:        CC0
BuildArch:      noarch
%description
%{summary}.
%prep
%build
rm -rf %{name}
mkdir -p %{name}
echo "Rpm-Name: %{name}" >> %{name}/MANIFEST.MF
echo "Rpm-Epoch: %{epoch}" >> %{name}/MANIFEST.MF
echo "Rpm-Version: %{version}" >> %{name}/MANIFEST.MF
echo "Rpm-Release: %{release}" >> %{name}/MANIFEST.MF
jar -c -m %{name}/MANIFEST.MF -f %{name}/%{name}.jar

%install
install -m 644 -D %{name}/%{name}.jar %{buildroot}/usr/share/java/%{name}.jar

%files
/usr/share/java/%{name}.jar

%changelog
