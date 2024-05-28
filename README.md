# Allgemeines

# Testat 01 

1. PostgreSQL DB aus dem ![Compose-File](docker-compose.yaml) verwenden (dort sind User+PW und DB-Name angegeben) oder eigene DB Instanz hochfahren.
2. Nach dem Start der Datenbank das SQL-Skript zum Anlegen der Datenbanktabellen ausführen (![create_tables.sql](sql/create_tables.sql)) + PSQL Function und Trigger (![function_trigger.sql](sql/functions_triggers.sql))
3. Die Umgebungsvariablen müssen für das Ausführen des Ladeporgramms gesetzt werden (DATA_DRESDEN_PATH, DATA_LEIPZIG_PATH, DATA_REVIEWS_PATH, DATA_CATEGORIES_PATH).
  Dasselbe gilt für die PostgreSQL Konfiguration (DATABASE_URL, DATABASE_USER, DATABASE_PW)
4. Zum Ausführen der Jar-Datei wird eine Java Installation in der Version >= 17 benötigt. 
   Der benötigte Datenbanktreiber (postgresql-42.7.3.jar) ist mit in den Dependencies enthalten
5. Ausführen der Executable JAR:

UNIX: 
```bash 
export DATA_DRESDEN_PATH=dbpraktikum-mediastore/data/dresden.xml
export DATA_LEIPZIG_PATH=dbpraktikum-mediastore/data/leipzig_transformed.xml
export DATA_REVIEWS_PATH=dbpraktikum-mediastore/data/reviews.csv
export DATABASE_URL=jdbc:postgresql://localhost:5432/postgres
export DATABASE_USER=postgres
export DATABASE_PW=postgres
export DATA_CATEGORIES_PATH=dbpraktikum-mediastore/data/categories.xml
export LOG_OUTPUT_FILE=testat-01-parser/logs/error.csv
export SILENT_MODE=false

java -jar out/artifacts/dbprak_sose24_testat01_jar/dbprak-sose24-testat01.jar
```

Windows:
```cmd
set DATA_DRESDEN_PATH=dbpraktikum-mediastore/data/dresden.xml
set DATA_LEIPZIG_PATH=dbpraktikum-mediastore/data/leipzig_transformed.xml
set DATA_REVIEWS_PATH=dbpraktikum-mediastore/data/reviews.csv
set DATABASE_URL=jdbc:postgresql://localhost:5432/postgres
set DATABASE_USER=postgres
set DATABASE_PW=postgres
set DATA_CATEGORIES_PATH=dbpraktikum-mediastore/data/categories.xml
set LOG_OUTPUT_FILE=testat-01-parser/logs/error.csv
set SILENT_MODE=false

java -jar out/artifacts/dbprak_sose24_testat01_jar/dbprak-sose24-testat01.jar
```

6. Das Programm liest die Daten aus den Dateien mit entsprechenden Error-Checks ein und schreibt diese in die Datenbank.
   Auftretende Fehler werden in einer CSV Datei nach dem Schema ENTITY,ATTRIBUTE,ERROR_MESSAGE gespeichert.
