CREATE OR REPLACE PACKAGE MARTIAN.harv_import IS

   -- Author  : WHURLEY
   -- Created : 6/14/2011 12:10:08 PM
   -- Purpose : import harvest packages

   
PROCEDURE p_compress(pv_version_i NUMBER) ;
FUNCTION f_get_blob(pv_version_i NUMBER) RETURN BLOB;		
END harv_import;
/
CREATE OR REPLACE PACKAGE BODY MARTIAN.harv_import IS

   
	 
	 PROCEDURE p_compress(pv_version_i NUMBER) IS
	 BEGIN
		 UPDATE  martian.harvest_objects a
                  SET a.version_data = utl_compress.lz_compress(src => a.version_data)
                WHERE a.version_id = pv_version_i;
		 COMMIT;
	 END;

   

   
FUNCTION f_get_blob(pv_version_i NUMBER) RETURN BLOB IS
   
      /* local variables */
      lv_blob BLOB;
   
   BEGIN
      BEGIN
         /* get the uncompressed blob from the table via the version input parameter */
         SELECT utl_compress.lz_uncompress(a.version_data)
           INTO lv_blob
           FROM martian.harvest_objects a
          WHERE a.version_id = pv_version_i;
      EXCEPTION
         /* return a null blob when there is no row for the version id */
         WHEN no_data_found THEN
            lv_blob := NULL;
      END;
   
      RETURN lv_blob;
   
   END f_get_blob;
	 
	 	 
END harv_import;
/
