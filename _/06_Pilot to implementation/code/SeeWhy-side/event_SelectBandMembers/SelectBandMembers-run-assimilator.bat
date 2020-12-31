@echo off

set DIR_HOME=%CD%

cd ..

call run-assimilator event_SelectBandMembers\SelectBandMembers_Assimilator.properties

cd %DIR_HOME%

