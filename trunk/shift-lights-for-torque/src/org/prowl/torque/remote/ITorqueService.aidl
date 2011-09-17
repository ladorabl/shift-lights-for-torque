package org.prowl.torque.remote;

interface ITorqueService {

   /**
    * Get the API version (currently 1)
    */
   int getVersion();

   /**
    * Get the most recent value stored for the given PID.  This will return immediately whether or not data exists.
    * @param triggersDataRefresh Cause the data to be re-requested from the ECU
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   float getValueForPid(long pid, boolean triggersDataRefresh);

   /**
    * Get a textual, long description regarding the PID, already translated (when translation is implemented)
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   String getDescriptionForPid(long pid);
   
   /**
    * Get the shortname of the PID
    * @note If the PID returns multiple values, then this call will return the data for the first matching PID
    */
   String getShortNameForPid(long pid);

   /**
    * Get the Si unit in string form for the PID, if no Si unit is available, a textual-description is returned instead.
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   String getUnitForPid(long pid);
   
   /**
    * Get the minimum value expected for this PID
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   float getMinValueForPid(long pid);
   
   /**
    * Get the maximum value expected for this PId
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   float getMaxValueForPid(long pid);
   
   /**
    * Returns a list of currently 'active' PIDs. This list will change often. Try not to call this method too frequently.
    *
    * PIDs are stored as hex values, so '0x01, 0x0D' (vehicle speed) is the equivalent:  Integer.parseInt("010D",16);
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   long[] getListOfActivePids();
   
   /**
    * Returns a list of PIDs that have been reported by the ECU as supported.
    *
    * PIDs are stored as hex values, so '0x01, 0x0D' (vehicle speed) is the equivalent:  Integer.parseInt("010D",16);
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   long[] getListOfECUSupportedPids();
   
   /**
    * Returns a list of all the known available sensors, active or inactive.
    * 
    * Try not to call this method too frequently.
    *
    * PIDs are stored as hex values, so '0x01, 0x0D' (vehicle speed) is the equivalent:  Integer.parseInt("010D",16);
    * @note If the PID returns multiple values(has multiple displays setup for the same PID), then this call will return the data for the first matching PID
    */
   long[] getListOfAllPids();
   
   /**
    * True if the user has granted full permissions to the plugin (to send anything that it wants)
    */
   boolean hasFullPermissions();
   
   /**
    * Send a specific request over the OBD bus and return the response as a string
    *
    * This is currently limited to only allowing the send of MODE22 commands.
    */
   String[] sendCommandGetResponse(String header, String command);

  /**
   * Given this unit, get the users preferred units
   */
   String getPreferredUnit(String unit);
   
   /**
    * Add / Update a PID from your plugin to the main app, Your PID is keyed by name.
    */
   boolean setPIDData(String name, String shortName, String unit, float max, float min, float value);
   
   /**
    * Get connection state to ECU
    * @return true if connected to the ECU, false if the app has not yet started retrieving data from the ECU 
    */
    boolean isConnectedToECU();

   /**
    * Turn on or off test mode. This is for debugging only and simulates readings to some of the sensors.
    */
   boolean setDebugTestMode(boolean activateTestMode);

   /**
    * Returns a string array containing the vehicle profile information:
    *
    *  Array:
    *     [0] Profile name
    *     [1] Engine Displacement (L)
    *     [2] Weight in kilogrammes
    *     [3] Fuel type (0 = Petrol, 1 = Diesel, 2 = E85 (Ethanol/Petrol)    *     [4] Boost adjustment
    *     [5] Max RPM setting
    *     [6] Volumetric efficiency (%)
    *     [7] Accumulated distance travelled
    */
   String[] getVehicleProfileInformation();

   /**
    * Store some information into the vehicle profile.
    *
    * @param key  Prefix the 'key' with your apps classpath (to avoid conflicts). eg: "com.company.pluginname.SOME_KEY_NAME" is nice and clear  (Don't use any characters other than A-Za-z0-9 . and _)
    * @param The value to store.
    * @param saveToFile Set this to true (on your last 'set') to commit the information to disk.
    * @return 0 if successful.
    */
    int storeInProfile(String key, String value, boolean saveToFileNow);

   /**
    * Retrieve some information from the vehicle profile.
    *
    * @param Prefix the 'key' with your apps classpath (to avoid conflicts).
    */
    String retrieveProfileData(String key);

}