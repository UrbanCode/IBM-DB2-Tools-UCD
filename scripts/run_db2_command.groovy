import com.urbancode.air.CommandHelper;
import com.urbancode.air.AirPluginTool;

def apTool = new AirPluginTool(this.args[0], this.args[1])
def props = apTool.getStepProperties();

def commandBody = props['commandBody'] != "" ? props['commandBody'] : null
def commandFile = props['commandFile'] != "" ? props['commandFile'] : null
def commandArgs = props['commandArgs'] != "" ? props['commandArgs'] : null
def continueOnFail = props['continueOnFail'] != "" ? props['continueOnFail'] : null

def dbinstance = props['dbinstance'] != "" ? props['dbinstance'] : null


final File PLUGIN_HOME = new File(System.getenv().get("PLUGIN_HOME"))

if (commandBody == null && commandFile == null) {
    println "You must provide either a Script Body or a Script File"
    System.exit(1)
}

if (commandBody != null && commandFile != null) {
    println "You must provide either a Script Body or a Script File, not both"
    System.exit(1)
}

def String getArch() {
    String result
    String arch = System.getProperty("os.arch").toLowerCase(Locale.US)

    if (arch.indexOf("amd64") > -1 || arch.indexOf("x64") > -1 || arch.indexOf("x86_64") > -1) {
        result = "x64"
    }
    else if (arch.indexOf("x86") > -1 || arch.indexOf("386") > -1 || arch.indexOf("486") > -1 ||
             arch.indexOf("586") > -1 || arch.indexOf("686") > -1 || arch.indexOf("pentium") > -1) {
        result = "x86"
    }
    else {
        result = "unknown"
    }
             
    return result
}

def arch = getArch()
def libraryPath = new File(PLUGIN_HOME, "lib/native/${arch}/WinAPI.dll")
System.setProperty("com.urbancode.winapi.WinAPI.dllPath", libraryPath.absolutePath)

def commandPath = ""

if (commandBody != null) {
    def commandData = File.createTempFile("tmp",".ps1")
    commandData.deleteOnExit()
    commandData.createNewFile()
    commandData.write("")
    commandData.write(commandBody)
    commandPath = commandData.getAbsolutePath()
}
else {
    commandPath = commandFile
}

def db2Commands = new File(commandPath)
db2Commands.eachLine 
{	db2Command ->
	println();
	println("=================================================================================================");
	println("DB2 Command : " + db2Command);
	println("DB2 Args    : " + commandArgs);
	println("DB2 Instance: " + dbinstance);

	if (commandArgs != null) {	
		def commandArgsList = commandArgs.split()
		def k = 0
		while (k < commandArgsList.size()) {	
			def searchString = "{" + (k + 1) + "}"
			def replaceString = commandArgsList[k]
			
			db2Command = db2Command.replace(searchString, replaceString)
			k++
		}
	}
	
	println("DB2 Command (resolved): " + db2Command);
	
	execString = ""
	if (dbinstance != null && dbinstance != "")
	{
		execString = "cmd /c set DB2INSTANCE=$dbinstance & db2cmd /c /w /i $db2Command";
		//execString = "cmd /c set DB2INSTANCE=$dbinstance & db2cmd db2 -f $commandPath";
	}
	else
	{
		execString = "db2cmd /c /w /i $db2Command";
	}
	
	Process proc = execString.execute();
	proc.waitFor();

	println("${proc.getText()}")
	println("command exit code: ${proc.exitValue()}")

	exitCode = proc.exitValue()

	if (exitCode != 0) {
		if (!continueOnFail)
		{
			println "Failed to execute command."
			System.exit(exitCode)
		}
		else
		{
			println "Command failed however processing will continue."
		}
	}
	else {
		println "Command executed successfully."
	}
}