# widequery-example-repo


This is a simple test example showcasing the usage of WideTable and WideQuery.

 Step1: createTable and inject a KeyValueStore service

 Step2: configure SelectQuery Templates for this table

 Step3: inject Rows Into Table

 Step4: run one or more of the preconfigured Queries

--------------------------------------------------------------------------------

Prerequisites:

If you are using maven, please add the following to maven settings.xml

    <activeProfiles>
        <activeProfile>github</activeProfile>
    </activeProfiles>

    <profiles>
        <profile>
            <id>github</id>
            <repositories>
                <repository>
                    <id>central</id>
                    <url>https://repo1.maven.org/maven2</url>
                </repository>
                <repository>
                    <id>github</id>
                    <name>GitHub widequery Apache Maven Packages</name>
                    <url>https://maven.pkg.github.com/widequery/widequery-repo</url>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>

    <servers>
        <server>
            <id>github</id>
            <username><YOUR GIT USER NAME></username>
            <password><YOUR GIT TOKEN></password>
        </server>
    </servers>