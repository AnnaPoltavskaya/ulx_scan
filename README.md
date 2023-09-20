# ulx_scan
ULX staking Hub NFT Metadata - This app collects NFT metadata from the source "Ultron Explorer" which tracks daily rewards for staking NFT packages.
Ultron Explorer - website which allows you to explore and search the Ultron blockchain for transactions, addresses, blocks, and NFT Metadata.
This app does:
1. Goes to the website Ultron explorer
2. Collects NFT metadata of the specific packages, writes in to the JSON Object
3. Records a page screenshot
4. Saves collected metadata and screenshots into a previously created folder with the current date on a local repository.
For now, you can launch this app manually using Intellij Idea, but future updates will allow the app to launch automatically using Crontab.