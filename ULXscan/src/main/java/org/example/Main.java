package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    static Dotenv dotenv = Dotenv.configure().load();
    static String BASE_URL = "https://ulxscan.com/nft/staking-hub/id/";
    // PTPIF - path to package Id's file
    static String PTPIF = dotenv.get("PTPIF");
    static int WAIT = 10;
    static String RESULT_BASE_DIRECTORY = dotenv.get("RESULT_BASE_DIRECTORY");
    static String METADATA_GRID_FILE_NAME = "MetaData.json";
    static String NEW_METADATA_GRID_FILE_NAME = "New_MetaData.json";
    static String METADATA_GRID_SCREEN = "Screen.png";
    // in case we have capvalue is not 0
    static String NORMAL_DATA_XPATH = ".//div[@class='tooltip pos-relative tooltipRight']";
    // in case capvalue = 0
    static String WRONG_DATA_XPATH = ".//div[@class='break-word break-word--bold']";
    // we found main div for 14 elements with data inside, data can't be
    //NORMAL_DATA_XPATH or WRONG_DATA_XPATH
    static String DATA_XPATH = ".//div[@class='row no-collapse']";
    static int CURRENT_STAKE_INDEX = 2;
    static int TOTAL_REWARDS_INDEX = 10;
    static int LOCKED_AMOUNT_INDEX = 7;
    static int CAP_VALUE_INDEX = 6;
    public static List<String> packageIdsReader() throws IOException {
        //Reads all lines from the file given path
        return Files.readAllLines(Paths.get(PTPIF));
    }
    public static String[] jsonFileReader(String filePath) throws IOException, ParseException {
        //here we are reading json file from filePath
        //from this jsonFile we need 3 parameters Current Stake, Total Rewards, Locked Amount
        JSONParser parser = new JSONParser();
        Reader reader = new FileReader(filePath);
        Object jsonObj = parser.parse(reader);
        JSONObject jsonObject = (JSONObject) jsonObj;
        String currentStake = jsonObject.get("current_stake").toString();
        String totalRewards = jsonObject.get("total_rewards").toString();
        String lockedAmount = jsonObject.get("locked_amount").toString();
        String capValue = "";
        try{
            capValue = jsonObject.get("cap_value").toString();
        }catch(NullPointerException e){
            System.out.println("no cap value");
        }
        return new String[] {
                currentStake,
                totalRewards,
                lockedAmount,
                capValue,
        };
    }
    public static ArrayList<String> dataParser(List<WebElement> foundedElements, int[] dataIndexes) {
        ArrayList<String> resultArray = new ArrayList<String>();
        for (int i=0; i<dataIndexes.length; i++){
            int currentDataIndex = dataIndexes[i];
            WebElement elem = foundedElements.get(currentDataIndex);
            List<WebElement> normal = elem.findElements(By.xpath(NORMAL_DATA_XPATH));
            List<WebElement> wrong = elem.findElements(By.xpath(WRONG_DATA_XPATH));
            if(normal.size() != 0){
                resultArray.add(normal.get(0).getText().replace(".",","));
                continue;
            } else if (wrong.size() != 0) {
                resultArray.add("0 USDT");
                continue;
            }
        }
        return resultArray;
    }
    public static String[] getUlxMetadataByPackageId(String packageId) throws IOException {
        /** base ULX URL is needed, to provide package Id in to the placeholder
         */
        String targetURL = BASE_URL + packageId;
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("headless");
        opt.addArguments("--remote-allow-origins=*");
        System.setProperty("webdriver.chrome.driver","/Users/anna/chromedriver-mac-arm64/chromedriver");
        WebDriver driver = new ChromeDriver(opt);
        driver.get(targetURL);
        driver.manage().window().maximize();
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript("document.body.style.zoom='50%'");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(WAIT));
        LocalDate today = LocalDate.now();
        Path pth = Paths.get(RESULT_BASE_DIRECTORY+packageId+"/"+String.valueOf(today)+"/"+METADATA_GRID_SCREEN);
        Files.createDirectories(pth.getParent());
        //get X-path locators for Current Stake, Total Rewards, Locked Amount
        //X-path locator is .//div[@class='tooltip pos-relative tooltipRight']
        //From this locator you will get List of items
        //We need items (1,3,6) indexes of this items [0,2,5]
        List<WebElement> elements  = driver.findElements(By.xpath(DATA_XPATH));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(WAIT));
        System.out.println(elements.size());
        System.out.println(packageId);
        int[] dataIndexInElements = {
                CURRENT_STAKE_INDEX,
                TOTAL_REWARDS_INDEX,
                LOCKED_AMOUNT_INDEX,
                CAP_VALUE_INDEX,
        };
        ArrayList<String> foundedElems = dataParser(elements, dataIndexInElements);
//        String currentStake = elements.get(CURRENT_STAKE_INDEX).getText();
//        String capValue = "";
//        String lockedAmount = elements.get(LOCKED_AMOUNT_INDEX).getText();
//        //total rew as empty variable ""(string) because we have two ways to get this element
//        String totalRewards = "";
//        switch (elements.size()) {
//            case 6:
//                totalRewards = elements.get(5).getText();
//                capValue = elements.get(1).getText();
//                break;
//            case 5:
//                capValue = "0 USDT";
//                totalRewards = elements.get(4).getText();
//                break;
//            default:
//                System.out.println("unknown elements count "+elements.size());
//        }
        takeScreenshot(
                RESULT_BASE_DIRECTORY+packageId+"/"+String.valueOf(today)+"/"+METADATA_GRID_SCREEN,
                driver);
        driver.close();
//        return new String[] {

//                currentStake.replace(".",","),
//                totalRewards.replace(".",","),
//                lockedAmount.replace(".",","),
//                capValue.replace(".",","),
//        };
        return new String[]{
                foundedElems.get(0),
                foundedElems.get(1),
                foundedElems.get(2),
                foundedElems.get(3),
        };
    }
    public static boolean pathExist(String location){
        File file = new File(location);
        return file.exists();
    }
    public static boolean dataCompare(String pkgId, String[] pkgMetaData) throws IOException, ParseException {
        //we need to check if the folder with package number exist
        // check if the previous date folder exist
        // if folder with number doesn't exist, previous date check is unnecessary
        // if folder with number exist and previous date exist, then compare metadata
        if(pathExist(RESULT_BASE_DIRECTORY + pkgId) == true){
            LocalDate today = LocalDate.now();
            String previousLaunch = String.valueOf(today.minusDays(1));
            if(pathExist(RESULT_BASE_DIRECTORY+pkgId+"/"+previousLaunch)==true){
                //here we need to read data from MetaData.json
                //and compare it with pkgMetaData
                //if data has been changed, then create folder with data(variable now)
                //here we place new file New_MetaData.json and screenshot New_Screen.jpg
                String[] possibleMetaDataArrayPath = {
                        RESULT_BASE_DIRECTORY+pkgId+"/"+previousLaunch+"/"+METADATA_GRID_FILE_NAME,
                        RESULT_BASE_DIRECTORY+pkgId+"/"+previousLaunch+"/"+NEW_METADATA_GRID_FILE_NAME,
                };
                String[] previousLaunchMetaData = packageSavedDataReader(possibleMetaDataArrayPath);
                //next step we compare data pkgMetaData,previousLaunchMetaData
                boolean isNew = stringCompare(pkgMetaData,previousLaunchMetaData, RESULT_BASE_DIRECTORY+pkgId+"/"
                        +String.valueOf(today)+"/",pkgId);
                //create method which except pkgMetaData,previousLaunchMetaData
                return isNew;
            } else {
                writeMetaData(RESULT_BASE_DIRECTORY+pkgId+"/"+String.valueOf(today)+"/"
                        + NEW_METADATA_GRID_FILE_NAME,pkgMetaData,pkgId);
                return true;
            }
        } else {
            writeMetaData(RESULT_BASE_DIRECTORY+pkgId+"/"+String.valueOf(LocalDate.now())+"/"
                    + NEW_METADATA_GRID_FILE_NAME,pkgMetaData,pkgId);
            return true;
        }
    }
    public static String[] packageSavedDataReader(String[] pathToMetaData) throws IOException, ParseException {
        //here we need read the JSON file from previous day or last checked time
        //pathToMetaData - we have path old and new metadata - possibleMetaDataArrayPath!!!!
        String[] fileData = new String[3];
        for(int i =0; i< pathToMetaData.length; i++){
            //check for each index pathToMetaData exists
            //if one path has been found then we're reading this file
            //from this file returns array of 3 values - Current Stake, Total Rewards, Locked Amount
            Boolean exists = pathExist(pathToMetaData[i]);
            if(exists == true) {
                fileData = jsonFileReader(pathToMetaData[i]);
                //return data from jsonFile
            }
        }
        return fileData;
    }
    public static boolean stringCompare(String[] todayMetaData, String[] yesterdayMetaData, String filePath, String pkgId) throws IOException {
        //logic for string values comparing
        /**
         * this is how to look todayMetaData and yesterdayMetaData
         * [
         * Current Stake, --> 0 if i=0 then i<3 then add 1 to i and as result i=1
         * Total Rewards, --> 1 if i=1 then i<3 then add 1 to i and as result i=2
         * Locked Amount, --> 2 if i=2 then i<3 then add 1 to i and as result i=3
         * ] if i=3 then i=3 then we not add 1 to i because condition i<3 false and for cycle (loop) is finished
         * for(int i =0; i< 3; i+1){}
         */
        boolean notChanged = true;
        for(int i = 0; i< todayMetaData.length; i++){
            if(!todayMetaData[i].equals(yesterdayMetaData[i])){
                //here make change true on false, and false on true using "!"
                // because we need true to get in to cycle (when todaymeta and yestmeta are not the same)
                // for data record
                // if some of Current Stake, Total Rewards, Locked Amount in todayMetaData different with -->
                // Current Stake, Total Rewards, Locked Amount in yesterdayMetaData should be created file with name
                // New_MetaData.json eg: "/Users/anna/ULXscan_NFT_Packages_results/4709/13-07-2023/New_MetaData.json"
                writeMetaData(filePath + NEW_METADATA_GRID_FILE_NAME,todayMetaData,pkgId);
                notChanged = false;
                //here we should send sms to telegram-bot
                return true;
            }
        }
        if(notChanged==true){
            writeMetaData(filePath + METADATA_GRID_FILE_NAME,todayMetaData,pkgId);
            return false;
        }
        return true;
    }
    public static void writeMetaData(String metaDataLocation, String[] data, String pkgId) throws IOException, FileAlreadyExistsException {
        /**if smth change in metadata isNew should be true and we add New_ prefix
         */
        Path pth = Paths.get(metaDataLocation);
        Files.createDirectories(pth.getParent());
        try {
            Files.createFile(pth);
            JSONObject obj = new JSONObject();
            obj.put("current_stake", data[0]);
            obj.put("total_rewards", data[1]);
            obj.put("locked_amount", data[2]);
            obj.put("link", BASE_URL+pkgId);
            obj.put("cap_value", data[3]);
            FileWriter file = new FileWriter(metaDataLocation);
            file.write(obj.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void takeScreenshot(String screenshotLocation, WebDriver drv ) throws IOException {
        /**add prefix New_ if isNew true
         */
        File src = ((TakesScreenshot) drv).getScreenshotAs(OutputType.FILE);
        Path destination = Paths.get(screenshotLocation);
        Files.move(src.toPath(), destination, REPLACE_EXISTING);
    }
    public static void sendToBot(String msg){
        //will be logic related to send all new files locations in to telegram Bot
        String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

        //Add Telegram token (given Token is fake)
        String apiToken = "6526805020:AAEv2siaw3pwT1W3yYAxLg8Gomo2_Rp3Jrs";

        //Add chatId (given chatId is fake)
        String chatId = "214462038";

        urlString = String.format(urlString, apiToken, chatId, msg);

        try {
            URL url = new URL(urlString);
            System.out.println(url);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean TodayDataExists(String[] pathVariants){
        for(int i = 0; i < pathVariants.length; i++){
            boolean exist = pathExist(pathVariants[i]);
            if(exist){
                System.out.println("file is already exist "+pathVariants[i]);
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) throws IOException, ParseException {
        List<String> ListIds = packageIdsReader();
        String msg = String.valueOf(LocalDate.now()) + "||";
        boolean isNew = false;
        for(String packageId:ListIds) {
            if (packageId != "") {
                String[] possiblePath = {
                        RESULT_BASE_DIRECTORY + packageId + "/" + String.valueOf(LocalDate.now()) + "/" + METADATA_GRID_FILE_NAME,
                        RESULT_BASE_DIRECTORY + packageId + "/" + String.valueOf(LocalDate.now()) + "/" + NEW_METADATA_GRID_FILE_NAME,
                };
                if (!TodayDataExists(possiblePath)) {
                    String[] packageMetaData = getUlxMetadataByPackageId(packageId);
                    isNew = dataCompare(packageId, packageMetaData);
                    if (isNew) {
                        msg += packageId + ".";
                    }
                }
            }
        }
        sendToBot(msg);
    }
}
