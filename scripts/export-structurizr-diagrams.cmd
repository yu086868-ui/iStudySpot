@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0export-structurizr-diagrams.ps1" %*
