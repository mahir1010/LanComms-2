set /p ip="Enter IP for FileServer: "
java -cp ..\;"..\sql.jar" lanComms.server.main.Server %ip%