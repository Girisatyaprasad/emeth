$shell = New-Object -ComObject Shell.Application
$destFolder = $shell.NameSpace('C:\Users\apple\CrossDevice\realme P4 Power 5G (2)\storage\dev\Air OS')
$destFolder.CopyHere('C:\Users\apple\OneDrive\Documents\Air OS\emeth\android-project\emeth.apk', 0x14)
