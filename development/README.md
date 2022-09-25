# Notes for development and the project

The size of the Devwex binary is limited to a maximum of 30kB. There is no
technical reason for this, it is rather the more than 20 years old quirk and
question -- Why are web servers so big? This should make some unconventional
decisions in the project easier to understand :-)

__Each decision always considers the size of the binary.__ 

# List of known peculiarities

- Use Java-5 as compiler setting for source and target  
  This creates the smaller binary, later versions will always be larger.
- Use the language features of Java 5 and 6 but no generics
- Use functionally the API and the JDK of 1.8.x  
  Later versions do not support the Java 5 binary format.  
- Write the code so that it finds more patterns for compression in zip format  
  This sounds strange, but it is possible, if similar code passages have a
  comparable structure, it can be compressed better.  
  as an example: `a <= 0` has no advantage for length and size, but uses a
  general pattern for values smaller than 0, which then frequently used, improve
  the compression rate, albeit very slightly -- but it adds up.  
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
