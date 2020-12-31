@echo off

set DIR_HOME=%CD%

cd ..

call run-eventinterface event_SelectBandMembers\SelectBandMembers_EventDataFeed.properties

cd %DIR_HOME%
