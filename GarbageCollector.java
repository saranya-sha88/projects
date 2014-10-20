package Project;

import java.io.*;
import java.util.*;


public class GarbageCollector{

        public static final String GB = "GB";
        public static final String MB = "MB";
        public static final String KB = "KB";
        public static final String B = "B";
        public static final String LOCAL_SCOPE_COUNT = "LOCALSCOPECOUNT";
        public static final String MARKED = "MARKED";
        public static final String UNMARKED = "UNMARKED";
        public static final String GENERATION1 = "generation1";
        public static final String ADDRESS_CHANGED = "true";
        public static final String ADDRESS_UNCHANGED = "false";
        public static final String INITIALIZATION_VALUE = "initialization_value";
        public static final String CONDITIONAL_OPERATOR = "conditional_operator";
        public static final String CONDITIONAL_VALUE = "conditional_value";
        public static final String INC_OR_DEC = "inc_or_dec";
        public static final int ZERO = 0;
        public static final int ONE = 1;
        public static final int TWO = 2;
        public static final int THREE = 3;
        public static final int FOUR = 4;
        public static final int FIVE = 5;
        public static final String CONT_VARIABLE_REGEX = "([a-zA-Z_]+)(\\w*)(\\[?\\w*\\]?)(\\s*)(={1})(\\s*)([a]{1}[l]{2}[o]{1}[c]{1})(\\({1})(\\d+)([K]?[M]?[G]?[k]?[m]?[g]?)([B|b]{1})(\\){1})(\\;{1})(.*)";
        public static final String VARIABLE_REGEX = "([a-zA-Z_]+)(\\w*)(\\[?\\w*\\]?)(\\s*)(={1})(\\s*)([a]{1}[l]{2}[o]{1}[c]{1})(\\({1})(\\d+)([K]?[M]?[G]?[k]?[m]?[g]?)([B|b]{1})(\\){1})(\\;{1})";
        public static final String FOR_LOOP_REGEX = "\\s*for\\s*(\\(+)\\s*\\w+\\s*(={1})\\s*\\d+\\s*(;{1})\\s*\\w+\\s*([<,>,>=,<=,!]{1})(={0,1})\\s*\\d+\\s*(;{1})\\s*\\w+\\s*([\\+,-]{1})([\\+,-,=]{1})(\\s*)(\\d*)(\\)+)(.*)";
        public static final String POINTER_VARIABLE_REGEX = "([a-zA-Z_]+)(\\w*)(\\.{1})([n]{1}[e]{1}[x]{1}[t]{1})(\\s*)(={1})(\\s*)([a-zA-Z_]+)(\\w*)(\\;{1})";
        public static final String VARIABLE_POINTER_FORMAT_REGEX = "([a-zA-Z_]+)(\\w*)(\\.{1})([n]{1}[e]{1}[x]{1}[t]{1})(\\s*)(={1})(\\s*)([a]{1}[l]{2}[o]{1}[c]{1})(\\({1})(\\d+)([K]?[M]?[G]?[k]?[m]?[g]?)([B|b]{1})(\\){1})(\\;{1})";
        public static int memorySize;
        public static String memoryUnit;
        public static String localScopeName;
        public static long totalMemorySize;
        public static long partitionChunkSize;
        public static int partition;
        public static int localScope=1;
        public static boolean isforLoopVariable = false;
        public static List<String> inputArray = new ArrayList<String>();
        public static Map<String,ArrayList<Long>> generationMap = new LinkedHashMap<String,ArrayList<Long>>(); 
        public static List<Map<String,ArrayList<String>>> allocationInfo = new ArrayList<Map<String,ArrayList<String>>>();
        public static Map<String,String> pointerInfoMap = new LinkedHashMap<String,String>();
        public static Map<String,String> forLoopMap = new LinkedHashMap<String,String>();
        public static Map<String[],Boolean> forLoopInput = new LinkedHashMap<String[],Boolean>();
       

public static void main(String[] args){
  try{
     GarbageCollector garbageCollector = new GarbageCollector();
     garbageCollector.readInput();
     garbageCollector.clearAllVariables();
   }
  catch(Exception exe){
   System.out.println("error occurred while parsing the data :");
 }

}

//Method used to read Input the input data
String readInput()
 {
     String input="";
     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      try{
       while((input =  br.readLine()) != null){
         inputArray.add(input);
        }
       validateInput();
       }
    catch(IOException ioe){
      System.out.println("IO error");
      System.exit(1);
     }
    return input;
  }

//Validating the input data
void validateInput(){

  int openbracket=0,closebracket=0;
  boolean endOfInput = false;
  String inputDataEntry = "";
  
  inputArray = removeComments();
  if(inputArray.size() != 0){
    for(String entry : inputArray){
      inputDataEntry = entry;
      if(!endOfInput){
       if((null != inputDataEntry) && inputDataEntry.contains("{")){
         openbracket++;
       }else if((null != inputDataEntry) && inputDataEntry.contains("}")){
         closebracket++;
       }
       if((openbracket != 0) && openbracket == closebracket){
        endOfInput = true;
       }
     }else{
        System.out.println("Invalid input data!!!");
        System.exit(1);
       }
    } 
    if(openbracket != closebracket){
     System.out.println("Invalid input data!!!");
     System.exit(1);
    }else{
     parseInputData();
    }
  }else{
   System.out.println("enter valid input");
   System.exit(1);
  }
}

//Method used to remove comments and extra spaces
ArrayList<String> removeComments(){
  String entry_trim="";
  int index;
  ArrayList<String> processedArray = new ArrayList<String>();
  for(String entry : inputArray){
    if(null != entry){
     entry_trim = entry.trim();
      if(null != entry_trim && entry_trim.contains("#")){
         index = entry_trim.indexOf("#");
          if(index > 0)
          processedArray.add(entry_trim.substring(0,index));
       }else if(null != entry_trim && !entry_trim.matches("\\s*")){
           processedArray.add(entry_trim);
       }
     }
   }
 
  return processedArray;
}

//Method used to parse the input data
void parseInputData(){

  int openBracketIndex;
  String entry;
  boolean isGlobalScope = true;
  String[] memoryDetails = new String[2];
  List<Map<String,ArrayList<String>>> inputListMap;

 if(null != inputArray && inputArray.size() > 0){
      entry=inputArray.get(0);
      inputArray.remove(entry);
      if(entry.contains("{")){
        openBracketIndex = entry.indexOf("{");
        entry = entry.substring(0,openBracketIndex);
        inputArray.add(ZERO,"{");
      }
      entry=entry.toUpperCase();
      memoryUnit = getMemoryUnit(entry);
      memoryDetails = entry.split(memoryUnit);
      if(null != memoryDetails &&  memoryDetails.length == 2){
         if(null != memoryDetails[0] && null != memoryDetails[0].trim()){
            memorySize = Integer.parseInt(memoryDetails[0].trim());}
           else{
             System.out.println("invalid input data!!!");
             System.exit(1);
            }
         if(null != memoryDetails[1] && null != memoryDetails[1].trim()){
            partition = Integer.parseInt(memoryDetails[1].trim()); }
          else{
            System.out.println("invalid input data!!!");
            System.exit(1);
          }
         System.out.println("total size of physical memory: " +memorySize+memoryUnit+ " split into "+partition+" generations");
         populateGenerationMap();
     } else{
      System.out.println("invalid input data!!!");
      System.exit(1);
     }
     inputListMap =  getInputMap(isGlobalScope,inputArray);
     memoryAllocation(inputListMap);
     }
     else{
     System.out.println("invalid input data!!!");
     System.exit(1);
     }


}

//calculating generation information
void populateGenerationMap(){
 int i;
 long startAddress,endAddress,allocatedSpace,freeSpace;
 ArrayList<Long> generationInfo;
 
 totalMemorySize = getByteEquivalent(memorySize,memoryUnit);
 partitionChunkSize = totalMemorySize/partition;

 startAddress=0;
 endAddress=partitionChunkSize-1;
 allocatedSpace=0;
 freeSpace=partitionChunkSize;

 for(i=1;i<=partition;i++){
   generationInfo = new ArrayList<Long>();
   generationMap.put("generation"+i,generationInfo);
   generationInfo.add(startAddress);
   generationInfo.add(endAddress);
   generationInfo.add(allocatedSpace);
   generationInfo.add(freeSpace);
   startAddress = endAddress + 1;
   endAddress = startAddress + partitionChunkSize  - 1;
 }

}

//Generating byte equivalent value for memory unit
long getByteEquivalent(int memorySize,String memoryUnit){

  long byteEquivalent=0;

  if(memoryUnit.equalsIgnoreCase(B)){
    byteEquivalent = Long.valueOf(memorySize);
   return byteEquivalent;
  }else if(memoryUnit.equalsIgnoreCase(KB)){
    byteEquivalent = (long)(Math.pow(2,10)*memorySize);
    return byteEquivalent;
  }else if(memoryUnit.equalsIgnoreCase(MB)){
   byteEquivalent = (long)(Math.pow(2,20)*memorySize);
   return byteEquivalent;
  }else if(memoryUnit.equalsIgnoreCase(GB)){
   byteEquivalent = (long)(Math.pow(2,30)*memorySize);
   return byteEquivalent;
  }else{
  System.out.println("Not a valid memory unit");
  System.exit(1);
  return byteEquivalent;
  }

}

//method returns the memory  unit
String getMemoryUnit(String entry){
  
     if(entry.contains(GB)){
      return GB;
     }else if(entry.contains(MB)){
      return MB;
     }else if(entry.contains(KB)){
      return KB;
     } else if(entry.contains(B)){
      return B;
     }else{
     System.out.println("invalid input data");
     System.exit(1);
     return "";
     }
  }

//Check if variable exists in global scope
String checkForGlobalVariable(String inputEntry){
  
   int index;
   String variableName,variablePointer,arrayVariable;
   String variableScope = "NOTEXISTS";
   List<String> tempArray ;
  
  if(inputEntry.contains("[")){
   index = inputEntry.indexOf("[");
   inputEntry = inputEntry.substring(0,index);
  }

 for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
     variableName = entry.getKey();
     variablePointer = variableName + ".next";
     tempArray = entry.getValue();
     if((variableName).equals(inputEntry) && (tempArray.get(TWO)).equals("UNMARKED")){
     variableScope = tempArray.get(ONE);
     }else if((variablePointer).contains(inputEntry) && (tempArray.get(TWO)).equals("UNMARKED")){
     variableScope = tempArray.get(ONE);
    }
  }
}
 
 return variableScope;

}

//process the variables for allocation info
void processVariables(String variable,String variableScope,List<Map<String,ArrayList<String>>> inputListMap){

 int startIndex=0,endIndex,memoryAvail;
 String memorySize,memoryUnit,splitedMemorySize,parentScope;
 long byteEquivalent;
 boolean isValidVariable=true;
 String[] splitedEntry = new String[2];
 Map<String,ArrayList<String>> inputMap = new LinkedHashMap<String,ArrayList<String>>();
 ArrayList<String> inputList = new ArrayList<String>();
 
 splitedEntry = variable.split("=");
 splitedEntry[0] =splitedEntry[0].trim();
 splitedEntry[1] = splitedEntry[1].trim();

 startIndex = splitedEntry[1].indexOf("(");
 endIndex = splitedEntry[1].indexOf(")");
 memorySize = splitedEntry[1].substring(startIndex+1,endIndex);
 memorySize = memorySize.toUpperCase();
 memoryUnit = getMemoryUnit(memorySize);
 endIndex = memorySize.indexOf(memoryUnit);
 splitedMemorySize = memorySize.substring(0,endIndex);
 memoryAvail = Integer.parseInt(splitedMemorySize);
  if(splitedEntry[0].contains(".next")){
   isValidVariable = checkForValidElement(splitedEntry[0],inputListMap);
    if(!isValidVariable){
     System.out.println("Invalid pointer reference "+splitedEntry[0]);
     return;
    }
  }
  parentScope=checkForGlobalVariable(splitedEntry[0]);
  if(!"NOTEXISTS".equals(parentScope)){
    variableScope=parentScope;
  }
 byteEquivalent = getByteEquivalent(memoryAvail,memoryUnit);
 inputList.add(String.valueOf(byteEquivalent));
 inputList.add(variableScope);
 inputList.add(UNMARKED);
 inputMap.put(splitedEntry[0],inputList);

 inputListMap.add(inputMap);
   
}

//process the local scope block
int processLocalScope(String scopeName,int currentEntryCount,List<String> inputArray,List<Map<String,ArrayList<String>>> inputListMap){
 
 
 int openBracketCount=0,closeBracketCount=0,entryCount = 0;
 boolean isLocalScope=false;
 Map<String,ArrayList<String>> localVariableMap = new LinkedHashMap<String,ArrayList<String>>();
 ArrayList<String> localVariables = new ArrayList<String>();

 
   for(String entry : inputArray){
     entryCount++;
    if(entryCount > currentEntryCount){
     if(entry.contains("{")){
       localVariables.add("{");
       openBracketCount++;     
       isLocalScope = true;
     }else if(isLocalScope && !entry.contains("}")){
       localVariables.add(entry);
     }else if(isLocalScope && entry.contains("}")){
       localVariables.add("}");
       closeBracketCount++;
       if(openBracketCount == closeBracketCount){
       isLocalScope = false;
       break;
       }
     }
    }
   }
  
  localVariableMap.put(scopeName,localVariables);
  inputListMap.add(localVariableMap);
  return entryCount;
}

//Check whether for loop element and pointer element are valid
boolean  checkForValidElement(String inputEntry,List<Map<String,ArrayList<String>>> inputListMap){

 int variableIndex=0,index;
 String variableName,splittedInputEntry;
 String[] parentEntryVariable = new String[2];
 boolean isValidVariable = false;
 List<String> tempData;   

 
if(inputEntry.contains("[") || inputEntry.contains(".next")){
   for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
     variableName = entry.getKey();
     tempData = entry.getValue();
     if((inputEntry).contains(variableName) && UNMARKED.equals(tempData.get(TWO)))
     isValidVariable = true;
  }
} 

  for(Map<String,ArrayList<String>> variableEntry : inputListMap){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
     variableName = entry.getKey();
     if((inputEntry).contains(variableName))
     isValidVariable = true;
  }
}

} else{
 
 isValidVariable = true;
}
 

return isValidVariable;
}

boolean  checkForReferenceElement(String inputEntry,List<Map<String,ArrayList<String>>> inputListMap){

 
 String variableName;
 boolean isValidVariable = false;
 List<String> tempData;



   for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
     variableName = entry.getKey();
     tempData = entry.getValue();
     if((inputEntry).contains(variableName) && UNMARKED.equals(tempData.get(TWO)))
     isValidVariable = true;
  }
}

  for(Map<String,ArrayList<String>> variableEntry : inputListMap){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
     variableName = entry.getKey();
     if((inputEntry).contains(variableName))
     isValidVariable = true;
  }
}

return isValidVariable;
}


//Process the for loop statement
boolean processForLoop(String inputEntry,boolean isGlobalScope,List<Map<String,ArrayList<String>>> inputListMap)
{
  
  int indexOfParanthesis,indexOfSquareBracket,incrementValueIndex=0;
  boolean containForStmt = false,isArrayElement = false;
  String[] splitInputData = new String[2];
  String[] forLoopData = new String[3];
  String[] tempData = new String[2];
  String[] forLoopStatement1 = new String[2];
 

      
        inputEntry = inputEntry.trim();
        
    	indexOfParanthesis = inputEntry.indexOf(")"); 
    	splitInputData[0] = inputEntry.substring(ZERO,indexOfParanthesis+1);
    	if(indexOfParanthesis < inputEntry.length()-1){
    	splitInputData[1] = inputEntry.substring(indexOfParanthesis+1,inputEntry.length());
    	splitInputData[1] = splitInputData[1].trim();
           if(splitInputData[1].contains("[")){
    	      indexOfSquareBracket = splitInputData[1].indexOf("[");
    	      forLoopStatement1[0] = splitInputData[1].substring(0,indexOfSquareBracket+1);
    	      forLoopStatement1[1] = splitInputData[1].substring(indexOfSquareBracket+2,splitInputData[1].length());
              isArrayElement = true;
            }else{
             forLoopStatement1[0] = splitInputData[1];
           }
           if(!(forLoopStatement1[0].equals("{")))
           containForStmt = true;
        
    	}
   	forLoopData = splitInputData[0].split(";");
    	tempData = forLoopData[0].split("=");
    	forLoopMap.put(INITIALIZATION_VALUE,tempData[1].trim());
    	if(forLoopData[1].contains("<")){	
     	tempData = forLoopData[1].split("<");
    	forLoopMap.put(CONDITIONAL_OPERATOR,"<");
    	forLoopMap.put(CONDITIONAL_VALUE, tempData[1].trim());
   	}else if(forLoopData[1].contains(">")){
     	tempData = forLoopData[1].split(">");
    	forLoopMap.put(CONDITIONAL_OPERATOR,">");
    	forLoopMap.put(CONDITIONAL_VALUE, tempData[1].trim());
   	}else if(forLoopData[1].contains(">=")){
    	tempData = forLoopData[1].split(">=");
    	forLoopMap.put(CONDITIONAL_OPERATOR,">=");
    	forLoopMap.put(CONDITIONAL_VALUE, tempData[1].trim());
   	}else if(forLoopData[1].contains("<=")){
    	tempData = forLoopData[1].split("<=");
    	forLoopMap.put(CONDITIONAL_OPERATOR,"<=");
    	forLoopMap.put(CONDITIONAL_VALUE, tempData[1].trim());
   	}else if(forLoopData[1].contains("!=")){
    	tempData = forLoopData[1].split("!=");
        //System.out.println(tempData[1]);
    	forLoopMap.put(CONDITIONAL_OPERATOR,"!=");
    	forLoopMap.put(CONDITIONAL_VALUE, tempData[1].trim());
   	}

       if(forLoopData[2].contains("++"))
       {
    	forLoopMap.put(INC_OR_DEC,"++");
       }else if(forLoopData[2].contains("--")){
    	forLoopMap.put(INC_OR_DEC,"--");
       }else if(forLoopData[2].contains("+=")){
    	forLoopMap.put(INC_OR_DEC,"+=");
    	incrementValueIndex = forLoopData[2].indexOf("+=");
    	forLoopData[2] = forLoopData[2].trim();
    	forLoopData[2] = forLoopData[2].substring(incrementValueIndex+1,forLoopData[2].length()-1);
    	forLoopData[2] = forLoopData[2].trim();
    	forLoopMap.put("incrementValue",forLoopData[2]);
      }

               if(containForStmt)
               runFoorLoop(forLoopStatement1,isArrayElement,isGlobalScope,false,inputListMap);
 		
  return containForStmt;
}

boolean checkInputList(String[] loopEntry)
{
int entryIndex=-1,bracketIndex,i=0,j=0;
String splittedLoopEntry=null;
String[] loopStatement,splittedVariable;
boolean isPresent = false;


  if(loopEntry[0].contains("[")){
    bracketIndex=loopEntry[0].indexOf("[");
    splittedLoopEntry = loopEntry[0].substring(0,bracketIndex);

 for(Map.Entry<String[],Boolean> entry : forLoopInput.entrySet()){
   if((entry.getKey()).equals(loopEntry)){
   entryIndex = i;
  }
  i++;
}  
 for(Map.Entry<String[],Boolean> entry : forLoopInput.entrySet()){
   loopStatement=entry.getKey();
     if(loopStatement[0].contains("=")){
     splittedVariable = loopStatement[0].split("=");
    if(splittedLoopEntry != null && splittedLoopEntry.equals(splittedVariable[0].trim())){
    if(entryIndex > j)
    isPresent = true;
  }
  }
   j++;
 }


}

return isPresent;
}

void runForLoopBlock(List<String> inputList,int index,List<Map<String,ArrayList<String>>> inputListMap){

int entryIndex=-1,bracketIndex,i;
String splittedEntry=null;
String[] loopStatement;
boolean isValidStatement = false,isPresent = false;

for(Map.Entry<String[],Boolean> entry : forLoopInput.entrySet()){
 loopStatement = entry.getKey();
 isValidStatement = checkForValidElement(loopStatement[0],inputListMap);
 isPresent = checkInputList(loopStatement);
if(isValidStatement || isPresent){
  if(entry.getValue()){
   inputList.add(loopStatement[0]+index+loopStatement[1]);
  }else{
   inputList.add(loopStatement[0]);
}
}else{
 System.out.println("Invalid ForLoop statement!!!");
}
}
}

//Execute the for loop parsed from input data
void runFoorLoop(String[] forLoopStatement1,boolean isArrayElement,boolean isGlobalScope,boolean isBlockStatement,List<Map<String,ArrayList<String>>> inputListMap){

  int index=0,startIndex=0,conditional_Value=0,incrementValueIndex=0;
  boolean isValidElement=true;
  ArrayList<String> inputList = new ArrayList<String>();
  List<Map<String,ArrayList<String>>> forLoopInputListMap;
  Map<String,ArrayList<String>> forLoopDataMap;
         
         if(!isBlockStatement)
         isValidElement=checkForValidElement(forLoopStatement1[0],inputListMap);
         if(isValidElement)
         {
           if(null != forLoopMap && forLoopMap.size() > 0){
                if(forLoopMap.get(INITIALIZATION_VALUE) != null){
                        startIndex = Integer.parseInt(forLoopMap.get(INITIALIZATION_VALUE));
                }
                if(forLoopMap.get(CONDITIONAL_VALUE) != null){
                         conditional_Value = Integer.parseInt(forLoopMap.get(CONDITIONAL_VALUE));
                }
                if(forLoopMap.get("incrementValue") != null){
                        incrementValueIndex = Integer.parseInt(forLoopMap.get("incrementValue"));
                }
                if(("<").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) && ("++").equals(forLoopMap.get(INC_OR_DEC))){
                        inputList.add("{");
           
                        for(index = startIndex; index < conditional_Value; index++){
                           if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                           }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }
                         }
                        inputList.add("}");
                }else if(("<").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) && ("+=").equals(forLoopMap.get(INC_OR_DEC))){
                        inputList.add("{");
                        for(index = startIndex; index < conditional_Value; index+=incrementValueIndex){
                           if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }
                        }
                        inputList.add("}");
                 }else if(("!=").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("++").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index != conditional_Value; index++){
                            if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }

                        }
                        inputList.add("}");
                 }else if(("!=").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("+=").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index != conditional_Value; index+=incrementValueIndex){
                             if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }

                        }
                        inputList.add("}");

                 } else if((">").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("++").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index > conditional_Value; index++){
                             if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }
                        }
                        inputList.add("}");
                  } else if((">").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("+=").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index > conditional_Value; index+=incrementValueIndex){
                             if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }

                        }
                        inputList.add("}");

                 } else if((">=").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("++").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index >= conditional_Value; index++){
                             if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                           }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }
                        }
                        inputList.add("}");
                } else if((">=").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("+=").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index >= conditional_Value; index+=incrementValueIndex){
                             if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                           }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }

                        }
                        inputList.add("}");

                }  else if(("<=").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("++").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index <= conditional_Value; index++){
                            if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }

                        }
                        inputList.add("}");
                  }else if(("<=").equals(forLoopMap.get(CONDITIONAL_OPERATOR)) &&( ("+=").equals(forLoopMap.get(INC_OR_DEC)))){
                        inputList.add("{");
                        for(index = startIndex; index <= conditional_Value; index+=incrementValueIndex){
                            if(!isBlockStatement){
                              if(isArrayElement){
                                inputList.add(forLoopStatement1[0]+index+forLoopStatement1[1]);
                              }else{
                                inputList.add(forLoopStatement1[0]);
                              }
                            }else{
                            runForLoopBlock(inputList,index,inputListMap);
                          }
                        }
                        inputList.add("}");
                  }

                if(inputList.size() > 2 && !isBlockStatement){
                forLoopInputListMap =  getInputMap(isGlobalScope,inputList);
                if(forLoopInputListMap != null && forLoopInputListMap.size() > 0)
                inputListMap.addAll(forLoopInputListMap);
                }
                if(inputList.size() > 2 && isBlockStatement){
                forLoopDataMap = new LinkedHashMap<String,ArrayList<String>>();
                forLoopDataMap.put("LOCALSCOPENUMBER"+localScope,inputList);
               inputListMap.add(forLoopDataMap);
              }
            }
         }else{
         System.out.println("Invalid for loop data !!!");
      }

}

//Process the block of statements following for loop
void processForLoopBlock(List<String> processStmts,boolean isGlobalScope,boolean isBlockStmt,List<Map<String,ArrayList<String>>> inputListMap){

 int indexOfSquareBracket,index;
 String forLoopStatement;
 boolean isArrayElement=false;
 String[] forLoopStatement1 = new String[2];
 forLoopInput = new LinkedHashMap<String[],Boolean>();
 List<String> processedInput;
   
  if(null != processStmts && processStmts.size() == 1){
    processedInput = processContinuousVariable(processStmts.get(ZERO));
    if(null != processedInput && processedInput.size() > 0){
        forLoopStatement = processedInput.get(ONE);
        if(forLoopStatement.contains("[")){
              indexOfSquareBracket = forLoopStatement.indexOf("[");
              forLoopStatement1[0] = forLoopStatement.substring(0,indexOfSquareBracket+1);
              forLoopStatement1[1] = forLoopStatement.substring(indexOfSquareBracket+2,forLoopStatement.length());
              isArrayElement = true;
         }else{
            forLoopStatement1[0] = forLoopStatement;
         }
           forLoopInput.put(forLoopStatement1,isArrayElement);
           runFoorLoop(forLoopStatement1,isArrayElement,isGlobalScope,isBlockStmt,inputListMap);
 
    }
   if(null != processedInput && processedInput.size() > 3){
    index = (processStmts.get(ZERO)).indexOf(";");
    forLoopStatement = processStmts.get(ZERO);
    forLoopStatement = forLoopStatement.trim();
    forLoopStatement = forLoopStatement.substring(index+1,forLoopStatement.length());   
    continuousVariableInput(forLoopStatement,inputListMap,isGlobalScope);
   }
  }else if(null != processStmts && processStmts.size() >  1){
    for(String loopStatement : processStmts){
      forLoopStatement1 = new String[2];
       if(loopStatement.contains("[")){
              indexOfSquareBracket = loopStatement.indexOf("[");
              forLoopStatement1[0] = loopStatement.substring(0,indexOfSquareBracket+1);
              forLoopStatement1[1] = loopStatement.substring(indexOfSquareBracket+2,loopStatement.length());
              isArrayElement = true;
              forLoopInput.put(forLoopStatement1,isArrayElement);
         }else{
              forLoopStatement1[0] = loopStatement;
              isArrayElement=false;
              forLoopInput.put(forLoopStatement1,isArrayElement);
    }
     //runFoorLoop(forLoopStatement1,isArrayElement,isGlobalScope,true,inputListMap);
  }
  runFoorLoop(forLoopStatement1,isArrayElement,isGlobalScope,true,inputListMap);
}

}


//process the variables declared in the same line of input
void continuousVariableInput(String inputEntry,List<Map<String,ArrayList<String>>> inputListMap,boolean isGlobalScope)
{
   

   List<String> inputList;
   List<Map<String,ArrayList<String>>> processedListMap;

   inputList = processContinuousVariable(inputEntry);
   processedListMap = getInputMap(isGlobalScope,inputList);
   if(null != processedListMap && processedListMap.size() > 0)
   inputListMap.addAll(processedListMap);
   

}

//process continuous variable and return as list of string
List<String> processContinuousVariable(String inputEntry){

   int index;
   String trimmedInputEntry;
   String[] splittedInput;
   List<String> inputList = new ArrayList<String>();

   splittedInput = inputEntry.split(";");
   inputList.add("{");
   for(index = 0;index<splittedInput.length;index++)
   {
     trimmedInputEntry = splittedInput[index].trim();
     if(null != trimmedInputEntry){
      trimmedInputEntry = trimmedInputEntry+";";
      inputList.add(trimmedInputEntry);
    }
   }
   inputList.add("}");

 return inputList;
}

//process pointer variables
void processPointerVariables(String inputEntry,boolean isGlobalScope,List<Map<String,ArrayList<String>>> inputListMap){

 int index;
 boolean isValidReference = false,isValidReferencedVariable=false;
 String variable,referencedVariable;
 String[] splittedInputEntry,splittedPointerVariable;
 List<String> inputList = new ArrayList<String>();
 ArrayList<String> pointerInfoList;
 Map<String,ArrayList<String>> pointerVariableMap;
 List<Map<String,ArrayList<String>>> processedListMap;
    
      inputList.add("{");
      if(null != inputEntry && inputEntry.contains(";")){
         splittedInputEntry = inputEntry.split(";");
         for(index = 0; index < splittedInputEntry.length; index++){
           variable = splittedInputEntry[index].trim();
           variable = variable + ";";
           
          if(variable.matches(POINTER_VARIABLE_REGEX)){
             
             pointerVariableMap = new LinkedHashMap<String,ArrayList<String>>();
             pointerInfoList = new ArrayList<String>();
             splittedPointerVariable = variable.split("=");
             referencedVariable = splittedPointerVariable[1].trim();
             isValidReference=checkForValidElement(splittedPointerVariable[0],inputListMap);
             isValidReferencedVariable = checkForReferenceElement(referencedVariable,inputListMap);
            if(isValidReference && isValidReferencedVariable){
             pointerInfoList.add(referencedVariable.substring(0,(referencedVariable.length())-1));
             pointerVariableMap.put(splittedPointerVariable[0].trim(),pointerInfoList);
             inputListMap.add(pointerVariableMap);
            }else{
             System.out.println("Invalid pointer reference!!! "+variable);
           }
          }else if(variable.matches(VARIABLE_POINTER_FORMAT_REGEX)){
             isValidReference=checkForValidElement(variable,inputListMap);
            if(isValidReference){
             inputList.add(variable);
             }else{
              System.out.println("Invalid pointer reference!!! "+variable);
             }
          }else if(variable.matches(VARIABLE_REGEX)){
             inputList.add(variable);
          }else{
            System.out.println("Invalid input data!!!");
            System.exit(1);
          }
          
         }
      }else{
       System.out.println("Invalid input data!!!");
       System.exit(1);
     }
     inputList.add("}");
   if(inputList.size() > 2)
   {
    processedListMap = getInputMap(isGlobalScope,inputList);
    inputListMap.addAll(processedListMap);
   }
          
}

int getLocalScopeCount(){
   boolean isOpenBracket=false;
   int localScopeCount=0;
   
   for(String entry : inputArray){
    if(entry.equals("{")){
     isOpenBracket = true;
    }
    if(isOpenBracket && entry.equals("{")){
     localScopeCount++;
     isOpenBracket = false;
    }
   }
 return localScopeCount;
}

void processVariablesWithBracket(String inputEntry,boolean isGlobalScope,List<Map<String,ArrayList<String>>> inputListMap){

int bracketIndex=0; 
String associatedVariable;
List<String> inputList = new ArrayList<String>();
List<Map<String,ArrayList<String>>> processedListMap;

   if(inputEntry.contains("{")){
      bracketIndex = inputEntry.indexOf("{");
   }else if(inputEntry.contains("}")){
      bracketIndex = inputEntry.indexOf("}");
   }

  associatedVariable = inputEntry.substring(0,bracketIndex);
  associatedVariable = associatedVariable.trim();
  inputList.add("{");
  inputList.add(associatedVariable);
  inputList.add("}");

 processedListMap = getInputMap(isGlobalScope,inputList);
 inputListMap.addAll(processedListMap);
  
}

//process the input map
List<Map<String,ArrayList<String>>>  getInputMap(boolean isGlobalScope,List<String> inputToBeProcessed){
 
  int localScopeCount,currentEntryCount =0,openBracketCount=0,closeBracketCount=0;
  boolean isLocal = false,isForLoop=false,isForLoopStmt = false,isForLoopBlock=false,isBlockStmt=false;
  String openBracket,closeBracket,variableScope;
  List<Map<String,ArrayList<String>>> inputListMap = new ArrayList<Map<String,ArrayList<String>>>();
  List<String> forLoopData = new ArrayList<String>();
  Map<String,ArrayList<String>> inputMap = new LinkedHashMap<String,ArrayList<String>>();

  
   openBracket = inputToBeProcessed.remove(0);
   closeBracket = inputToBeProcessed.remove((inputToBeProcessed.size())-1);
   localScopeCount = getLocalScopeCount();
    inputMap.put(LOCAL_SCOPE_COUNT,new ArrayList<String>());
    inputMap.get(LOCAL_SCOPE_COUNT).add(String.valueOf(localScopeCount));
   inputListMap.add(inputMap);
  if(!(openBracket.equals("{") && closeBracket.equals("}"))){
    System.out.println("invalid input data!!!");
    System.exit(1);
   }
  if(isGlobalScope){
   variableScope = "GLOBAL";
  }else{
   variableScope = localScopeName;
  }

      for(String entry : inputToBeProcessed){
         if(null != entry){
           if(!isLocal && !isForLoop && (entry.matches(VARIABLE_REGEX) || entry.matches(VARIABLE_POINTER_FORMAT_REGEX))){
             processVariables(entry,variableScope,inputListMap);
           }else if((!isForLoop && entry.contains("{")) && !entry.matches(FOR_LOOP_REGEX)){
             isLocal = true;
             if(entry.length() > 1)
              processVariablesWithBracket(entry,isGlobalScope,inputListMap);
             if(openBracketCount == 0){
             currentEntryCount = processLocalScope("LOCALSCOPENUMBER"+localScope,currentEntryCount,inputToBeProcessed,inputListMap);
             localScope++;
             }
             openBracketCount++;
             //localScope++;
            // System.out.println(entry+"count "+localScope);
          }else if(isLocal && !isForLoop && entry.contains("}")){
            closeBracketCount++;
             if(entry.length() > 1)
              processVariablesWithBracket(entry,isGlobalScope,inputListMap);
             if(closeBracketCount == openBracketCount){
               openBracketCount = 0;
               closeBracketCount = 0;
               isLocal = false;
             }
            
          }else if(isLocal && !isForLoop){
           //skipping the local variables
          }else if(!isLocal && entry.matches(FOR_LOOP_REGEX)){       
            isForLoop = true;
            isForLoopStmt =  processForLoop(entry,isGlobalScope,inputListMap);
            if(isForLoopStmt){ 
             isForLoop = false;
           }
            if(entry.contains("{")){
              isForLoopBlock=true;
            }
             
          }else if(isForLoop && !isLocal){
          
             if(entry.equals("{")){
                isForLoopBlock = true;
             }else if(isForLoopBlock && !entry.equals("}")){
                forLoopData.add(entry);
             }else if(isForLoopBlock && entry.equals("}")){
                isForLoopBlock = false;
                isBlockStmt = true;
                isForLoop = false;
             }
             if(!isForLoopBlock && (entry.matches(VARIABLE_REGEX) || entry.matches(CONT_VARIABLE_REGEX) || entry.matches(POINTER_VARIABLE_REGEX) || entry.matches(VARIABLE_POINTER_FORMAT_REGEX))){
                forLoopData.add(entry);
                isBlockStmt = false;
                isForLoop=false;
             }
            if(!isForLoop)
             processForLoopBlock(forLoopData,isGlobalScope,isBlockStmt,inputListMap);
            if(isBlockStmt)
            localScope++;
 
          }else if(!isLocal && !isForLoop && entry.contains(".next"))
          {
             processPointerVariables(entry,isGlobalScope,inputListMap);
          }else if(!isLocal && !isForLoop && entry.matches(CONT_VARIABLE_REGEX)){
             continuousVariableInput(entry,inputListMap,isGlobalScope);
          }else{
            System.out.println("Invalid Input data!!!");
            System.exit(1);
          }

         }    
      }
  return inputListMap;
}

//remove marked marked variables during memory compaction.
boolean removeMarkedVariables(String generationName){
  
  long variableSize, variableAddress,avaliableFreeSpace,allocatedSpace;
  boolean isCompacted=false,isReferenced;
  ArrayList<String> tempArray;
  ArrayList<Long> generationInfo = generationMap.get(generationName);
  List<Map<String,ArrayList<String>>> updatedAllocationInfo = new ArrayList<Map<String,ArrayList<String>>>(allocationInfo);

  
  for(Map<String,ArrayList<String>> variableEntry : updatedAllocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
      tempArray = entry.getValue();
      isReferenced = checkForReferenceVariable(entry.getKey());
      if(MARKED.equals(tempArray.get(TWO)) && generationName.equals(tempArray.get(THREE)) && !isReferenced){
       isCompacted = true;
       variableSize = Long.parseLong(tempArray.get(ZERO));
       variableAddress = Long.parseLong(tempArray.get(FOUR));
       allocationInfo.remove(variableEntry);
       avaliableFreeSpace = generationInfo.get(THREE) + variableSize;
       allocatedSpace = generationInfo.get(TWO) - variableSize;
       generationInfo.set(TWO,allocatedSpace);
       generationInfo.set(THREE,avaliableFreeSpace);
       memoryCompaction(variableAddress,generationName);
     }
   }
 }
 return isCompacted;
}

//print the variable address
void printVariableAddress(){
   
   ArrayList<String> tempArray;

   System.out.println("Garbage Collection:");
   for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
     tempArray = entry.getValue();
     if(tempArray.size()== 6 && ADDRESS_CHANGED.equals(tempArray.get(FIVE)))
     System.out.println(entry.getKey()+" allocated "+tempArray.get(ZERO)+"B with starting address "+tempArray.get(FOUR)+"B");
  }
}

}

//After removing marked variables, here I am compacting the memory
void memoryCompaction(long variableAddress,String generationName){


long currentAddress,currentVariableSize;
ArrayList<String> tempArray;

 for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
   tempArray = entry.getValue();
   currentAddress = Long.parseLong(tempArray.get(FOUR));
   currentVariableSize = Long.parseLong(tempArray.get(ZERO));
    if(variableAddress < currentAddress && generationName.equals(tempArray.get(THREE))){
      tempArray.set(FOUR,String.valueOf(variableAddress));
      if(tempArray.size() == 6){
      tempArray.set(FIVE,ADDRESS_CHANGED);
      }else{
      tempArray.add(FIVE,ADDRESS_CHANGED);
      }
      variableAddress = variableAddress+currentVariableSize;
    }else{
      if(tempArray.size() == 6){
      tempArray.set(FIVE,ADDRESS_UNCHANGED);
      }else{
      tempArray.add(FIVE,ADDRESS_UNCHANGED);
      }
   }
  }
 }
}

//compact the memory of lower generation when variables move to higher generation
void compactionMovingToHigherGenertaion(long variableAddress,String generationName){


long currentAddress,currentVariableSize;
ArrayList<String> tempArray;

 for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
   for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
   tempArray = entry.getValue();
   currentAddress = Long.parseLong(tempArray.get(FOUR));
   currentVariableSize = Long.parseLong(tempArray.get(ZERO));
    if(variableAddress < currentAddress && generationName.equals(tempArray.get(THREE))){
      tempArray.set(FOUR,String.valueOf(variableAddress));
      if(tempArray.size() == 6){
      tempArray.set(FIVE,ADDRESS_CHANGED);
      }else{
      tempArray.add(FIVE,ADDRESS_CHANGED);
      }
      variableAddress = variableAddress+currentVariableSize;
    }
  }
 }
}


//mark the duplicate variables for garbage collection
void markGarbages(String variableName,String variableScope){

  String variablePointer,arrayVariable,variableChunks;
  
   if(null != allocationInfo && allocationInfo.size() > 0){
     for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
       for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
          variablePointer = variableName+".next";
          arrayVariable = variableName+"[";
          variableChunks = variableName+"_CHUNK";
         if(entry.getKey().equals(variableName) && variableScope.equals(entry.getValue().get(ONE))){
            deleteReference(variableName,false);        
           (entry.getValue()).set(TWO,MARKED);
         }
          if(entry.getKey().equals(variablePointer) && variableScope.equals(entry.getValue().get(ONE))){
            deleteReference(variableName,false);
           (entry.getValue()).set(TWO,MARKED);
         }
          
          if(entry.getKey().contains(arrayVariable) && variableScope.equals(entry.getValue().get(ONE))){
            deleteReference(entry.getKey(),false);
           (entry.getValue()).set(TWO,MARKED);
         }
         if(entry.getKey().contains(variableChunks) && variableScope.equals(entry.getValue().get(ONE))){
           (entry.getValue()).set(TWO,MARKED);
       }
      }
    }
  }
}

//delete the pointer reference
void deleteReference(String variableName,boolean isLocalScopeExit){
 
 Map<String,String> tempPointerInfoMap = new LinkedHashMap<String,String>(pointerInfoMap);
 variableName = variableName + ".next";
    if(pointerInfoMap.size() > 0){
    for(Map.Entry<String,String> entry : tempPointerInfoMap.entrySet())
     {
       
         if(!isLocalScopeExit)
         {
          if(variableName.contains(entry.getValue())){
             pointerInfoMap.remove(entry.getKey());
           }
         }
          if(variableName.equals(entry.getKey())){
             pointerInfoMap.remove(entry.getKey());
           }
     }

   }
}

//check whether the variable is referenced by active variable
boolean checkForReferenceVariable(String variableName){

  boolean isReferenced = false;

  if(pointerInfoMap.size() > 0){
    for(Map.Entry<String,String> entry : pointerInfoMap.entrySet())
     {
          if(variableName.equals(entry.getValue())){
            isReferenced = true;
           }
     }
  }  
   return isReferenced;

   }

//Once all the variables in local scope are processed, I am marking it for garbage collection
void markLocalVariables(String localScopeName){
  
   if(null != allocationInfo && allocationInfo.size() > 0){
     for(Map<String,ArrayList<String>> variableEntry : allocationInfo){
       for(Map.Entry<String,ArrayList<String>> entry : variableEntry.entrySet()){
         if(localScopeName.equals(entry.getValue().get(ONE))){
           deleteReference(entry.getKey(),true);
           (entry.getValue()).set(TWO,MARKED);
         }
       }
      }
    }
}

//Allocate the variable and update variable address
void updateVariableAddress(ArrayList<Long> lowerGenerationInfo,ArrayList<String> variableDetails,String variableName){

 long variableSize,variableAddress,availableFreeSpace,startAddress;
 ArrayList<String> tempData = new ArrayList<String>(variableDetails);
 Map<String,ArrayList<String>> variableAllocationInfo = new LinkedHashMap<String,ArrayList<String>>();

        variableSize = Long.parseLong(variableDetails.get(ZERO));

        availableFreeSpace = (lowerGenerationInfo.get(THREE)) - variableSize;
        startAddress = lowerGenerationInfo.get(ZERO);
        variableAddress = startAddress+lowerGenerationInfo.get(TWO);
        lowerGenerationInfo.set(TWO,variableSize+variableAddress);
        lowerGenerationInfo.set(THREE,availableFreeSpace);
        tempData.add(GENERATION1);
        tempData.add(String.valueOf(variableAddress));
        variableAllocationInfo.put(variableName,tempData);
        allocationInfo.add(variableAllocationInfo);
        System.out.println(variableName+" allocated "+variableSize+"B with starting address "+variableAddress+"B");
}

//update the variable address when moving to higher generation
void moveVariableAddress(ArrayList<Long> generationInfo,ArrayList<String> variableDetails,String variableName){

 long variableSize,variableAddress,availableFreeSpace,startAddress=0;

    
        variableSize = Long.parseLong(variableDetails.get(ZERO));
        availableFreeSpace = (generationInfo.get(THREE)) - variableSize;
        if(generationInfo.get(TWO) == 0){
        startAddress = generationInfo.get(ZERO);}
        else{
        startAddress = 0;
        }
        variableAddress = startAddress+generationInfo.get(TWO);
        generationInfo.set(TWO,variableSize+variableAddress);
        generationInfo.set(THREE,availableFreeSpace);
        variableDetails.set(FOUR,String.valueOf(variableAddress));
        if(variableDetails.size() == 6){
        variableDetails.set(FIVE,ADDRESS_CHANGED);
        } else{
        variableDetails.add(FIVE,ADDRESS_CHANGED);
        }
        
}




//Move variables to higher generations
void moveToHigherGeneration(int generationWithFreeSpace,boolean isCompacted){

   int index,nextHigherGeneration,count=0;
   String lowerGenerationName,higherGenerationName;
   long lowerAllocatedSpace,lowerGenerationFreeSpace,variableSize,variableAddress;
   ArrayList<Long> generationInfo,lowerGenerationInfo;
   ArrayList<String> variableInfoList;
  
   
   for(index = generationWithFreeSpace-1; index >= ONE; index--){
    nextHigherGeneration = index+1;
    higherGenerationName = "generation"+nextHigherGeneration;
    generationInfo = generationMap.get(higherGenerationName);
    lowerGenerationName = "generation"+index;
    lowerGenerationInfo = generationMap.get(lowerGenerationName);
    lowerAllocatedSpace = lowerGenerationInfo.get(TWO);
      for(Map<String,ArrayList<String>> allocatedVariables : allocationInfo){
        for(Map.Entry<String,ArrayList<String>> variableInfoMap : allocatedVariables.entrySet()){
           variableInfoList = variableInfoMap.getValue();
            variableSize = Long.parseLong(variableInfoList.get(ZERO));
           variableAddress = Long.parseLong(variableInfoList.get(FOUR));
           generationInfo = generationMap.get(higherGenerationName);
           if(lowerGenerationName.equals(variableInfoList.get(THREE)) && generationInfo.get(THREE) >= variableSize) {
               variableInfoList.set(THREE,higherGenerationName);
               lowerAllocatedSpace = lowerAllocatedSpace - variableSize;
               lowerGenerationFreeSpace = lowerGenerationInfo.get(THREE)+variableSize;
               lowerGenerationInfo.set(TWO,lowerAllocatedSpace);
               lowerGenerationInfo.set(THREE,lowerGenerationFreeSpace);
               compactionMovingToHigherGenertaion(variableAddress,lowerGenerationName);
               moveVariableAddress(generationInfo,variableInfoList,variableInfoMap.getKey());
               count++;
           } else if(!isCompacted && higherGenerationName.equals(variableInfoList.get(THREE))){
               if(count == 0)
                return;
                if(variableInfoList.size() == 6){
                 variableInfoList.set(FIVE,ADDRESS_UNCHANGED);
                } else{
                 variableInfoList.add(FIVE,ADDRESS_UNCHANGED);
                }
           }
        }
    }
  }

}


//find higher generation
void findHigherGeneration(int generationCount,long variableSize){

   long higherAvailFreeSpace;
   int nextGeneration;
   boolean isSpaceAvail = false,isCompacted = false;
   String higherGenerationName;
   ArrayList<Long> higherGenerationInfo,generation1Info;
   
   generation1Info = generationMap.get("generation"+ONE);
   nextGeneration = generationCount+1;
   if(nextGeneration <= partition){
   
   higherGenerationName = "generation"+nextGeneration;
   higherGenerationInfo = generationMap.get(higherGenerationName);
    isCompacted = removeMarkedVariables(higherGenerationName);
    higherAvailFreeSpace = higherGenerationInfo.get(THREE);
      if(higherAvailFreeSpace != 0){
           moveToHigherGeneration(nextGeneration,isCompacted);
         if(variableSize <= generation1Info.get(THREE)){
           isSpaceAvail = true;
         }else if(nextGeneration <  partition){
           findHigherGeneration(nextGeneration,variableSize);
         }
     }else if(nextGeneration < partition){
       findHigherGeneration(nextGeneration,variableSize);
    }
  }
}


void  splitVariable(String variableName, ArrayList<String> variableDetails,String generationName)
{

 int splitNumber = 0;
 long availableMem,variableSize,remainingMem;
 boolean isSplitted=true;
 String splittedVariableName;
 ArrayList<Long> lowerGenerationInfo;
 ArrayList<String> variableChunks;
 Map<String,ArrayList<String>> splittedVariableMap = new LinkedHashMap<String,ArrayList<String>>();
 List<Map<String,ArrayList<String>>> splittedVariableInfo = new ArrayList<Map<String,ArrayList<String>>>();
 
 lowerGenerationInfo = generationMap.get(generationName);
 availableMem = lowerGenerationInfo.get(THREE);
 variableSize = Long.parseLong(variableDetails.get(ZERO));
 remainingMem = variableSize-availableMem;
 variableDetails.set(ZERO,String.valueOf(availableMem));
 splittedVariableMap.put(variableName,variableDetails);
 splittedVariableInfo.add(splittedVariableMap);
 
 //System.out.println("remainingMem"+remainingMem+"partition"+partition);
   while(remainingMem > partitionChunkSize){
     variableChunks = new ArrayList<String>(variableDetails);
     splittedVariableMap = new LinkedHashMap<String,ArrayList<String>>();
     splittedVariableName = variableName+"_CHUNK"+splitNumber;
     remainingMem = remainingMem - partitionChunkSize;
     variableChunks.set(ZERO,String.valueOf(partitionChunkSize));
     splittedVariableMap.put(variableName,variableChunks);
     splittedVariableInfo.add(splittedVariableMap);
   }

 if(remainingMem != 0){
 variableChunks = new ArrayList<String>(variableDetails); 
 variableChunks.set(ZERO,String.valueOf(remainingMem));
 splittedVariableName = variableName+"_CHUNK"+splitNumber;
 splittedVariableMap = new LinkedHashMap<String,ArrayList<String>>();
 splittedVariableMap.put(splittedVariableName,variableChunks);
 splittedVariableInfo.add(splittedVariableMap);
 }

 memoryAllocation(splittedVariableInfo);
 
 

}

//allocate variables 
void memoryAllocation(List<Map<String,ArrayList<String>>> inputListMap){


  String variableName,variableScope;
  long variableSize;
  ArrayList<Long> lowerGenerationInfo;
  ArrayList<String> tempData;
  List<Map<String,ArrayList<String>>> localInputList; 

  lowerGenerationInfo = generationMap.get(GENERATION1);
  for(Map<String,ArrayList<String>> inputData : inputListMap){
  for(Map.Entry<String,ArrayList<String>> entry : inputData.entrySet()) {
     variableName = entry.getKey();
     tempData = entry.getValue();
     if(null != variableName && !(variableName.contains("LOCALSCOPE")) && !(variableName.contains("LOCALSCOPENUMBER")) && tempData.size() > 1){
     
     variableScope = tempData.get(ONE);
     variableSize = Long.parseLong(tempData.get(ZERO));
      //System.out.println("memoryAllocation"+variableName+" "+variableScope);
       if(variableSize <= totalMemorySize ){
          markGarbages(variableName,variableScope);
        if(variableSize <= lowerGenerationInfo.get(THREE)){
        updateVariableAddress(lowerGenerationInfo,entry.getValue(),variableName);
        }else{
            removeMarkedVariables(GENERATION1);
            if(variableSize <= lowerGenerationInfo.get(THREE)){
               printVariableAddress();
               updateVariableAddress(lowerGenerationInfo,entry.getValue(),variableName);
            }else{
                   findHigherGeneration(ONE,variableSize);
                   printVariableAddress(); 
                  if(variableSize <= lowerGenerationInfo.get(THREE)){
                  updateVariableAddress(lowerGenerationInfo,entry.getValue(),variableName);
                  } else{
                    if(variableSize <= totalMemorySize && lowerGenerationInfo.get(TWO) == ZERO && allocationInfo.size() == 0){
                      //split the variable
                      splitVariable(variableName,tempData,GENERATION1);

                   }else{
                    System.out.println("Out of Memory!!!");
                   }
                  }          
           }
       }
      }else{
         System.out.println("Out of Memory!!!");
      }
     }else if(variableName.contains("LOCALSCOPENUMBER")){
       localScopeName = variableName;
       localInputList = getInputMap(false,tempData);
       memoryAllocation(localInputList);
       markLocalVariables(variableName);
      }else if(null != variableName && variableName.contains(".next") && tempData.size() == 1){
          pointerInfoMap.put(variableName,tempData.get(ZERO));
     }
    }
  }
}

void clearAllVariables()
{
 allocationInfo = new ArrayList<Map<String,ArrayList<String>>>();
}

}
