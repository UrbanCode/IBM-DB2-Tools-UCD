/**
 *  © Copyright IBM Corporation 2014, 2016.
 *  This is licensed under the following license.
 *  The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 *  U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import com.urbancode.air.CommandHelper;
import com.urbancode.air.AirPluginTool;
import com.urbancode.shell.Shell;
import com.urbancode.commons.util.processes.Processes

final def workDir = new File('.').canonicalFile

def apTool = new AirPluginTool(this.args[0], this.args[1])
def props = apTool.getStepProperties();

def commandBody = props['commandBody'] != "" ? props['commandBody'] : null
def commandFile = props['commandFile'] != "" ? props['commandFile'] : null
def commandArgs = props['commandArgs'] != "" ? props['commandArgs'] : null
def escapeChars = props['escapeChars'] != "" ? props['escapeChars'].split('\n') : null

final Processes processes = new Processes()
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
    def commandData = File.createTempFile("tmp",".cmd")
    commandData.deleteOnExit()
    commandData.createNewFile()
    commandData.write("")
    commandData.write(commandBody)
    commandPath = commandData.getAbsolutePath()
}
else {
    commandPath = commandFile
}

def runFile = File.createTempFile("tmp",".bat")
runFile.deleteOnExit()
runFile.createNewFile()
runFile.write("")

def ln = System.getProperty('line.separator')

def db2Commands = new File(commandPath)
db2Commands.eachLine
{	db2Command ->

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

	// Escape special DOS characters so they can be interpreted by DB2
	escapeChars.each { escapeChar ->
		db2Command = db2Command.replace(escapeChar, "^" + escapeChar)
	}

	println("DB2 Command : " + db2Command);

	runFile.append(db2Command)
	runFile.append(ln)
}

runPath = runFile.getAbsolutePath()

execString = "db2cmdadmin /c /w /i $runPath";


def shell = new Shell(execString as String)
shell.workingDirectory = workDir

println("")
println("Command line: ${execString}")
println("Working directory: ${workDir.path}")

shell.execute()
def proc = shell.process
def exitCode = -1

def hook = {
	proc.destroy()
}
Runtime.getRuntime().addShutdownHook(hook as Thread);

proc.outputStream.close()           // close process stdin
def outFuture = processes.redirectOutput(proc, System.out);
def errFuture = processes.redirectError(proc, System.err);
outFuture.await()
errFuture.await()
proc.waitFor()

Runtime.getRuntime().removeShutdownHook(hook as Thread);

// print results
println('===============================')
println("Command exit code: ${proc.exitValue()}")
println("")

exitCode = proc.exitValue()

if (exitCode != 0) {
	println "Failed to execute command."
	System.exit(exitCode)
}
else {
	println "Command executed successfully."
	System.exit(0)
}
