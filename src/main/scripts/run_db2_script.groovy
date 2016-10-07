import com.urbancode.air.CommandHelper;
import com.urbancode.air.AirPluginTool;
import groovy.sql.Sql;

println("Script start");

def apTool = new AirPluginTool(this.args[0], this.args[1])
def props = apTool.getStepProperties();

def scriptBody = props['scriptBody'] != "" ? props['scriptBody'] : null
def scriptFile = props['scriptFile'] != "" ? props['scriptFile'] : null
def continueOnFail = props['continueOnFail'] != "" ? props['continueOnFail'] : null

def dbuser = props['dbuser'] != "" ? props['dbuser'] : null
def dbpassword = props['dbpassword'] != "" ? props['dbpassword'] : null
def dbhost = props['dbhost'] != "" ? props['dbhost'] : null
def dbport = props['dbport'] != "" ? props['dbport'] : null
def dbname = props['dbname'] != "" ? props['dbname'] : null
def dbjar = props['dbjar'] != "" ? props['dbjar'] : null

if (scriptBody == null && scriptFile == null) {
    println "You must provide either a Script Body or a Script File"
    System.exit(1)
}

if (scriptBody != null && scriptFile != null) {
    println "You must provide either a Script Body or a Script File, not both"
    System.exit(1)
}

def commandScriptPath = ""

//create the SQL script, set it to delete when the step exits, create it and clear it just in case there was somehow some data there.
if (scriptBody != null) {
    def scriptData = File.createTempFile("tmp",".sql")
    scriptData.deleteOnExit()
    scriptData.createNewFile()
    scriptData.write("")
    scriptData.write(scriptBody)
    commandScriptPath = scriptData.getAbsolutePath()
    
}
else {
    commandScriptPath = scriptFile
}

this.class.classLoader.rootLoader.addURL(new URL("file:///" + dbjar));

def sql = Sql.newInstance("jdbc:db2://" + dbhost + ":" + dbport + "/" + dbname, dbuser, dbpassword, "com.ibm.db2.jcc.DB2Driver")

def sqlStatements = new File(commandScriptPath)
sqlStatements.eachLine 
{	sqlStatement ->
	println();
	println("=================================================================================================");
	println("Statement: " + sqlStatement);
	
	try
	{
		sql.execute(sqlStatement)
	}
	catch (e)
	{
		if (!continueOnFail)
		{
			println();
			println("=================================================================================================");
			println "Statement failed."
			System.exit(-1)
		}
		else
		{
			println();
			println("=================================================================================================");
			println "Statement failed however processing will continue..."
		}
	}
}

println();
println("=================================================================================================");
println "Script executed successfully."
System.exit(0)