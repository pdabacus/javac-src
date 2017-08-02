# javac-src
A simple script for compiling and executing java source files within packages without having to type the -cp and -d paths during compilation. I created this mainly to make it easier to make quick java applications with sublime text.

## Possible Uses
* Sublime Text: Copy `Java.sublime-build` to your sublime text user packages folder and make a new directory called `JavaCompiler`. Then copy `Compile.java` into that folder and compile it in the same directory (creating `Compile.class`) Now you can run java code with packages through sublime text.
* Shell Script: As root, copy `Compile.java` to `/usr/local/share/javac-src/` and compile it in the same directory (creating `Compile.class`) Then copy `javac-src.sh` to `/usr/local/bin/` and use it to compile and run java code with packages through the terminal.

