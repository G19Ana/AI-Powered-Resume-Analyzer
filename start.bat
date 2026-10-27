@echo off
start "Resume Analyser - Backend" cmd /k "cd /d "%~dp0backend" && "%USERPROFILE%\maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run"
start "Resume Analyser - Frontend" cmd /k "cd /d "%~dp0frontend" && npm start"
timeout /t 6 /nobreak >nul
start http://localhost:4200
