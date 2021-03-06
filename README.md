# javapackages-validator

This tool is used for checking existing .rpm packages against various criteria.

## Building

Requires OpenJDK 11.

Executing

	mvn package

will compile the project and generate a tarball containing the validator `.jar`
and its dependencies.

Furthermore executing

	mvn install

will unpack the tarball and make the validator `.jar` executable.

## Usage

### In case of `mvn package`

After packaging the project extract files:

	tar -xf target/assembly-${version}.tar.gz

Run the executable `.jar` file:

	java -jar ./assembly-${version}/validator-${version}.jar [OPTIONS]

### In case of `mvn install`

Simply run

	./assembly-${version}/validator-${version}.jar [OPTIONS]

## Command line options

Traditional help with `-h`, `--help`.

An option to print the configuration in XML form after being read, this is used
for debugging. Enabled by `-d`, `--dump-config`.

### Mandatory

Path to the configuration file is provided by the `-c`, `--config` flag.
More to configuration in the next sections.

The list of `.rpm` files to validate can be provided one of these ways:

* Specify the file paths directly using the `-f`, `--files` flag.

* Specify a file which names the validated files (one file path per line) by
using the `-i`, `--input` flag.

* Stream the file paths separated by new line via the standard input at
the program invocation.

	  ./generator | java -jar validator.jar

### Optional

The text output is written to the standard output or, if provided by `-o`,
`--output` into the provided file.

For colored output it is possible to set the `-r`, `--color` flag.

For more detailed output which shows which checks failed or succeeded there is
the `-v`, `--verbose` flag.

In order to print only failed checks use the `-n`, `--only-failed` flag.


## Configuration

The configuration file is an XML file.
It contains the main node `<config>`. This contains one node `<execution>` and
a variable amount of nodes of type `<rule>`. Each rule must have exactly one
`<name>` and `<match>` node.

### Execution

`<execution>` consists of recursive strucutre of `<tag>` nodes optionally
grouped in logical operators `<all>` and `<any>`.

`<tag>` refers to a group of rules which contain the same `<tag>` node. An empty
tag is equivalent to a tag containing an empty string.

The validator itself executes the rules in order specified by this node. The
execution of a `<tag>` means the execution of all the rules of that tag. It only
succeeds if all the rules succeed. The composite nodes `<all>` and `<any>` are
self-explanatory.

`<any>` has a special behaviour that it first tries to select a tag for which
at least one rule is applicable to the given `.rpm`.

Rules may / must contain the following nodes:

### Name

`<name>` is used to refer to already defined rules. Two rules cannot have the
same name.

### Tag
`<tag>` is used to group rules together into an execution as explained above.
Tag may be omitted in which case it is considered an empty string.

### Match

`<match>` is used to deterimne whether the rule is applicable to given `.rpm` file.
This is determined for each validated package.

It may consist of logical predicates, names of previous rules or `.rpm` file
attributes (such as pachage name) which are checked as regular expressions.

It may also be empty in which case it matches everything (but still must be declared).

#### Expressions

* Logical predicates: **`<not>`**, **`<and>`**, **`<or>`**.

* **`<rule>`** - Matches *exactly* the name of an existing rule.

* Expressions using regular expressions:

	* **`<name>`** - Matches according to the package name.

	* **`<arch>`** - Matches according to the package architecture.

#### Examples

* Matching any `javadoc` package.

	  <match>
	    <name>.*-javadoc.*</name>
	  </match>

* Matching any source `.rpm`.

	  <match>
	    <or>
	      <arch>src</arch>
	      <arch>nosrc</arch>
	    </or>
	  </match>

* Matching non-source package for the `x86_64` architecture, assuming previously
declared rule for matching source packages.

	  <match>
	    <and>
          <not>
            <rule>source</rule>
          </not>
	      <arch>x86_64</arch>
	    </and>
	  </match>

### Checks

Other than `<match>` a rule may contain variable amount of checks to make. These
are:

* **`<rpm-file-size-bytes>`** -
Applies the validator on the file size of the `.rpm` file in bytes.

* **`<requires>`** -
Applies the validator on each string in the `requires` section.

* **`<provides>`** -
Applies the validator on each string in the `provides` section.

* **`<java-bytecode>`** -
Applies the validator on each numeric version string of each `.class` file of
each `.jar` file contained in the `.rpm` file.

* **`<files>`** are more complex
They contain at least one `<file-rule>`.
A rule has a `<match>` which is a regular expression and determines whether this
rule should be applied to the a file by its file name.
  
  Further nodes are optional:
  
  * `<name>` Applies the inner validator to the file name
  * `<symlink>` Presence of this node requires the file type to be a symbolic
  link
  It may additionally contain an inner node `<target>` which applies its inner
  validator to the target of the smbolic link.
  * `<directory/>` Presence of this node requires the file type to be a
  directory

### Validators

There are two types of validators: aggregate and primitive.

#### Aggregate

These validators are recursively composed of other validators.

* **`<all>`** -
The validator passes if all its member validators accept the value.

* **`<any>`** -
The validator passes if any of its member validators accept the value.

* **`<none>`** -
The validator passes if none of its member validators accept the value.

#### Primitive

These simply take the string value and accept or reject it.

* **`<pass/>`** - Unconditionally passes, must have empty body.

* **`<fail/>`** - Unconditionally fails, must have empty body.

* **`<text>`** - Compares the text inside the node with the value. Passes only
on exact match.

* **`<regex>`** -
The validator applies a regular expression search to the value. The regular
expression is in the form conforming to the [java.util.regex.Pattern](
https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
)
class.
<br>
Examples:

* Accepts everything.

      <regex>.*</regex>

* **`<int-range>`** -
The validator contains two integer values separated by a dash "`-`". For
readability it is possible to separate the digits by an underscore "`_`" or by
a single quote symbol "`'`". Whitespace between digits and the dash is ignored.
<br><br>
If any of the range limits is ommited then it is substituted by
negative / positive maximum integer depending on the position within the range.
The validator expects the string value to represent an integer number as well.
It passes if the string value is between the limits specified in the range
**inclusive**.
<br>
Examples:

* Accepts any value in the inclusive range of `[25-75]`.

      <int-range>25-75</int-range>

* Accepts any representable value lesser or equal to `1000`.

      <int-range> - 1'000</int-range>
      <int-range> - 1_000</int-range>

## Configuration examples

	<config>
	  <execution>
	    <all>
          <tag>prohibiting</tag>
          <any>
            <tag>specific</tag>
            <tag>generic</tag>
          </any>
        </all>
      </execution>
      
	  <!-- Rule for source packages -->
	  <rule>
	    <name>source</name>
	    <match>
	      <or>
	        <arch>src</arch>
	        <arch>nosrc</arch>
	      </or>
	    </match>
	  </rule>
	  
	  <!-- Rule for checking the size of every non-source package -->
	  <rule>
	    <name>size</name>
	    <tag>prohibiting</tag>
	    <match>
	      <not>
	        <rule>source</rule>
	      </not>
	    </match>
	    
	    <rpm-file-size-bytes>
	      <int-range> - 15'000'000</int-range>
	    </rpm-file-size-bytes>
	  </rule>
	  
	  <rule>
	    <name>javadoc</name>
	    <tag>specific</tag>
	    <match>
	      <and>
	        <not>
	          <rule>source</rule>
	        </not>
	        <name>.*-javadoc.*</name>
	      </and>
	    </match>
	    
	    <!-- -->
	  </rule>
	  
	  <rule>
	    <name>javapackages-tools</name>
	    <tag>specific</tag>
	    <match>
	      <and>
	        <not>
	          <rule>source</rule>
	        </not>
	        <name>javapackages-tools</name>
	      </and>
	    </match>
	    
	    <!-- -->
	  </rule>
	  
	  <rule>
	    <name>byaccj</name>
	    <tag>specific</tag>
	    <match>
	      <and>
	        <not>
	          <rule>source</rule>
	        </not>
	        <name>byaccj</name>
	      </and>
	    </match>
	    
	    <!-- -->
	  </rule>
	  
	  <!-- Matches all non-source packages -->
	  <rule>
	    <name>generic</name>
	    <tag>generic</tag>
		<match>
		  <not>
	        <rule>source</rule>
		  </not>
		</match>
	    
	    <requires>
	      <any>
	        <regex>maven-local</regex>
	        <regex>maven-local-openjdk8</regex>
	        <regex>mvn\(.+:.+\)</regex>
	        <regex>rpmlib\(.+\)</regex>
	        <regex>javapackages-local</regex>
	      </any>
	    </requires>
	  </rule>
	</config>
