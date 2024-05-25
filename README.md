# Allgemeines



# Testat 01 

1. PostgreSQL DB aus dem ![Compose-File](docker-compose.yaml) verwenden (dort sind User+PW und DB-Name angegeben) oder eigene DB Instanz hochfahren.
2. Nach dem Start der Datenbank das SQL-Skript zum Anlegen der Datenbanktabellen ausführen (![create_tables.sql](sql/create_tables.sql)) + PSQL Function und Trigger (![create_function_trigger.sql](sql/functions_triggers.sql))
3. Der Pfad zu den Dateien mit den initialen Daten sind harcoded und müssen für das Ausführen des Codes 
  außerhalb der IDE mittels Umgebungsvariablen überschrieben werden (DATA_DRESDEN_PATH, DATA_LEIPZIG_PATH, DATA_REVIEWS_PATH).
  Dasselbe gilt für die PostgreSQL Konfiguration (DATABASE_URL, DATABASE_USER, DATABASE_PW)
4. Zum Ausführen der Jar-Datei wird eine Java Installation in der Version >= 17 benötigt. 
   Der benötigte Datenbanktreiber (postgresql-42.7.3.jar) ist mit in den Dependencies enthalten (/lib)
5. Ausführen der Executable JAR:

```bash 
java -DDATA_DRESDEN_PATH=<> \
  -DDATA_LEIPZIG_PATH=<> \
  -DDATA_REVIEWS_PATH=<> \
  -DDATABASE_URL=<> \
  -DDATABASE_USER=<> \
  -DDATABASE_PW=<> \
  -DDATA_CATEGORIES_PATH=<> \
  -DLOG_OUTPUT_FILE=<> \
  -DSILENT_MODE=<> \
  -jar out/artifacts/testat_01_parser_jar/testat-01-parser.jar
```

6. Das Programm liest die Daten aus den Dateien mit entsprechenden Error-Checks ein und schreibt diese in die Datenbank.
   Auftretende Fehler werden in einer eigenen Tabelle abgespeichert (ebenfalls Teil des ER-Diagramms).