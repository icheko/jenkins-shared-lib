/*
This function parses variables defined in a commit message.
For example, a commit message like:

Merge in latest code /env:staging /h:heroku-app-stage

Usage:

env.ENVIRONMENT = readCommitVar('env')
env.HEROKU_APP = readCommitVar('h')

Additionally, this method takes in an optional second parameter.

When defined, the function will return the value from the specified job parameter
if the commit message does not define a value. This is useful when you want to define
values for the job via params or commit messages.

env.ENVIRONMENT = readCommitVar('env', 'Environment')

Of course this would mean using an environment variable instead of referencing
the params directly.
*/
def call(String commitVariableName, String paramVariableName = "null") {
    String message = sh (script: 'git log -1 --pretty=%B', returnStdout: true).trim()
    def words = message.split(/\s/)

    for (String word : words) {
        if(word.startsWith("/") || word.startsWith("\\")){
            pv = word.substring(1)
            def param, value

            if(pv.contains(":")){
                (param, value) = pv.split(":")
            }else{
                param = pv
            }

            if(param.equalsIgnoreCase(commitVariableName)){
                println "Found var '${commitVariableName}' with value '${value}' in commit message"
                // case 1: \command
                // case 2: \command:t
                if(!param.equals('') && ( value.equals(null) || value.equals('t') ) ){
                    println "Returning true"
                    return 'true'
                }
                // case 3: \command:anyvalue
                if(value != ""){
                    println "Returning value from commit message"
                    return value
                }
            }
        }
    }

    // case 4: variable not found in commmit message, return job param
    if(!(paramVariableName.equals("null") || paramVariableName.equals(""))){
        println "Returning value from job parameter"
        return params."${paramVariableName}"
    }
}