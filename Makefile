MAKEFLAGS += -r
TARGET ?= rpms

components:= $(addprefix $(TARGET)/,\
ant \
antlr \
apache-commons-beanutils \
apache-commons-cli \
apache-commons-codec \
apache-commons-collections \
apache-commons-compress \
apache-commons-io \
apache-commons-jxpath \
apache-commons-lang3 \
apache-commons-logging \
apache-commons-net \
apache-commons-parent \
apache-parent \
apache-resource-bundles \
apiguardian \
aqute-bnd \
assertj-core \
atinject \
bcel \
beust-jcommander \
bsf \
byte-buddy \
cdi-api \
cglib \
easymock \
extra-enforcer-rules \
felix-parent \
felix-utils \
fusesource-pom \
google-guice \
guava \
hamcrest \
httpcomponents-client \
httpcomponents-core \
httpcomponents-project \
jakarta-activation \
jakarta-annotations \
jakarta-mail \
jakarta-oro \
jakarta-servlet \
jansi \
java_cup \
javapackages-bootstrap \
javapackages-tools \
jdepend \
jdom \
jdom2 \
jflex \
jsch \
jsr-305 \
junit \
junit5 \
jzlib \
maven \
maven-antrun-plugin \
maven-archiver \
maven-artifact-transfer \
maven-assembly-plugin \
maven-common-artifact-filters \
maven-compiler-plugin \
maven-dependency-analyzer \
maven-dependency-plugin \
maven-dependency-tree \
maven-enforcer \
maven-file-management \
maven-filtering \
maven-jar-plugin \
maven-parent \
maven-plugin-build-helper \
maven-plugin-bundle \
maven-plugin-testing \
maven-plugin-tools \
maven-remote-resources-plugin \
maven-resolver \
maven-resources-plugin \
maven-shared-incremental \
maven-shared-io \
maven-shared-utils \
maven-source-plugin \
maven-surefire \
maven-verifier \
maven-wagon \
mockito \
modello \
mojo-parent \
munge-maven-plugin \
objectweb-asm \
objenesis \
opentest4j \
osgi-annotation \
osgi-compendium \
osgi-core \
plexus-archiver \
plexus-build-api \
plexus-cipher \
plexus-classworlds \
plexus-compiler \
plexus-components-pom \
plexus-containers \
plexus-interpolation \
plexus-io \
plexus-languages \
plexus-pom \
plexus-resources \
plexus-sec-dispatcher \
plexus-utils \
qdox \
regexp \
sisu \
sisu-mojos \
slf4j \
testng \
univocity-parsers \
velocity \
xalan-j2 \
xbean \
xerces-j2 \
xml-commons-apis \
xml-commons-resolver \
xmlunit \
xmvn \
xz-java \
)

.PHONY: all

all: $(components)
$(TARGET):
	@mkdir $@
$(components): | $(TARGET)
	@echo $@
	@cd $(@D) && "$${OLDPWD}/prepare_ci.sh" $(@F) f37 || rm -rf $(@F)
