@echo off
if not exist out mkdir out
javac -d out src\*.java
java -cp "out;lib\sqlite-jdbc.jar" Main
pause
