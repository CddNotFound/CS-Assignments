include Makefile.git

LLVM_JAR = $(shell echo `find /usr/local/lib -name "llvm-*.jar"` | sed "s/\s\+/:/g")
JAVACPP_JAR = $(shell echo `find /usr/local/lib -name "javacpp-*.jar"` | sed "s/\s\+/:/g")
ANTLR_PATH = $(shell find /usr/local/lib -name "antlr-*-complete.jar")
export CLASSPATH = $(ANTLR_PATH):$(LLVM_JAR):$(JAVACPP_JAR)
# export CLASSPATH=/usr/local/lib/antlr-*-complete.jar

DOMAINNAME = oj.compilers.cpl.icu
ANTLR = java -jar $(ANTLR_PATH) -listener -visitor -long-messages
# ANTLR = java -jar /usr/local/lib/antlr-*-complete.jar -listener -visitor -long-messages
JAVAC = javac -g
JAVA = java


PFILE = $(shell find . -name "SysYParser.g4")
LFILE = $(shell find . -name "SysYLexer.g4")
JAVAFILE = $(shell find . -name "*.java")
# JAVAFILE = src/*.java
# ANTLRPATH = $(shell find /usr/local/lib -name "antlr-*-complete.jar")


compile: antlr
# 	$(call git_commit,"make")
	rm -rf src/.antlr
	mkdir -p classes
	$(JAVAC) -classpath $(CLASSPATH) $(JAVAFILE) -d classes
# 	$(JAVAC) -classpath $(ANTLRPATH) $(JAVAFILE) -d classes

run: compile
	java -classpath ./classes:$(CLASSPATH) Main $(FILEPATH)
# 	java -classpath ./classes:$(ANTLRPATH) Main $(FILEPATH)


antlr: $(LFILE) $(PFILE) 
	$(ANTLR) $(PFILE) $(LFILE)


test: compile
# 	$(call git_commit, "test")
	rm -f ./tests/nohup*.out
	if [ -e nohup.out ]; then rm nohup.out; fi
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test1.sysy ./tests/test1.ll > tests/nohup1.out &
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test2.sysy ./tests/test2.ll > tests/nohup2.out &
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test3.sysy ./tests/test3.ll > tests/nohup3.out &
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test4.sysy ./tests/test4.ll > tests/nohup4.out &
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test5.sysy ./tests/test5.ll > tests/nohup5.out &
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test6.sysy ./tests/test6.ll > tests/nohup6.out &
	nohup java -classpath ./classes:$(CLASSPATH) Main ./tests/test7.sysy ./tests/test7.ll > tests/nohup7.out &

clean:
	rm -f src/*.tokens
	rm -f src/*.interp
	rm -f src/SysYLexer.java src/SysYParser.java src/SysYParserBaseListener.java src/SysYParserBaseVisitor.java src/SysYParserListener.java src/SysYParserVisitor.java
	rm -rf classes
	rm -rf out
	rm -rf src/.antlr


submit: clean
	git gc
	bash submit.sh


.PHONY: compile antlr test run clean submit


