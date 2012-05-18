--***********************************************************************
--
--  University of South Florida
--  Student Information System
--  Program Unit Information
--
--  General Information
--  -------------------
--  Program Unit Name  : harv_import
--  Process Associated : Subversion 
--  Object Source File Location and Name : dbgrsyn\harv_import_grants.sql
--  Business Logic : 
--   This script creates required public synonym and grants for
--   the package harv_import.
--
--
-- Audit Trail (in descending date order)
-- --------------------------------------  
--  Version  Issue      Date         User         Reason For Change
--  -------  ---------  -----------  --------     -----------------------
--     1     OASADMS-2  5/18/2012       VBANGALO     Initial Creation 
--   
--************************************************************************

DROP PUBLIC SYNONYM harv_import;
CREATE PUBLIC SYNONYM harv_import FOR martian.harv_import;
