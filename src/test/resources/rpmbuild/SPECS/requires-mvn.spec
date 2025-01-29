Name:           requires-mvn
Version:        1
Release:        1
Summary:        RPM that requires valid 'mvn' coordinates
License:        CC0
BuildArch:      noarch

Requires:       mvn(artifact1:groupID)
Requires:       mvn(artifact2:groupID) > 1.0.0
Requires:       mvn(artifact3:groupID) >= 1.0.0
Requires:       mvn(artifact4:groupID) == 1.0.0
Requires:       mvn(artifact5:groupID) <= 1.0.0
Requires:       mvn(artifact6:groupID) < 1.0.0

%description
%{summary}.

%prep

%build

%install

%files

%changelog
