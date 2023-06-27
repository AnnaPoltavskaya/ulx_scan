package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    static String BASE_URL = "https://ulxscan.com/nft/staking-hub/id/";
    // PTPIF - path to package Id's file
    static String PTPIF = "/Users/anna/QA/ulx_scan/ULXscan/package_ids.txt";
    public static void packageIdsReader() throws IOException {
        //Create new row create empty array of strings (ID's)
        List<String> listOfPackageIds = new ArrayList<String>();
        //Reads all lines from the file given path
        listOfPackageIds = Files.readAllLines(Paths.get(PTPIF));
        for (String eachString :listOfPackageIds ) {
            System.out.println(eachString);
        }
    }
    public void getUlxMetadataByPackageId(String packageId){
        /** base ULX URL is needed, to provide package Id in to the placeholder
         */
    }
    public void dataCompare(){
        //think about input data
    }
    public void packageSavedDataReader(String packageId, String dateMark){
        //here we need read the JSON file from previous day or last checked time
    }
    public void stringCompare(String first, String second){
        //logic for string values comparing
    }
    public void writeMetaData(String metaDataLocation, Map data, Boolean isNew){
        /**if smth change in meta data isNew should be true and we add New_ prefix
         */
    }
    public void takeScreenshot(String screenshotLocation, Boolean isNew){
        /**add prefix New_ if isNew true
         */
    }
    public void checkNewData(String dateTimeMark){
        //should check all packages from some time mark for the new data,
        // still make screenshoot if there no changes (we have bugs)
    }
    public void sendToBot(String data){
        //will be logic realted to send all new files locations in to telegram Bot
    }
    public void createFolder(String path, String folderName, Boolean shouldCreate){
        /**returns string of path + folderName
         */
    }

    public static void main(String[] args ) throws IOException {
        packageIdsReader();
    }
}
