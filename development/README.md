# Notes for development and the project
The size of the Devwex binary is limited to a maximum of 30kB. There is no
technical reason for this, it is rather the more than 20 years old quirk and
question -- Why are web servers so big? This should make some unconventional
decisions in the project easier to understand :-)

__Each decision always considers the size of the binary.__ 


# List of peculiarities
- Use Java 5 as compiler setting for source and target  
  This creates the smaller binary, later versions will always be larger.
- Use the language features of Java 5 and 6 but no generics
- Use functionally the API of JDK of 1.8.x  
  Later versions do not support the Java 5 binary format.  
- Write the code so that it finds more patterns for compression in zip format  
  This sounds strange, but it is possible, if similar code passages have a
  comparable structure, it can be compressed better. As an example: `a <= 0` has
  no advantage for length and size, but uses a general pattern for values
  smaller than 0, which then frequently used, improve the compression rate,
  albeit very slightly -- but it adds up.  
  Similar effect, have code passages that are always structured the same way.
- Each import and method costs space, so always think carefully
- Use as many board means of API as possible, but only official java (and javax)
- Each code block in curly classes produces space, if possible use keywords like
  return, break, continue to be able to use subsequent code with conditional
  returns and breaks without further blocks.
- Always look critically at the code where you work -- there will always be
  something that can be optimized -- but please without side-effects
- Refactoring is a standard procedure, therefore the tests must always be well
  maintained 
- Even with all the quirks, keep the code simple, understandable and clean  


# What can/should be ignored during code analysis?
- Java | Compiler issues | Unchecked warning  
  Safety in data types is a win, but creates a larger binary and up until Java
  4 you could handle it just fine. In general, many views are based on Java 4.
- Java | Error handling | Catch block may ignore exception  
  Comment mainly on the _why_. The _how_ can be seen in the code. However, this
  decision was made later in the project. But an empty catch block "can be
  ignored" was kind of like boilerplate.
- Java | Java language level migration aids | Java 5 | Raw use of parameterized class  
  See comment to: Unchecked warning

In general, these quirks and peculiarities apply only to Devwex, everything that
arises around it uses modern and well-known standards and recommendations.


# Project Setup

## Eclipse
TODO:

## IntelliJ
- Clone: https://github.com/seanox/devwex.git
- Project Structure: 
  - Project Settings
    - Project
      - SDK: _Java-8_
      - Language Level: _8 - Lambdas, type, annotations etc._
      - Compiler Output: `B:\Documents\Projects\devwex\program\classes`
    - Modules:
      - Sources: `B:\Documents\Projects\devwex\sources`
      - Tests: `B:\Documents\Projects\devwex\test\sources`
      - Dependencies: (JARs or Directorires) `B:\Documents\Projects\devwex\test\libraries`
- Settings
  - Editor
    - File Encoding
      - Project Encoding: _ISO-8859-1_ or _Windows 1252_

      
# Windows Service (procrun)
- Documentation
  https://commons.apache.org/daemon/procrun.html
- Download  
  https://downloads.apache.org/commons/daemon/binaries/
