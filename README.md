# appdparser

Simple excel parser with information collected from App Dynamics to identify application dependencies.

## Buidling and Running

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--inputfile=<FILE>.xlsx"
```

OR

```bash
mvn clean package -DSkipTests
java -jar target/appdparser-0.0.1-SNAPSHOT.jar --inputfile <FILE>.xlsx
```

The output will be a CSV file with a list of the dependencies and a count. The file will have a suffix of **_output.csv**
