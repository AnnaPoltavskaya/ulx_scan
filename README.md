# ulx_scan
ULX staking Hub NFT Metadata - This app collects NFT metadata from the source "Ultron Explorer" which tracks daily rewards for staking NFT packages.
Ultron Explorer - website which allows you to explore and search the Ultron blockchain for transactions, addresses, blocks, and NFT Metadata.
This app does:
1. Goes to the website Ultron explorer
2. Collects NFT metadata of the specific packages, writes in to the JSON Object
3. Records a page screenshot
4. Saves collected metadata and screenshots into a previously created folder with the current date on a local repository.
5. Iside project folder should be created file with name "package_ids.txt" inside this file each package ID it is new string
as eg: 
   1 package ID
   2 package ID
6. Inside project folder should be created ".env" file with next variable inside:
   PTPIF=/Users/username/folder/to/project/package_ids.txt
   RESULT_BASE_DIRECTORY=/Users/username/folder/to/project_results/ULXscan_NFT_Packages_results/
   CHROME_DRIVER_LOCATION=/Users/username/chromedriver/chromedriver
For now, you can launch this app manually using Intellij Idea, but future updates will allow the app to launch automatically using Crontab.